package net.djrogers.aac4u.domain.model

/**
 * Represents a user profile with all their personalised settings.
 * Each profile has its own categories, buttons, grid config, and TTS preferences.
 */
data class UserProfile(
    val id: Long = 0,
    val name: String,
    val gridConfig: GridConfig = GridConfig(),
    val inputMethod: InputMethod = InputMethod.TAP,
    val feedbackMode: FeedbackMode = FeedbackMode.BOTH,
    val ttsVoiceName: String? = null, // null = system default
    val ttsRate: Float = 1.0f,        // 0.5 to 2.0
    val ttsPitch: Float = 1.0f,       // 0.5 to 2.0
    val isActive: Boolean = false,
    val highContrastEnabled: Boolean = false,
    val dwellTimeMs: Long = 1500,     // Dwell selection delay (ms)
    val scanSpeedMs: Long = 2000      // Scanning auto-advance delay (ms)
)
