package net.djrogers.aac4u.domain.repository

import kotlinx.coroutines.flow.Flow
import net.djrogers.aac4u.domain.model.UserProfile

interface ProfileRepository {
    fun getAllProfiles(): Flow<List<UserProfile>>
    fun getActiveProfile(): Flow<UserProfile?>
    suspend fun getProfileById(id: Long): UserProfile?
    suspend fun insertProfile(profile: UserProfile): Long
    suspend fun updateProfile(profile: UserProfile)
    suspend fun deleteProfile(id: Long)
    suspend fun setActiveProfile(profileId: Long)
}
