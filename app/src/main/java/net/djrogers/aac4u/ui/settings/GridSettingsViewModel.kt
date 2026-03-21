package net.djrogers.aac4u.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import net.djrogers.aac4u.domain.model.GridConfig
import net.djrogers.aac4u.domain.model.LabelPosition
import net.djrogers.aac4u.domain.model.UserProfile
import net.djrogers.aac4u.domain.repository.ProfileRepository
import javax.inject.Inject

data class GridSettingsState(
    // Saved values
    val savedColumns: Int = 4,
    val savedLabelPosition: LabelPosition = LabelPosition.BELOW,
    val savedOrientationLock: OrientationLock = OrientationLock.NONE,

    // Preview values
    val previewColumns: Int = 4,
    val previewLabelPosition: LabelPosition = LabelPosition.BELOW,
    val previewOrientationLock: OrientationLock = OrientationLock.NONE,

    // UI state
    val hasUnsavedChanges: Boolean = false,
    val showSavedToast: Boolean = false,
    val isLoading: Boolean = true
)

enum class OrientationLock(val label: String, val description: String) {
    NONE("Auto", "Follows device orientation"),
    PORTRAIT("Portrait", "Always vertical"),
    LANDSCAPE("Landscape", "Always horizontal")
}

@HiltViewModel
class GridSettingsViewModel @Inject constructor(
    private val profileRepository: ProfileRepository
) : ViewModel() {

    private val _state = MutableStateFlow(GridSettingsState())
    val state: StateFlow<GridSettingsState> = _state.asStateFlow()

    private var activeProfile: UserProfile? = null

    init {
        loadSettings()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            profileRepository.getActiveProfile().collect { profile ->
                if (profile != null) {
                    activeProfile = profile

                    val orientationLock = when (profile.gridConfig.rows) {
                        -1 -> OrientationLock.PORTRAIT
                        -2 -> OrientationLock.LANDSCAPE
                        else -> OrientationLock.NONE
                    }

                    _state.update {
                        it.copy(
                            savedColumns = profile.gridConfig.columns,
                            savedLabelPosition = profile.gridConfig.labelPosition,
                            savedOrientationLock = orientationLock,
                            previewColumns = profile.gridConfig.columns,
                            previewLabelPosition = profile.gridConfig.labelPosition,
                            previewOrientationLock = orientationLock,
                            isLoading = false
                        )
                    }
                }
            }
        }
    }

    fun setPreviewColumns(columns: Int) {
        val clamped = columns.coerceIn(2, 10)
        _state.update { state ->
            state.copy(
                previewColumns = clamped,
                hasUnsavedChanges = hasChanges(clamped, state.previewLabelPosition, state.previewOrientationLock)
            )
        }
    }

    fun setPreviewLabelPosition(position: LabelPosition) {
        _state.update { state ->
            state.copy(
                previewLabelPosition = position,
                hasUnsavedChanges = hasChanges(state.previewColumns, position, state.previewOrientationLock)
            )
        }
    }

    fun setPreviewOrientationLock(lock: OrientationLock) {
        _state.update { state ->
            state.copy(
                previewOrientationLock = lock,
                hasUnsavedChanges = hasChanges(state.previewColumns, state.previewLabelPosition, lock)
            )
        }
    }

    fun saveSettings() {
        val profile = activeProfile ?: return
        val currentState = _state.value

        val orientationRows = when (currentState.previewOrientationLock) {
            OrientationLock.PORTRAIT -> -1
            OrientationLock.LANDSCAPE -> -2
            OrientationLock.NONE -> profile.gridConfig.rows
        }

        viewModelScope.launch {
            val updatedProfile = profile.copy(
                gridConfig = profile.gridConfig.copy(
                    columns = currentState.previewColumns,
                    labelPosition = currentState.previewLabelPosition,
                    showLabels = currentState.previewLabelPosition != LabelPosition.HIDDEN,
                    rows = orientationRows
                )
            )

            profileRepository.updateProfile(updatedProfile)
            activeProfile = updatedProfile

            _state.update { state ->
                state.copy(
                    savedColumns = currentState.previewColumns,
                    savedLabelPosition = currentState.previewLabelPosition,
                    savedOrientationLock = currentState.previewOrientationLock,
                    hasUnsavedChanges = false,
                    showSavedToast = true
                )
            }

            delay(2000)
            _state.update { it.copy(showSavedToast = false) }
        }
    }

    fun discardChanges() {
        _state.update { state ->
            state.copy(
                previewColumns = state.savedColumns,
                previewLabelPosition = state.savedLabelPosition,
                previewOrientationLock = state.savedOrientationLock,
                hasUnsavedChanges = false,
                showSavedToast = false
            )
        }
    }

    private fun hasChanges(columns: Int, labelPosition: LabelPosition, orientationLock: OrientationLock): Boolean {
        val currentState = _state.value
        return columns != currentState.savedColumns ||
                labelPosition != currentState.savedLabelPosition ||
                orientationLock != currentState.savedOrientationLock
    }
}
