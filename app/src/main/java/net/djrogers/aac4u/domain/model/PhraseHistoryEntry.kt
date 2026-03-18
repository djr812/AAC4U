package net.djrogers.aac4u.domain.model

/**
 * Records a phrase that was spoken via TTS.
 * Used for speech history export and frequency-based prediction.
 */
data class PhraseHistoryEntry(
    val id: Long = 0,
    val profileId: Long,
    val fullPhrase: String,
    val timestamp: Long = System.currentTimeMillis(),
    val wasEdited: Boolean = false // True if user typed/modified the phrase manually
)
