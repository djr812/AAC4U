package net.djrogers.aac4u.domain.repository

import kotlinx.coroutines.flow.Flow
import net.djrogers.aac4u.domain.model.AACButton

/**
 * Contract for button data access.
 * Implemented by ButtonRepositoryImpl in the data layer.
 */
interface ButtonRepository {
    fun getButtonsByCategory(categoryId: Long): Flow<List<AACButton>>
    fun getQuickPhraseButtons(profileId: Long): Flow<List<AACButton>>
    suspend fun getButtonById(id: Long): AACButton?
    suspend fun insertButton(button: AACButton): Long
    suspend fun updateButton(button: AACButton)
    suspend fun deleteButton(id: Long)
    suspend fun updateSortOrder(buttons: List<AACButton>)
    suspend fun incrementUsageCount(buttonId: Long)
    fun searchButtons(profileId: Long, query: String): Flow<List<AACButton>>
}
