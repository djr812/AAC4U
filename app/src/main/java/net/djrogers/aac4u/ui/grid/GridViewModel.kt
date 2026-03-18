package net.djrogers.aac4u.ui.grid

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import net.djrogers.aac4u.data.tts.AACTextToSpeech
import net.djrogers.aac4u.domain.model.AACButton
import net.djrogers.aac4u.domain.model.Category
import net.djrogers.aac4u.domain.model.VocabularyType
import net.djrogers.aac4u.domain.repository.CategoryRepository
import net.djrogers.aac4u.domain.repository.ProfileRepository
import net.djrogers.aac4u.domain.usecase.grid.BuildSentenceUseCase
import net.djrogers.aac4u.domain.usecase.grid.GetGridButtonsUseCase
import net.djrogers.aac4u.domain.usecase.grid.SelectButtonUseCase
import net.djrogers.aac4u.domain.usecase.prediction.GetPredictedButtonsUseCase
import net.djrogers.aac4u.domain.usecase.tts.SpeakPhraseUseCase
import net.djrogers.aac4u.ui.grid.state.GridUiState
import javax.inject.Inject

@HiltViewModel
class GridViewModel @Inject constructor(
    private val getGridButtonsUseCase: GetGridButtonsUseCase,
    private val selectButtonUseCase: SelectButtonUseCase,
    private val buildSentenceUseCase: BuildSentenceUseCase,
    private val speakPhraseUseCase: SpeakPhraseUseCase,
    private val getPredictedButtonsUseCase: GetPredictedButtonsUseCase,
    private val categoryRepository: CategoryRepository,
    private val profileRepository: ProfileRepository,
    private val tts: AACTextToSpeech
) : ViewModel() {

    private val _uiState = MutableStateFlow(GridUiState())
    val uiState: StateFlow<GridUiState> = _uiState.asStateFlow()

    init {
        observeActiveProfile()
        observeTtsState()
    }

    /**
     * Watch for active profile changes and load their categories/settings.
     */
    private fun observeActiveProfile() {
        viewModelScope.launch {
            profileRepository.getActiveProfile().collect { profile ->
                if (profile != null) {
                    // Apply profile TTS settings
                    tts.applyProfile(profile.ttsVoiceName, profile.ttsRate, profile.ttsPitch)

                    // Update grid config
                    _uiState.update { state ->
                        state.copy(
                            gridColumns = profile.gridConfig.columns,
                            showLabels = profile.gridConfig.showLabels,
                            isLoading = false
                        )
                    }

                    // Load categories for this profile
                    loadCategories(profile.id)
                } else {
                    _uiState.update { it.copy(isLoading = false, error = "No active profile") }
                }
            }
        }
    }

    /**
     * Load categories and auto-select the first fringe category.
     */
    private fun loadCategories(profileId: Long) {
        viewModelScope.launch {
            categoryRepository.getCategoriesByProfile(profileId).collect { categories ->
                val fringeCategories = categories.filter { it.vocabularyType == VocabularyType.FRINGE }
                val currentCategory = _uiState.value.currentCategory
                    ?: fringeCategories.firstOrNull()

                _uiState.update { state ->
                    state.copy(categories = fringeCategories)
                }

                // Select category if none selected
                if (currentCategory != null) {
                    selectCategory(currentCategory)
                }

                // Load core vocabulary buttons
                loadCoreButtons(profileId)
            }
        }
    }

    /**
     * Load core vocabulary buttons (always visible at top/bottom of screen).
     */
    private fun loadCoreButtons(profileId: Long) {
        viewModelScope.launch {
            categoryRepository.getCategoriesByType(profileId, VocabularyType.CORE).collect { coreCategories ->
                // Collect buttons from all core categories
                coreCategories.firstOrNull()?.let { coreCategory ->
                    getGridButtonsUseCase(coreCategory.id).collect { buttons ->
                        _uiState.update { state ->
                            state.copy(coreButtons = buttons)
                        }
                    }
                }
            }
        }
    }

    /**
     * Observe TTS speaking state for UI feedback.
     */
    private fun observeTtsState() {
        viewModelScope.launch {
            tts.isSpeaking.collect { speaking ->
                _uiState.update { it.copy(isSpeaking = speaking) }
            }
        }
        viewModelScope.launch {
            tts.isReady.collect { ready ->
                _uiState.update { it.copy(isTtsReady = ready) }
            }
        }
    }

    // ── User Actions ──

    /**
     * User tapped a category tab — load its buttons.
     */
    fun selectCategory(category: Category) {
        _uiState.update { it.copy(currentCategory = category) }

        viewModelScope.launch {
            getGridButtonsUseCase(category.id).collect { buttons ->
                _uiState.update { state ->
                    state.copy(buttons = buttons)
                }
            }
        }
    }

    /**
     * User tapped an AAC button — add to sentence and optionally speak.
     */
    fun onButtonTapped(button: AACButton) {
        viewModelScope.launch {
            val profileId = getCurrentProfileId() ?: return@launch

            // Record usage + prediction data
            selectButtonUseCase(
                button = button,
                previousButtonId = _uiState.value.lastTappedButtonId,
                profileId = profileId
            )

            // Add to sentence builder
            val updatedParts = buildSentenceUseCase.addPart(button.phrase)

            _uiState.update { state ->
                state.copy(
                    sentenceParts = updatedParts,
                    lastTappedButtonId = button.id
                )
            }

            // Update predictions based on this button
            updatePredictions(profileId, button.id)
        }
    }

    /**
     * User tapped the speak button — speak the full sentence.
     */
    fun speakSentence() {
        val sentence = _uiState.value.fullSentence
        if (sentence.isBlank()) return

        tts.speakPhrase(sentence)

        viewModelScope.launch {
            val profileId = getCurrentProfileId() ?: return@launch
            speakPhraseUseCase(sentence, profileId)
        }

        // Clear the sentence bar after speaking
        clearSentence()
    }

    /**
     * Speak a single button's phrase immediately (tap-to-speak mode).
     */
    fun speakButtonDirectly(button: AACButton) {
        tts.speakPhrase(button.phrase)

        viewModelScope.launch {
            val profileId = getCurrentProfileId() ?: return@launch
            speakPhraseUseCase(button.phrase, profileId)
            selectButtonUseCase(
                button = button,
                previousButtonId = _uiState.value.lastTappedButtonId,
                profileId = profileId
            )

            _uiState.update { it.copy(lastTappedButtonId = button.id) }
            updatePredictions(profileId, button.id)
        }
    }

    /**
     * Remove the last word from the sentence bar (backspace).
     */
    fun removeLastPart() {
        val updatedParts = buildSentenceUseCase.removeLastPart()
        _uiState.update { state ->
            state.copy(sentenceParts = updatedParts)
        }
    }

    /**
     * Clear the entire sentence bar.
     */
    fun clearSentence() {
        val updatedParts = buildSentenceUseCase.clear()
        _uiState.update { state ->
            state.copy(
                sentenceParts = updatedParts,
                lastTappedButtonId = null
            )
        }
    }

    /**
     * Stop TTS playback.
     */
    fun stopSpeaking() {
        tts.stop()
    }

    /**
     * Toggle edit mode on/off.
     */
    fun toggleEditMode() {
        _uiState.update { it.copy(isEditMode = !it.isEditMode) }
    }

    // ── Private helpers ──

    private fun updatePredictions(profileId: Long, lastButtonId: Long) {
        viewModelScope.launch {
            getPredictedButtonsUseCase(profileId, lastButtonId).collect { predictions ->
                _uiState.update { it.copy(predictedButtons = predictions) }
            }
        }
    }

    private suspend fun getCurrentProfileId(): Long? {
        return profileRepository.getActiveProfile().first()?.id
    }
}
