package net.djrogers.aac4u.domain.repository

import kotlinx.coroutines.flow.Flow
import net.djrogers.aac4u.domain.model.AACButton

interface PredictionRepository {
    /**
     * Record that buttonB was tapped after buttonA.
     * Builds the bigram frequency table for prediction.
     */
    suspend fun recordSequence(profileId: Long, buttonAId: Long, buttonBId: Long)

    /**
     * Get predicted next buttons based on the last button tapped.
     * Returns buttons ordered by frequency (most likely first).
     */
    fun getPredictions(profileId: Long, lastButtonId: Long, limit: Int = 5): Flow<List<AACButton>>

    /**
     * Get the most frequently used buttons overall (fallback when no sequence data).
     */
    fun getMostUsedButtons(profileId: Long, limit: Int = 5): Flow<List<AACButton>>
}
