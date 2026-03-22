package net.djrogers.aac4u.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import net.djrogers.aac4u.data.local.database.dao.ButtonDao
import net.djrogers.aac4u.data.local.database.mapper.toDomain
import net.djrogers.aac4u.data.local.database.mapper.toEntity
import net.djrogers.aac4u.domain.model.AACButton
import net.djrogers.aac4u.domain.repository.ButtonRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ButtonRepositoryImpl @Inject constructor(
    private val buttonDao: ButtonDao
) : ButtonRepository {

    override fun getButtonsByCategory(categoryId: Long): Flow<List<AACButton>> {
        return buttonDao.getButtonsByCategory(categoryId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getButtonsByProfile(profileId: Long): Flow<List<AACButton>> {
        return buttonDao.getButtonsByProfile(profileId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getQuickPhraseButtons(profileId: Long): Flow<List<AACButton>> {
        return buttonDao.getQuickPhraseButtons(profileId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getButtonById(id: Long): AACButton? {
        return buttonDao.getButtonById(id)?.toDomain()
    }

    override suspend fun insertButton(button: AACButton): Long {
        return buttonDao.insertButton(button.toEntity())
    }

    override suspend fun updateButton(button: AACButton) {
        buttonDao.updateButton(button.toEntity())
    }

    override suspend fun deleteButton(id: Long) {
        buttonDao.deleteButton(id)
    }

    override suspend fun updateSortOrder(buttons: List<AACButton>) {
        buttonDao.updateButtons(buttons.map { it.toEntity() })
    }

    override suspend fun incrementUsageCount(buttonId: Long) {
        buttonDao.incrementUsageCount(buttonId)
    }

    override fun searchButtons(profileId: Long, query: String): Flow<List<AACButton>> {
        return buttonDao.searchButtons(profileId, query).map { entities ->
            entities.map { it.toDomain() }
        }
    }
}