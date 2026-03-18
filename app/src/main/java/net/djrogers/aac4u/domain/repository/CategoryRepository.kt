package net.djrogers.aac4u.domain.repository

import kotlinx.coroutines.flow.Flow
import net.djrogers.aac4u.domain.model.Category
import net.djrogers.aac4u.domain.model.VocabularyType

interface CategoryRepository {
    fun getCategoriesByProfile(profileId: Long): Flow<List<Category>>
    fun getCategoriesByType(profileId: Long, type: VocabularyType): Flow<List<Category>>
    suspend fun getCategoryById(id: Long): Category?
    suspend fun insertCategory(category: Category): Long
    suspend fun updateCategory(category: Category)
    suspend fun deleteCategory(id: Long)
    suspend fun updateSortOrder(categories: List<Category>)
}
