package net.djrogers.aac4u.domain.model

/**
 * Represents a user profile with all their personalised settings.
 * Each profile has its own categories, buttons, grid config, and TTS preferences.
 */
data class UserProfile(
    val id: Long = 0,
    val name: String,
    val avatar: String = "😊",
    val ageRange: AgeRange = AgeRange.ADULT,
    val gridConfig: GridConfig = GridConfig(),
    val inputMethod: InputMethod = InputMethod.TAP,
    val feedbackMode: FeedbackMode = FeedbackMode.BOTH,
    val ttsVoiceName: String? = null,
    val ttsRate: Float = 1.0f,
    val ttsPitch: Float = 1.0f,
    val isActive: Boolean = false,
    val highContrastEnabled: Boolean = false,
    val dwellTimeMs: Long = 1500,
    val scanSpeedMs: Long = 2000
)

/**
 * Age ranges for vocabulary level selection.
 * Used to determine appropriate core/fringe word lists in future.
 */
enum class AgeRange(val label: String, val description: String) {
    TODDLER("2–4", "Early communicator, basic needs and wants"),
    YOUNG_CHILD("5–7", "Early reader, simple phrases and social words"),
    CHILD("8–12", "Expanding vocabulary, school and social contexts"),
    TEEN("13–17", "Complex sentences, emotional expression, independence"),
    ADULT("18+", "Full vocabulary, work and community participation")
}
