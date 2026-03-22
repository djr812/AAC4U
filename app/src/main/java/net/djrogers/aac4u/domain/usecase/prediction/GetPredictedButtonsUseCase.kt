package net.djrogers.aac4u.domain.usecase.prediction

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import net.djrogers.aac4u.domain.model.AACButton
import net.djrogers.aac4u.domain.repository.ButtonRepository
import net.djrogers.aac4u.domain.repository.PredictionRepository
import javax.inject.Inject

/**
 * Gets predicted next buttons, combining personal usage data with
 * common English phrase patterns as a fallback.
 *
 * Priority:
 * 1. Personal bigram predictions (from user's actual usage)
 * 2. Common phrase patterns (English word pair probabilities)
 * 3. Most frequently used buttons (global popularity)
 */
class GetPredictedButtonsUseCase @Inject constructor(
    private val predictionRepository: PredictionRepository,
    private val buttonRepository: ButtonRepository
) {
    operator fun invoke(
        profileId: Long,
        lastButtonId: Long,
        limit: Int = 3
    ): Flow<List<AACButton>> = flow {
        // 1. Try personal bigram predictions first
        val personalPredictions = predictionRepository
            .getPredictions(profileId, lastButtonId, limit)
            .first()

        if (personalPredictions.size >= limit) {
            emit(personalPredictions.take(limit))
            return@flow
        }

        // 2. Fill remaining slots with common phrase pattern matches
        val results = personalPredictions.toMutableList()
        val existingLabels = results.map { it.label.lowercase() }.toMutableSet()

        // Get the label of the last button to look up patterns
        val lastButton = buttonRepository.getButtonById(lastButtonId)
        if (lastButton != null) {
            val commonNextWords = CommonPhrasePatterns.getNextWords(lastButton.label)

            if (commonNextWords.isNotEmpty()) {
                // Find matching buttons in the user's vocabulary
                val allButtons = getAllProfileButtons(profileId)

                for (nextWord in commonNextWords) {
                    if (results.size >= limit) break
                    if (nextWord.lowercase() in existingLabels) continue

                    val matchingButton = allButtons.find {
                        it.label.equals(nextWord, ignoreCase = true)
                    }
                    if (matchingButton != null) {
                        results.add(matchingButton)
                        existingLabels.add(matchingButton.label.lowercase())
                    }
                }
            }
        }

        emit(results.take(limit))
    }

    /**
     * Get all visible buttons across all categories for a profile.
     * Cached per invocation since this is called during prediction.
     */
    private suspend fun getAllProfileButtons(profileId: Long): List<AACButton> {
        return buttonRepository.getButtonsByProfile(profileId).first()
    }
}
