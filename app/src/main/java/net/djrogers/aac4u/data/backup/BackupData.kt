package net.djrogers.aac4u.data.backup

import kotlinx.serialization.Serializable

/**
 * Serializable backup format for a complete AAC4U backup.
 * Contains one or more profiles with all their categories and buttons.
 */
@Serializable
data class BackupData(
    val version: Int = 1,
    val appVersion: String = "0.1.0",
    val timestamp: Long = System.currentTimeMillis(),
    val backupType: String, // "single_profile" or "all_profiles"
    val profiles: List<ProfileBackup>
)

@Serializable
data class ProfileBackup(
    val name: String,
    val avatar: String = "😊",
    val ageRange: String = "ADULT",
    val gridColumns: Int = 4,
    val gridRows: Int = 4,
    val buttonPaddingDp: Int = 4,
    val showLabels: Boolean = true,
    val labelPosition: String = "BELOW",
    val inputMethod: String = "TAP",
    val feedbackMode: String = "BOTH",
    val ttsVoiceName: String? = null,
    val ttsRate: Float = 1.0f,
    val ttsPitch: Float = 1.0f,
    val highContrastEnabled: Boolean = false,
    val dwellTimeMs: Long = 1500,
    val scanSpeedMs: Long = 2000,
    val categories: List<CategoryBackup>
)

@Serializable
data class CategoryBackup(
    val name: String,
    val iconPath: String? = null,
    val sortOrder: Int = 0,
    val isVisible: Boolean = true,
    val vocabularyType: String = "FRINGE",
    val buttons: List<ButtonBackup>
)

@Serializable
data class ButtonBackup(
    val label: String,
    val phrase: String,
    val imagePath: String? = null,
    val imageType: String = "BUNDLED",
    val sortOrder: Int = 0,
    val isVisible: Boolean = true,
    val backgroundColor: String? = null,
    val isQuickPhrase: Boolean = false
)
