package net.djrogers.aac4u.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import net.djrogers.aac4u.data.local.database.dao.PhraseHistoryDao
import net.djrogers.aac4u.data.local.database.dao.PredictionDao
import net.djrogers.aac4u.data.local.database.mapper.toDomain
import net.djrogers.aac4u.data.local.database.mapper.toEntity
import net.djrogers.aac4u.domain.model.AACButton
import net.djrogers.aac4u.domain.model.PhraseHistoryEntry
import net.djrogers.aac4u.domain.repository.PhraseHistoryRepository
import net.djrogers.aac4u.domain.repository.PredictionRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PhraseHistoryRepositoryImpl @Inject constructor(
    private val phraseHistoryDao: PhraseHistoryDao
) : PhraseHistoryRepository {

    override fun getHistory(profileId: Long, limit: Int): Flow<List<PhraseHistoryEntry>> {
        return phraseHistoryDao.getHistory(profileId, limit).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun recordPhrase(entry: PhraseHistoryEntry) {
        phraseHistoryDao.insertEntry(entry.toEntity())
    }

    override suspend fun clearHistory(profileId: Long) {
        phraseHistoryDao.clearHistory(profileId)
    }

    override suspend fun exportHistory(profileId: Long): List<PhraseHistoryEntry> {
        return phraseHistoryDao.exportHistory(profileId).map { it.toDomain() }
    }
}

@Singleton
class PredictionRepositoryImpl @Inject constructor(
    private val predictionDao: PredictionDao
) : PredictionRepository {

    override suspend fun recordSequence(profileId: Long, buttonAId: Long, buttonBId: Long) {
        predictionDao.recordSequence(profileId, buttonAId, buttonBId)
    }

    override fun getPredictions(profileId: Long, lastButtonId: Long, limit: Int): Flow<List<AACButton>> {
        return predictionDao.getPredictions(profileId, lastButtonId, limit).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getMostUsedButtons(profileId: Long, limit: Int): Flow<List<AACButton>> {
        return predictionDao.getMostUsedButtons(profileId, limit).map { entities ->
            entities.map { it.toDomain() }
        }
    }
}
