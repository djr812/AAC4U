package net.djrogers.aac4u.domain.usecase.profile

import net.djrogers.aac4u.domain.model.UserProfile
import net.djrogers.aac4u.domain.repository.ProfileRepository
import javax.inject.Inject

/**
 * Switches the active user profile.
 * Deactivates all profiles, then activates the selected one.
 */
class SwitchProfileUseCase @Inject constructor(
    private val profileRepository: ProfileRepository
) {
    suspend operator fun invoke(profileId: Long) {
        profileRepository.setActiveProfile(profileId)
    }
}
