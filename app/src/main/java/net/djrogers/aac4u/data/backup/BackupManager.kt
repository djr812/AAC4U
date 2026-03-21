package net.djrogers.aac4u.data.backup

import android.content.Context
import android.net.Uri
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
        private const val SALT_SIZE = 16
        private const val IV_SIZE = 12
        private const val GCM_TAG_LENGTH = 128
        private const val KEY_LENGTH = 256
        private const val ITERATION_COUNT = 100_000
        private const val ENCRYPTED_ENTRY_NAME = "backup.enc"
        private const val SALT_ENTRY_NAME = "salt.bin"
        private const val IV_ENTRY_NAME = "iv.bin"
        private const val META_ENTRY_NAME = "meta.json"
    }

    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
    }

    // ══════════════════════════════════════
    // EXPORT
    // ══════════════════════════════════════

    /**
     * Export a single profile as an encrypted ZIP.
     * Returns the ZIP bytes ready to be written to a file or shared.
     */
    suspend fun exportProfile(profileId: Long, password: String): ByteArray = withContext(Dispatchers.IO) {
        val profileEntity = database.profileDao().getProfileById(profileId)
            ?: throw IllegalArgumentException("Profile not found")

        val profileBackup = buildProfileBackup(profileEntity)
        val backupData = BackupData(
            backupType = "single_profile",
            profiles = listOf(profileBackup)
        )

        createEncryptedZip(backupData, password)
    }

    /**
     * Export all profiles (except Default template) as an encrypted ZIP.
     */
    suspend fun exportAllProfiles(password: String): ByteArray = withContext(Dispatchers.IO) {
        val allProfiles = database.profileDao().getAllProfiles().first()
        val userProfiles = allProfiles.filter { it.name != "Default" }

        if (userProfiles.isEmpty()) {
            throw IllegalStateException("No profiles to export")
        }

        val profileBackups = userProfiles.map { buildProfileBackup(it) }
        val backupData = BackupData(
            backupType = "all_profiles",
            profiles = profileBackups
        )

        createEncryptedZip(backupData, password)
    }

    // ══════════════════════════════════════
    // IMPORT
    // ══════════════════════════════════════

    /**
     * Read and decrypt a backup file, returning the parsed BackupData.
     * Call this first to preview what's in the backup before importing.
     */
    suspend fun readBackup(uri: Uri, password: String): BackupData = withContext(Dispatchers.IO) {
        val zipBytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
            ?: throw IllegalArgumentException("Cannot read backup file")

        decryptZip(zipBytes, password)
    }

    /**
     * Import a single profile from backup data, adding it as a new profile.
     */
    suspend fun importAsNewProfile(profileBackup: ProfileBackup): Long = withContext(Dispatchers.IO) {
        val profileId = database.profileDao().insertProfile(
            ProfileEntity(
                name = profileBackup.name,
                avatar = profileBackup.avatar,
                ageRange = profileBackup.ageRange,
                gridColumns = profileBackup.gridColumns,
                gridRows = profileBackup.gridRows,
                buttonPaddingDp = profileBackup.buttonPaddingDp,
                showLabels = profileBackup.showLabels,
                labelPosition = profileBackup.labelPosition,
                inputMethod = profileBackup.inputMethod,
                feedbackMode = profileBackup.feedbackMode,
                ttsVoiceName = profileBackup.ttsVoiceName,
                ttsRate = profileBackup.ttsRate,
                ttsPitch = profileBackup.ttsPitch,
                isActive = false,
                highContrastEnabled = profileBackup.highContrastEnabled,
                dwellTimeMs = profileBackup.dwellTimeMs,
                scanSpeedMs = profileBackup.scanSpeedMs
            )
        )

        importCategoriesAndButtons(profileId, profileBackup.categories)
        profileId
    }

    /**
     * Import a profile from backup data, replacing an existing profile's vocabulary.
     * Keeps the existing profile's ID and active state, replaces everything else.
     */
    suspend fun importReplaceProfile(
        existingProfileId: Long,
        profileBackup: ProfileBackup
    ) = withContext(Dispatchers.IO) {
        val existing = database.profileDao().getProfileById(existingProfileId)
            ?: throw IllegalArgumentException("Profile not found")

        // Delete all existing categories (cascade deletes buttons too)
        val existingCategories = database.categoryDao()
            .getAllCategoriesByProfile(existingProfileId).first()
        existingCategories.forEach { category ->
            database.categoryDao().deleteCategory(category.id)
        }

        // Update profile settings from backup
        database.profileDao().updateProfile(
            existing.copy(
                name = profileBackup.name,
                avatar = profileBackup.avatar,
                ageRange = profileBackup.ageRange,
                gridColumns = profileBackup.gridColumns,
                gridRows = profileBackup.gridRows,
                buttonPaddingDp = profileBackup.buttonPaddingDp,
                showLabels = profileBackup.showLabels,
                labelPosition = profileBackup.labelPosition,
                inputMethod = profileBackup.inputMethod,
                feedbackMode = profileBackup.feedbackMode,
                ttsVoiceName = profileBackup.ttsVoiceName,
                ttsRate = profileBackup.ttsRate,
                ttsPitch = profileBackup.ttsPitch,
                highContrastEnabled = profileBackup.highContrastEnabled,
                dwellTimeMs = profileBackup.dwellTimeMs,
                scanSpeedMs = profileBackup.scanSpeedMs
            )
        )

        // Import categories and buttons
        importCategoriesAndButtons(existingProfileId, profileBackup.categories)
    }

    /**
     * Import all profiles from a backup, each as a new profile.
     */
    suspend fun importAllAsNew(backupData: BackupData): List<Long> = withContext(Dispatchers.IO) {
        backupData.profiles.map { profileBackup ->
            importAsNewProfile(profileBackup)
        }
    }

    // ══════════════════════════════════════
    // INTERNAL HELPERS
    // ══════════════════════════════════════

    private suspend fun buildProfileBackup(profileEntity: ProfileEntity): ProfileBackup {
        val categories = database.categoryDao()
            .getAllCategoriesByProfile(profileEntity.id).first()

        val categoryBackups = categories.map { category ->
            val buttons = database.buttonDao()
                .getAllButtonsByCategory(category.id).first()

            CategoryBackup(
                name = category.name,
                iconPath = category.iconPath,
                sortOrder = category.sortOrder,
                isVisible = category.isVisible,
                vocabularyType = category.vocabularyType,
                buttons = buttons.map { button ->
                    ButtonBackup(
                        label = button.label,
                        phrase = button.phrase,
                        imagePath = button.imagePath,
                        imageType = button.imageType,
                        sortOrder = button.sortOrder,
                        isVisible = button.isVisible,
                        backgroundColor = button.backgroundColor,
                        isQuickPhrase = button.isQuickPhrase
                    )
                }
            )
        }

        return ProfileBackup(
            name = profileEntity.name,
            avatar = profileEntity.avatar,
            ageRange = profileEntity.ageRange,
            gridColumns = profileEntity.gridColumns,
            gridRows = profileEntity.gridRows,
            buttonPaddingDp = profileEntity.buttonPaddingDp,
            showLabels = profileEntity.showLabels,
            labelPosition = profileEntity.labelPosition,
            inputMethod = profileEntity.inputMethod,
            feedbackMode = profileEntity.feedbackMode,
            ttsVoiceName = profileEntity.ttsVoiceName,
            ttsRate = profileEntity.ttsRate,
            ttsPitch = profileEntity.ttsPitch,
            highContrastEnabled = profileEntity.highContrastEnabled,
            dwellTimeMs = profileEntity.dwellTimeMs,
            scanSpeedMs = profileEntity.scanSpeedMs,
            categories = categoryBackups
        )
    }

    private suspend fun importCategoriesAndButtons(profileId: Long, categories: List<CategoryBackup>) {
        for (categoryBackup in categories) {
            val categoryId = database.categoryDao().insertCategory(
                CategoryEntity(
                    profileId = profileId,
                    name = categoryBackup.name,
                    iconPath = categoryBackup.iconPath,
                    sortOrder = categoryBackup.sortOrder,
                    isVisible = categoryBackup.isVisible,
                    vocabularyType = categoryBackup.vocabularyType
                )
            )

            for (buttonBackup in categoryBackup.buttons) {
                database.buttonDao().insertButton(
                    ButtonEntity(
                        categoryId = categoryId,
                        label = buttonBackup.label,
                        phrase = buttonBackup.phrase,
                        imagePath = buttonBackup.imagePath,
                        imageType = buttonBackup.imageType,
                        sortOrder = buttonBackup.sortOrder,
                        isVisible = buttonBackup.isVisible,
                        backgroundColor = buttonBackup.backgroundColor,
                        isQuickPhrase = buttonBackup.isQuickPhrase
                    )
                )
            }
        }
    }

    // ══════════════════════════════════════
    // ENCRYPTION
    // ══════════════════════════════════════

    private fun createEncryptedZip(backupData: BackupData, password: String): ByteArray {
        val jsonString = json.encodeToString(backupData)
        val jsonBytes = jsonString.toByteArray(Charsets.UTF_8)

        // Generate salt and IV
        val salt = ByteArray(SALT_SIZE).also { SecureRandom().nextBytes(it) }
        val iv = ByteArray(IV_SIZE).also { SecureRandom().nextBytes(it) }

        // Derive key from password
        val key = deriveKey(password, salt)

        // Encrypt
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, key, GCMParameterSpec(GCM_TAG_LENGTH, iv))
        val encrypted = cipher.doFinal(jsonBytes)

        // Create metadata
        val meta = json.encodeToString(
            mapOf(
                "version" to "1",
                "app" to "AAC4U",
                "encrypted" to "true",
                "profiles" to backupData.profiles.size.toString(),
                "type" to backupData.backupType
            )
        )

        // Package into ZIP
        val zipBytes = ByteArrayOutputStream()
        ZipOutputStream(zipBytes).use { zip ->
            // Meta (unencrypted — lets the app know what's inside without decrypting)
            zip.putNextEntry(ZipEntry(META_ENTRY_NAME))
            zip.write(meta.toByteArray(Charsets.UTF_8))
            zip.closeEntry()

            // Salt
            zip.putNextEntry(ZipEntry(SALT_ENTRY_NAME))
            zip.write(salt)
            zip.closeEntry()

            // IV
            zip.putNextEntry(ZipEntry(IV_ENTRY_NAME))
            zip.write(iv)
            zip.closeEntry()

            // Encrypted data
            zip.putNextEntry(ZipEntry(ENCRYPTED_ENTRY_NAME))
            zip.write(encrypted)
            zip.closeEntry()
        }

        return zipBytes.toByteArray()
    }

    private fun decryptZip(zipBytes: ByteArray, password: String): BackupData {
        var salt: ByteArray? = null
        var iv: ByteArray? = null
        var encrypted: ByteArray? = null

        ZipInputStream(ByteArrayInputStream(zipBytes)).use { zip ->
            var entry = zip.nextEntry
            while (entry != null) {
                when (entry.name) {
                    SALT_ENTRY_NAME -> salt = zip.readBytes()
                    IV_ENTRY_NAME -> iv = zip.readBytes()
                    ENCRYPTED_ENTRY_NAME -> encrypted = zip.readBytes()
                }
                zip.closeEntry()
                entry = zip.nextEntry
            }
        }

        if (salt == null || iv == null || encrypted == null) {
            throw IllegalArgumentException("Invalid backup file — missing encryption data")
        }

        // Derive key and decrypt
        val key = deriveKey(password, salt!!)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(GCM_TAG_LENGTH, iv!!))

        val decrypted = try {
            cipher.doFinal(encrypted!!)
        } catch (e: Exception) {
            throw IllegalArgumentException("Incorrect password or corrupted backup file")
        }

        val jsonString = String(decrypted, Charsets.UTF_8)
        return json.decodeFromString<BackupData>(jsonString)
    }

    private fun deriveKey(password: String, salt: ByteArray): SecretKeySpec {
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val spec = PBEKeySpec(password.toCharArray(), salt, ITERATION_COUNT, KEY_LENGTH)
        val secretKey = factory.generateSecret(spec)
        return SecretKeySpec(secretKey.encoded, "AES")
    }
}
