package net.djrogers.aac4u.data.backup

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import net.djrogers.aac4u.data.local.database.AAC4UDatabase
import net.djrogers.aac4u.util.Constants
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Handles backup and restore of the complete AAC4U configuration.
 *
 * Backup format: ZIP file containing:
 * - aac4u.db (Room database)
 * - custom/ (custom user images)
 * - metadata.json (app version, backup date, profile info)
 *
 * CRITICAL: For AAC users, losing their communication setup is devastating.
 * Backup/restore must be bulletproof and easy to use.
 */
@Singleton
class BackupManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val database: AAC4UDatabase
) {
    /**
     * Create a backup ZIP file containing the full configuration.
     * Returns the path to the created backup file.
     */
    suspend fun createBackup(): File {
        // Close database connections for clean copy
        database.close()

        val backupDir = File(context.filesDir, "backups").also { it.mkdirs() }
        val timestamp = System.currentTimeMillis()
        val backupFile = File(backupDir, "aac4u_backup_$timestamp${Constants.BACKUP_FILE_EXTENSION}")

        ZipOutputStream(FileOutputStream(backupFile)).use { zip ->
            // Add database
            val dbFile = context.getDatabasePath(AAC4UDatabase.DATABASE_NAME)
            if (dbFile.exists()) {
                addFileToZip(zip, dbFile, "aac4u.db")
            }

            // Add custom images
            val customDir = File(context.filesDir, Constants.CUSTOM_IMAGES_DIRECTORY)
            if (customDir.exists()) {
                customDir.listFiles()?.forEach { file ->
                    addFileToZip(zip, file, "custom/${file.name}")
                }
            }

            // Add metadata
            val metadata = """
                {
                    "version": 1,
                    "timestamp": $timestamp,
                    "app_version": "0.1.0"
                }
            """.trimIndent()
            zip.putNextEntry(ZipEntry("metadata.json"))
            zip.write(metadata.toByteArray())
            zip.closeEntry()
        }

        return backupFile
    }

    /**
     * Restore from a backup ZIP file.
     * WARNING: This replaces all existing data.
     */
    suspend fun restoreBackup(backupFile: File): Boolean {
        if (!backupFile.exists()) return false

        try {
            // Close database
            database.close()

            ZipInputStream(FileInputStream(backupFile)).use { zip ->
                var entry = zip.nextEntry
                while (entry != null) {
                    when {
                        entry.name == "aac4u.db" -> {
                            val dbFile = context.getDatabasePath(AAC4UDatabase.DATABASE_NAME)
                            FileOutputStream(dbFile).use { out ->
                                zip.copyTo(out)
                            }
                        }
                        entry.name.startsWith("custom/") -> {
                            val customDir = File(context.filesDir, Constants.CUSTOM_IMAGES_DIRECTORY)
                            customDir.mkdirs()
                            val file = File(customDir, entry.name.removePrefix("custom/"))
                            FileOutputStream(file).use { out ->
                                zip.copyTo(out)
                            }
                        }
                        // metadata.json — read but don't need to store separately
                    }
                    zip.closeEntry()
                    entry = zip.nextEntry
                }
            }

            return true
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }

    private fun addFileToZip(zip: ZipOutputStream, file: File, entryName: String) {
        zip.putNextEntry(ZipEntry(entryName))
        FileInputStream(file).use { input ->
            input.copyTo(zip)
        }
        zip.closeEntry()
    }
}
