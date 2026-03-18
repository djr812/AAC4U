package net.djrogers.aac4u.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import net.djrogers.aac4u.data.local.database.dao.CategoryDao
import net.djrogers.aac4u.data.local.database.dao.ProfileDao
import net.djrogers.aac4u.data.local.database.mapper.toDomain
import net.djrogers.aac4u.data.local.database.mapper.toEntity
import net.djrogers.aac4u.domain.model.Category
import net.djrogers.aac4u.domain.model.UserProfile
import net.djrogers.aac4u.domain.model.VocabularyType
import net.djrogers.aac4u.domain.repository.CategoryRepository
import net.djrogers.aac4u.domain.repository.ProfileRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CategoryRepositoryImpl @Inject constructor(
    private val categoryDao: CategoryDao
) : CategoryRepository {

    override fun getCategoriesByProfile(profileId: Long): Flow<List<Category>> {
        return categoryDao.getCategoriesByProfile(profileId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getCategoriesByType(profileId: Long, type: VocabularyType): Flow<List<Category>> {
        return categoryDao.getCategoriesByType(profileId, type.name).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getCategoryById(id: Long): Category? {
        return categoryDao.getCategoryById(id)?.toDomain()
    }

    override suspend fun insertCategory(category: Category): Long {
        return categoryDao.insertCategory(category.toEntity())
    }

    override suspend fun updateCategory(category: Category) {
        categoryDao.updateCategory(category.toEntity())
    }

    override suspend fun deleteCategory(id: Long) {
        categoryDao.deleteCategory(id)
    }

    override suspend fun updateSortOrder(categories: List<Category>) {
        categoryDao.updateCategories(categories.map { it.toEntity() })
    }
}

@Singleton
class ProfileRepositoryImpl @Inject constructor(
    private val profileDao: ProfileDao
) : ProfileRepository {

    override fun getAllProfiles(): Flow<List<UserProfile>> {
        return profileDao.getAllProfiles().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getActiveProfile(): Flow<UserProfile?> {
        return profileDao.getActiveProfile().map { it?.toDomain() }
    }

    override suspend fun getProfileById(id: Long): UserProfile? {
        return profileDao.getProfileById(id)?.toDomain()
    }

    override suspend fun insertProfile(profile: UserProfile): Long {
        return profileDao.insertProfile(profile.toEntity())
    }

    override suspend fun updateProfile(profile: UserProfile) {
        profileDao.updateProfile(profile.toEntity())
    }

    override suspend fun deleteProfile(id: Long) {
        profileDao.deleteProfile(id)
    }

    override suspend fun setActiveProfile(profileId: Long) {
        profileDao.setActiveProfile(profileId)
    }
}
