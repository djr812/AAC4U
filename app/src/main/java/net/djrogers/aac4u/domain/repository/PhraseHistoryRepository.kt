package net.djrogers.aac4u.domain.repository

import kotlinx.coroutines.flow.Flow
import net.djrogers.aac4u.domain.model.PhraseHistoryEntry

interface PhraseHistoryRepository {
    fun getHistory(profileId: Long, limit: Int = 50): Flow<List<PhraseHistoryEntry>>
    suspend fun recordPhrase(entry: PhraseHistoryEntry)
    suspend fun clearHistory(profileId: Long)
    suspend fun exportHistory(profileId: Long): List<PhraseHistoryEntry>
}
