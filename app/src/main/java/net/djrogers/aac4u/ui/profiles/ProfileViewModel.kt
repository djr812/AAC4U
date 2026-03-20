package net.djrogers.aac4u.ui.profiles

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import net.djrogers.aac4u.domain.model.AgeRange
import net.djrogers.aac4u.domain.model.UserProfile
import net.djrogers.aac4u.domain.repository.ButtonRepository
import net.djrogers.aac4u.domain.repository.CategoryRepository
import net.djrogers.aac4u.domain.repository.ProfileRepository
import javax.inject.Inject

data class ProfilesUiState(
    val profiles: List<UserProfile> = emptyList(),
    val activeProfileId: Long? = null,
    val isLoading: Boolean = true
)

data class ProfileDialogState(
    val isVisible: Boolean = false,
    val isNewProfile: Boolean = true,
    val editingProfile: UserProfile? = null,
    val name: String = "",
    val avatar: String = "😊",
    val ageRange: AgeRange = AgeRange.ADULT,
    val showDeleteConfirmation: Boolean = false
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val profileRepository: ProfileRepository,
    private val categoryRepository: CategoryRepository,
    private val buttonRepository: ButtonRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfilesUiState())
    val uiState: StateFlow<ProfilesUiState> = _uiState.asStateFlow()

    private val _dialogState = MutableStateFlow(ProfileDialogState())
    val dialogState: StateFlow<ProfileDialogState> = _dialogState.asStateFlow()

    init {
        loadProfiles()
    }

    private fun loadProfiles() {
        viewModelScope.launch {
            profileRepository.getAllProfiles().collect { profiles ->
                val activeId = profiles.find { it.isActive }?.id
                _uiState.value = ProfilesUiState(
                    profiles = profiles,
                    activeProfileId = activeId,
                    isLoading = false
                )
            }
        }
    }

    fun switchProfile(profileId: Long) {
        viewModelScope.launch {
            profileRepository.setActiveProfile(profileId)
        }
    }

    // ── Dialog actions ──

    fun showCreateDialog() {
        _dialogState.value = ProfileDialogState(
            isVisible = true,
            isNewProfile = true,
            name = "",
            avatar = "😊",
            ageRange = AgeRange.ADULT
        )
    }

    fun showEditDialog(profile: UserProfile) {
        _dialogState.value = ProfileDialogState(
            isVisible = true,
            isNewProfile = false,
            editingProfile = profile,
            name = profile.name,
            avatar = profile.avatar,
            ageRange = profile.ageRange
        )
    }

    fun updateName(name: String) {
        _dialogState.update { it.copy(name = name) }
    }

    fun updateAvatar(avatar: String) {
        _dialogState.update { it.copy(avatar = avatar) }
    }

    fun updateAgeRange(ageRange: AgeRange) {
        _dialogState.update { it.copy(ageRange = ageRange) }
    }

    fun showDeleteConfirmation() {
        _dialogState.update { it.copy(showDeleteConfirmation = true) }
    }

    fun hideDeleteConfirmation() {
        _dialogState.update { it.copy(showDeleteConfirmation = false) }
    }

    fun saveProfile() {
        val state = _dialogState.value
        val name = state.name.trim()
        if (name.isBlank()) return

        viewModelScope.launch {
            if (state.isNewProfile) {
                // Create the new profile
                val newProfileId = profileRepository.insertProfile(
                    UserProfile(
                        name = name,
                        avatar = state.avatar,
                        ageRange = state.ageRange,
                        isActive = false
                    )
                )

                // Copy vocabulary from the default (first) profile
                copyVocabularyFromDefault(newProfileId)
            } else {
                val existing = state.editingProfile ?: return@launch
                profileRepository.updateProfile(
                    existing.copy(
                        name = name,
                        avatar = state.avatar,
                        ageRange = state.ageRange
                    )
                )
            }
            dismissDialog()
        }
    }

    /**
     * Copies all categories and buttons from the first profile (Default)
     * into the new profile. Each category and button gets a new ID,
     * so edits to the copy don't affect the original.
     */
    private suspend fun copyVocabularyFromDefault(newProfileId: Long) {
        // Find the default profile (first one created, or first in the list)
        val allProfiles = _uiState.value.profiles
        val sourceProfile = allProfiles.firstOrNull() ?: return

        // Get all categories for the source profile
        val sourceCategories = categoryRepository
            .getCategoriesByProfile(sourceProfile.id)
            .first()

        // For each category, create a copy and copy its buttons
        for (sourceCategory in sourceCategories) {
            val newCategoryId = categoryRepository.insertCategory(
                sourceCategory.copy(
                    id = 0, // Room will auto-generate a new ID
                    profileId = newProfileId
                )
            )

            // Get all buttons in this category (including hidden ones)
            val sourceButtons = buttonRepository
                .getButtonsByCategory(sourceCategory.id)
                .first()

            // Copy each button into the new category
            for (button in sourceButtons) {
                buttonRepository.insertButton(
                    button.copy(
                        id = 0, // Room will auto-generate a new ID
                        categoryId = newCategoryId
                    )
                )
            }
        }
    }

    fun deleteProfile() {
        val state = _dialogState.value
        val profile = state.editingProfile ?: return

        viewModelScope.launch {
            val allProfiles = _uiState.value.profiles
            if (allProfiles.size <= 1) {
                hideDeleteConfirmation()
                return@launch
            }

            if (profile.isActive) {
                val nextProfile = allProfiles.first { it.id != profile.id }
                profileRepository.setActiveProfile(nextProfile.id)
            }

            profileRepository.deleteProfile(profile.id)
            dismissDialog()
        }
    }

    fun dismissDialog() {
        _dialogState.value = ProfileDialogState()
    }
}