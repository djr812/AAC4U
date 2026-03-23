package net.djrogers.aac4u.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import net.djrogers.aac4u.domain.model.UserProfile
import net.djrogers.aac4u.domain.repository.ProfileRepository
import javax.inject.Inject

data class AccessibilityState(
    val highContrastEnabled: Boolean = false,
    val largeTextEnabled: Boolean = false,
    val reducedAnimationsEnabled: Boolean = false,
    val previewHighContrast: Boolean = false,
    val previewLargeText: Boolean = false,
    val previewReducedAnimations: Boolean = false,
    val hasUnsavedChanges: Boolean = false,
    val showSavedToast: Boolean = false,
    val isLoading: Boolean = true
)

@HiltViewModel
class AccessibilitySettingsViewModel @Inject constructor(
    private val profileRepository: ProfileRepository
) : ViewModel() {

    private val _state = MutableStateFlow(AccessibilityState())
    val state: StateFlow<AccessibilityState> = _state.asStateFlow()

    private var activeProfile: UserProfile? = null

    init {
        loadSettings()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            profileRepository.getActiveProfile().collect { profile ->
                if (profile != null) {
                    activeProfile = profile
                    _state.update {
                        it.copy(
                            highContrastEnabled = profile.highContrastEnabled,
                            largeTextEnabled = profile.largeTextEnabled,
                            reducedAnimationsEnabled = profile.reducedAnimationsEnabled,
                            previewHighContrast = profile.highContrastEnabled,
                            previewLargeText = profile.largeTextEnabled,
                            previewReducedAnimations = profile.reducedAnimationsEnabled,
                            isLoading = false
                        )
                    }
                }
            }
        }
    }

    fun setHighContrast(enabled: Boolean) {
        _state.update {
            it.copy(previewHighContrast = enabled, hasUnsavedChanges = hasChanges(enabled, it.previewLargeText, it.previewReducedAnimations))
        }
    }

    fun setLargeText(enabled: Boolean) {
        _state.update {
            it.copy(previewLargeText = enabled, hasUnsavedChanges = hasChanges(it.previewHighContrast, enabled, it.previewReducedAnimations))
        }
    }

    fun setReducedAnimations(enabled: Boolean) {
        _state.update {
            it.copy(previewReducedAnimations = enabled, hasUnsavedChanges = hasChanges(it.previewHighContrast, it.previewLargeText, enabled))
        }
    }

    fun saveSettings() {
        val profile = activeProfile ?: return
        val s = _state.value

        viewModelScope.launch {
            profileRepository.updateProfile(
                profile.copy(
                    highContrastEnabled = s.previewHighContrast,
                    largeTextEnabled = s.previewLargeText,
                    reducedAnimationsEnabled = s.previewReducedAnimations
                )
            )

            _state.update {
                it.copy(
                    highContrastEnabled = s.previewHighContrast,
                    largeTextEnabled = s.previewLargeText,
                    reducedAnimationsEnabled = s.previewReducedAnimations,
                    hasUnsavedChanges = false,
                    showSavedToast = true
                )
            }

            delay(2000)
            _state.update { it.copy(showSavedToast = false) }
        }
    }

    fun discardChanges() {
        _state.update {
            it.copy(
                previewHighContrast = it.highContrastEnabled,
                previewLargeText = it.largeTextEnabled,
                previewReducedAnimations = it.reducedAnimationsEnabled,
                hasUnsavedChanges = false
            )
        }
    }

    private fun hasChanges(hc: Boolean, lt: Boolean, ra: Boolean): Boolean {
        val s = _state.value
        return hc != s.highContrastEnabled || lt != s.largeTextEnabled || ra != s.reducedAnimationsEnabled
    }
}
