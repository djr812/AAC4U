package net.djrogers.aac4u.domain.usecase.grid

import net.djrogers.aac4u.domain.model.AACButton
import net.djrogers.aac4u.domain.repository.ButtonRepository
import net.djrogers.aac4u.domain.repository.PredictionRepository
import javax.inject.Inject

/**
 * Handles the action of selecting (tapping) an AAC button.
 * Records usage for prediction, updates usage count, and returns the phrase to speak.
 */
class SelectButtonUseCase @Inject constructor(
    private val buttonRepository: ButtonRepository,
    private val predictionRepository: PredictionRepository
) {
    /**
     * @param button The button that was tapped
     * @param previousButtonId The last button tapped (for bigram prediction), null if first in sequence
     * @param profileId The active user profile
     * @return The phrase to be spoken or added to the sentence bar
     */
    suspend operator fun invoke(
        button: AACButton,
        previousButtonId: Long?,
        profileId: Long
    ): String {
        // Record usage count
        buttonRepository.incrementUsageCount(button.id)

        // Record sequence for prediction (if there was a previous button)
        if (previousButtonId != null) {
            predictionRepository.recordSequence(
                profileId = profileId,
                buttonAId = previousButtonId,
                buttonBId = button.id
            )
        }

        return button.phrase
    }
}
