package net.djrogers.aac4u.domain.usecase.tts

import net.djrogers.aac4u.domain.model.PhraseHistoryEntry
import net.djrogers.aac4u.domain.repository.PhraseHistoryRepository
import javax.inject.Inject

/**
 * Orchestrates speaking a phrase: triggers TTS and records to history.
 *
 * Note: The actual TTS call happens in the ViewModel/UI layer because
 * Android's TextToSpeech requires a Context. This use case handles
 * the domain-side bookkeeping (recording history).
 */
class SpeakPhraseUseCase @Inject constructor(
    private val phraseHistoryRepository: PhraseHistoryRepository
) {
    /**
     * Record the phrase in history. The ViewModel calls TTS separately.
     *
     * @param phrase The full phrase being spoken
     * @param profileId The active profile
     * @param wasEdited True if user manually typed/modified the phrase
     */
    suspend operator fun invoke(
        phrase: String,
        profileId: Long,
        wasEdited: Boolean = false
    ) {
        if (phrase.isBlank()) return

        phraseHistoryRepository.recordPhrase(
            PhraseHistoryEntry(
                profileId = profileId,
                fullPhrase = phrase.trim(),
                wasEdited = wasEdited
            )
        )
    }
}
