package net.djrogers.aac4u.domain.usecase.prediction

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import net.djrogers.aac4u.domain.model.AACButton
import net.djrogers.aac4u.domain.repository.PredictionRepository
import javax.inject.Inject

/**
 * Returns predicted next buttons based on usage patterns.
 * Falls back to most-used buttons if no sequence data exists.
 */
class GetPredictedButtonsUseCase @Inject constructor(
    private val predictionRepository: PredictionRepository
) {
    /**
     * @param profileId Active profile
     * @param lastButtonId The most recently tapped button (null if none)
     * @param limit Max predictions to return
     */
    operator fun invoke(
        profileId: Long,
        lastButtonId: Long?,
        limit: Int = 5
    ): Flow<List<AACButton>> = flow {
        if (lastButtonId != null) {
            emitAll(predictionRepository.getPredictions(profileId, lastButtonId, limit))
        } else {
            // No previous button — fall back to most frequently used
            emitAll(predictionRepository.getMostUsedButtons(profileId, limit))
        }
    }
}
