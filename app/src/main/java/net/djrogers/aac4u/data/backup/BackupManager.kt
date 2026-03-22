package net.djrogers.aac4u.data.backup

import android.content.Context
import android.net.Uri
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import net.djrogers.aac4u.data.local.database.AAC4UDatabase
import net.djrogers.aac4u.data.local.database.entity.*
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.security.SecureRandom
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BackupManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val database: AAC4UDatabase
) {
    companion object {
        private const val TAG = "AAC4U_BACKUP"
        private const val SALT_SIZE = 16
        private const val IV_SIZE = 12
        private const val GCM_TAG_LENGTH = 128
        private const val KEY_LENGTH = 256
        private const val ITERATION_COUNT = 100_000
        private const val ENCRYPTED_ENTRY_NAME = "backup.enc"
        private const val SALT_ENTRY_NAME = "salt.bin"
        private const val IV_ENTRY_NAME = "iv.bin"
        private const val META_ENTRY_NAME = "meta.json"
        private const val IMAGES_PREFIX = "images/"
        private const val DOWNLOADED_DIR = "downloaded_symbols"
    }

    private val json = Json { prettyPrint = true; ignoreUnknownKeys = true }
    private val downloadedDir: File by lazy { File(context.filesDir, DOWNLOADED_DIR) }

    suspend fun exportProfile(profileId: Long, password: String): ByteArray = withContext(Dispatchers.IO) {
        val profileEntity = database.profileDao().getProfileById(profileId)
            ?: throw IllegalArgumentException("Profile not found")
        val profileBackup = buildProfileBackup(profileEntity)
        val backupData = BackupData(backupType = "single_profile", profiles = listOf(profileBackup))
        val imageFiles = collectImageFiles(listOf(profileBackup))
        createEncryptedZip(backupData, password, imageFiles)
    }

    suspend fun exportAllProfiles(password: String): ByteArray = withContext(Dispatchers.IO) {
        val allProfiles = database.profileDao().getAllProfiles().first()
        val userProfiles = allProfiles.filter { it.name != "Default" }
        if (userProfiles.isEmpty()) throw IllegalStateException("No profiles to export")

        val profileBackups = userProfiles.map { buildProfileBackup(it) }
        val backupData = BackupData(backupType = "all_profiles", profiles = profileBackups)
        val imageFiles = collectImageFiles(profileBackups)

        for (pb in profileBackups) {
            Log.d(TAG, "EXPORT profile '${pb.name}': ${pb.categories.size} categories")
            for (cat in pb.categories) {
                Log.d(TAG, "  EXPORT cat '${cat.name}' (${cat.vocabularyType}): ${cat.buttons.size} buttons")
                for (btn in cat.buttons) {
                    Log.d(TAG, "    EXPORT btn: '${btn.label}'")
                }
            }
        }

        createEncryptedZip(backupData, password, imageFiles)
    }

    suspend fun readBackup(uri: Uri, password: String): BackupData = withContext(Dispatchers.IO) {
        val zipBytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
            ?: throw IllegalArgumentException("Cannot read backup file")
        val (backupData, _) = decryptZip(zipBytes, password)
        for (pb in backupData.profiles) {
            Log.d(TAG, "READ profile '${pb.name}': ${pb.categories.size} categories")
            for (cat in pb.categories) {
                Log.d(TAG, "  READ cat '${cat.name}' (${cat.vocabularyType}): ${cat.buttons.size} buttons")
                for (btn in cat.buttons) {
                    Log.d(TAG, "    READ btn: '${btn.label}'")
                }
            }
        }
        backupData
    }

    suspend fun importAsNewProfile(profileBackup: ProfileBackup, uri: Uri? = null, password: String? = null): Long = withContext(Dispatchers.IO) {
        if (uri != null && password != null) restoreImages(uri, password)

        val profileId = database.profileDao().insertProfile(
            ProfileEntity(
                name = profileBackup.name, avatar = profileBackup.avatar, ageRange = profileBackup.ageRange,
                gridColumns = profileBackup.gridColumns, gridRows = profileBackup.gridRows,
                buttonPaddingDp = profileBackup.buttonPaddingDp, showLabels = profileBackup.showLabels,
                labelPosition = profileBackup.labelPosition, inputMethod = profileBackup.inputMethod,
                feedbackMode = profileBackup.feedbackMode, ttsVoiceName = profileBackup.ttsVoiceName,
                ttsRate = profileBackup.ttsRate, ttsPitch = profileBackup.ttsPitch, isActive = false,
                highContrastEnabled = profileBackup.highContrastEnabled,
                dwellTimeMs = profileBackup.dwellTimeMs, scanSpeedMs = profileBackup.scanSpeedMs
            )
        )

        Log.d(TAG, "IMPORT profile '${profileBackup.name}' as ID $profileId: ${profileBackup.categories.size} categories")
        importCategoriesAndButtons(profileId, profileBackup.categories)
        profileId
    }

    suspend fun importReplaceProfile(existingProfileId: Long, profileBackup: ProfileBackup, uri: Uri? = null, password: String? = null) = withContext(Dispatchers.IO) {
        if (uri != null && password != null) restoreImages(uri, password)

        val existing = database.profileDao().getProfileById(existingProfileId)
            ?: throw IllegalArgumentException("Profile not found")

        val existingCategories = database.categoryDao().getAllCategoriesByProfile(existingProfileId).first()
        existingCategories.forEach { database.categoryDao().deleteCategory(it.id) }

        database.profileDao().updateProfile(existing.copy(
            name = profileBackup.name, avatar = profileBackup.avatar, ageRange = profileBackup.ageRange,
            gridColumns = profileBackup.gridColumns, gridRows = profileBackup.gridRows,
            buttonPaddingDp = profileBackup.buttonPaddingDp, showLabels = profileBackup.showLabels,
            labelPosition = profileBackup.labelPosition, inputMethod = profileBackup.inputMethod,
            feedbackMode = profileBackup.feedbackMode, ttsVoiceName = profileBackup.ttsVoiceName,
            ttsRate = profileBackup.ttsRate, ttsPitch = profileBackup.ttsPitch,
            highContrastEnabled = profileBackup.highContrastEnabled,
            dwellTimeMs = profileBackup.dwellTimeMs, scanSpeedMs = profileBackup.scanSpeedMs
        ))

        importCategoriesAndButtons(existingProfileId, profileBackup.categories)
    }

    suspend fun importAllAsNew(backupData: BackupData, uri: Uri? = null, password: String? = null): List<Long> = withContext(Dispatchers.IO) {
        if (uri != null && password != null) restoreImages(uri, password)

        backupData.profiles.map { profileBackup ->
            val profileId = database.profileDao().insertProfile(
                ProfileEntity(
                    name = profileBackup.name, avatar = profileBackup.avatar, ageRange = profileBackup.ageRange,
                    gridColumns = profileBackup.gridColumns, gridRows = profileBackup.gridRows,
                    buttonPaddingDp = profileBackup.buttonPaddingDp, showLabels = profileBackup.showLabels,
                    labelPosition = profileBackup.labelPosition, inputMethod = profileBackup.inputMethod,
                    feedbackMode = profileBackup.feedbackMode, ttsVoiceName = profileBackup.ttsVoiceName,
                    ttsRate = profileBackup.ttsRate, ttsPitch = profileBackup.ttsPitch, isActive = false,
                    highContrastEnabled = profileBackup.highContrastEnabled,
                    dwellTimeMs = profileBackup.dwellTimeMs, scanSpeedMs = profileBackup.scanSpeedMs
                )
            )
            Log.d(TAG, "IMPORT ALL: profile '${profileBackup.name}' as ID $profileId: ${profileBackup.categories.size} categories")
            importCategoriesAndButtons(profileId, profileBackup.categories)
            profileId
        }
    }

    // ══════════════════════════════════════

    private fun collectImageFiles(profiles: List<ProfileBackup>): Map<String, ByteArray> {
        val imageFiles = mutableMapOf<String, ByteArray>()
        for (profile in profiles) {
            for (category in profile.categories) {
                for (button in category.buttons) {
                    val imagePath = button.imagePath ?: continue
                    if (imagePath.startsWith("file:///android_asset/")) continue
                    val file = File(imagePath)
                    if (file.exists() && file.length() > 0) {
                        val entryName = "$IMAGES_PREFIX${file.name}"
                        if (entryName !in imageFiles) {
                            imageFiles[entryName] = file.readBytes()
                            Log.d(TAG, "Including image: ${file.name} (${file.length()} bytes)")
                        }
                    } else {
                        Log.w(TAG, "Image NOT found: $imagePath")
                    }
                }
            }
        }
        return imageFiles
    }

    private fun restoreImages(uri: Uri, password: String) {
        val zipBytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() } ?: return
        downloadedDir.mkdirs()
        val (_, imageEntries) = decryptZip(zipBytes, password)
        Log.d(TAG, "Restoring ${imageEntries.size} images")
        for ((entryName, imageBytes) in imageEntries) {
            val filename = entryName.removePrefix(IMAGES_PREFIX)
            val targetFile = File(downloadedDir, filename)
            targetFile.writeBytes(imageBytes)
            Log.d(TAG, "Restored: $filename (${imageBytes.size} bytes)")
        }
    }

    private suspend fun buildProfileBackup(profileEntity: ProfileEntity): ProfileBackup {
        val categories = database.categoryDao().getAllCategoriesByProfile(profileEntity.id).first()
        Log.d(TAG, "Building backup for '${profileEntity.name}': ${categories.size} categories (IDs: ${categories.map { it.id }})")

        val categoryBackups = categories.map { category ->
            val buttons = database.buttonDao().getAllButtonsByCategory(category.id).first()
            Log.d(TAG, "  Cat '${category.name}' ID=${category.id} type=${category.vocabularyType}: ${buttons.size} buttons")

            CategoryBackup(
                name = category.name, iconPath = category.iconPath,
                sortOrder = category.sortOrder, isVisible = category.isVisible,
                vocabularyType = category.vocabularyType,
                buttons = buttons.map { button ->
                    ButtonBackup(
                        label = button.label, phrase = button.phrase,
                        imagePath = button.imagePath, imageType = button.imageType,
                        sortOrder = button.sortOrder, isVisible = button.isVisible,
                        backgroundColor = button.backgroundColor, isQuickPhrase = button.isQuickPhrase
                    )
                }
            )
        }

        return ProfileBackup(
            name = profileEntity.name, avatar = profileEntity.avatar, ageRange = profileEntity.ageRange,
            gridColumns = profileEntity.gridColumns, gridRows = profileEntity.gridRows,
            buttonPaddingDp = profileEntity.buttonPaddingDp, showLabels = profileEntity.showLabels,
            labelPosition = profileEntity.labelPosition, inputMethod = profileEntity.inputMethod,
            feedbackMode = profileEntity.feedbackMode, ttsVoiceName = profileEntity.ttsVoiceName,
            ttsRate = profileEntity.ttsRate, ttsPitch = profileEntity.ttsPitch,
            highContrastEnabled = profileEntity.highContrastEnabled,
            dwellTimeMs = profileEntity.dwellTimeMs, scanSpeedMs = profileEntity.scanSpeedMs,
            categories = categoryBackups
        )
    }

    private suspend fun importCategoriesAndButtons(profileId: Long, categories: List<CategoryBackup>) {
        for (categoryBackup in categories) {
            val categoryId = database.categoryDao().insertCategory(
                CategoryEntity(
                    profileId = profileId, name = categoryBackup.name,
                    iconPath = categoryBackup.iconPath, sortOrder = categoryBackup.sortOrder,
                    isVisible = categoryBackup.isVisible, vocabularyType = categoryBackup.vocabularyType
                )
            )
            Log.d(TAG, "  IMPORTED cat '${categoryBackup.name}' (${categoryBackup.vocabularyType}) as ID $categoryId: ${categoryBackup.buttons.size} buttons")

            for (buttonBackup in categoryBackup.buttons) {
                val resolvedImagePath = resolveImportedImagePath(buttonBackup.imagePath)
                database.buttonDao().insertButton(
                    ButtonEntity(
                        categoryId = categoryId, label = buttonBackup.label,
                        phrase = buttonBackup.phrase, imagePath = resolvedImagePath,
                        imageType = buttonBackup.imageType, sortOrder = buttonBackup.sortOrder,
                        isVisible = buttonBackup.isVisible, backgroundColor = buttonBackup.backgroundColor,
                        isQuickPhrase = buttonBackup.isQuickPhrase
                    )
                )
            }
        }
    }

    private fun resolveImportedImagePath(imagePath: String?): String? {
        if (imagePath.isNullOrBlank()) return null
        if (imagePath.startsWith("file:///android_asset/")) return imagePath
        val filename = File(imagePath).name
        return File(downloadedDir, filename).absolutePath
    }

    private fun createEncryptedZip(backupData: BackupData, password: String, imageFiles: Map<String, ByteArray> = emptyMap()): ByteArray {
        val jsonBytes = json.encodeToString(backupData).toByteArray(Charsets.UTF_8)
        val salt = ByteArray(SALT_SIZE).also { SecureRandom().nextBytes(it) }
        val iv = ByteArray(IV_SIZE).also { SecureRandom().nextBytes(it) }
        val key = deriveKey(password, salt)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, key, GCMParameterSpec(GCM_TAG_LENGTH, iv))
        val encrypted = cipher.doFinal(jsonBytes)

        val meta = json.encodeToString(mapOf("version" to "2", "app" to "AAC4U", "encrypted" to "true", "profiles" to backupData.profiles.size.toString(), "type" to backupData.backupType, "images" to imageFiles.size.toString()))

        val zipBytes = ByteArrayOutputStream()
        ZipOutputStream(zipBytes).use { zip ->
            zip.putNextEntry(ZipEntry(META_ENTRY_NAME)); zip.write(meta.toByteArray(Charsets.UTF_8)); zip.closeEntry()
            zip.putNextEntry(ZipEntry(SALT_ENTRY_NAME)); zip.write(salt); zip.closeEntry()
            zip.putNextEntry(ZipEntry(IV_ENTRY_NAME)); zip.write(iv); zip.closeEntry()
            zip.putNextEntry(ZipEntry(ENCRYPTED_ENTRY_NAME)); zip.write(encrypted); zip.closeEntry()
            for ((entryName, imageBytes) in imageFiles) {
                zip.putNextEntry(ZipEntry(entryName)); zip.write(imageBytes); zip.closeEntry()
            }
        }
        return zipBytes.toByteArray()
    }

    private fun decryptZip(zipBytes: ByteArray, password: String): Pair<BackupData, Map<String, ByteArray>> {
        var salt: ByteArray? = null; var iv: ByteArray? = null; var encrypted: ByteArray? = null
        val imageEntries = mutableMapOf<String, ByteArray>()

        ZipInputStream(ByteArrayInputStream(zipBytes)).use { zip ->
            var entry = zip.nextEntry
            while (entry != null) {
                when {
                    entry.name == SALT_ENTRY_NAME -> salt = zip.readBytes()
                    entry.name == IV_ENTRY_NAME -> iv = zip.readBytes()
                    entry.name == ENCRYPTED_ENTRY_NAME -> encrypted = zip.readBytes()
                    entry.name.startsWith(IMAGES_PREFIX) && !entry.isDirectory -> imageEntries[entry.name] = zip.readBytes()
                }
                zip.closeEntry(); entry = zip.nextEntry
            }
        }
        if (salt == null || iv == null || encrypted == null) throw IllegalArgumentException("Invalid backup file")

        val key = deriveKey(password, salt!!)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(GCM_TAG_LENGTH, iv!!))
        val decrypted = try { cipher.doFinal(encrypted!!) } catch (e: Exception) { throw IllegalArgumentException("Incorrect password or corrupted backup file") }

        return Pair(json.decodeFromString(String(decrypted, Charsets.UTF_8)), imageEntries)
    }

    private fun deriveKey(password: String, salt: ByteArray): SecretKeySpec {
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val spec = PBEKeySpec(password.toCharArray(), salt, ITERATION_COUNT, KEY_LENGTH)
        return SecretKeySpec(factory.generateSecret(spec).encoded, "AES")
    }
}