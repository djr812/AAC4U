package net.djrogers.aac4u.ui.grid

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import net.djrogers.aac4u.data.tts.AACTextToSpeech
import net.djrogers.aac4u.domain.model.AACButton
import net.djrogers.aac4u.domain.model.Category
import net.djrogers.aac4u.domain.model.VocabularyType
import net.djrogers.aac4u.domain.repository.ButtonRepository
import net.djrogers.aac4u.domain.repository.CategoryRepository
import net.djrogers.aac4u.domain.repository.ProfileRepository
import net.djrogers.aac4u.domain.usecase.grid.BuildSentenceUseCase
import net.djrogers.aac4u.domain.usecase.grid.SelectButtonUseCase
import net.djrogers.aac4u.domain.usecase.prediction.GetPredictedButtonsUseCase
import net.djrogers.aac4u.domain.usecase.tts.SpeakPhraseUseCase
import net.djrogers.aac4u.ui.grid.state.GridUiState
import javax.inject.Inject

@HiltViewModel
class GridViewModel @Inject constructor(
    private val selectButtonUseCase: SelectButtonUseCase,
    private val buildSentenceUseCase: BuildSentenceUseCase,
    private val speakPhraseUseCase: SpeakPhraseUseCase,
    private val getPredictedButtonsUseCase: GetPredictedButtonsUseCase,
    private val buttonRepository: ButtonRepository,
    private val categoryRepository: CategoryRepository,
    private val profileRepository: ProfileRepository,
    private val tts: AACTextToSpeech
) : ViewModel() {

    private val _uiState = MutableStateFlow(GridUiState())
    val uiState: StateFlow<GridUiState> = _uiState.asStateFlow()

    // Track active collection jobs so we can cancel and replace them
    private var buttonsJob: Job? = null
    private var coreButtonsJob: Job? = null
    private var predictionsJob: Job? = null

    // Track active profile ID
    private var activeProfileId: Long? = null

    init {
        observeActiveProfile()
        observeTtsState()
    }

    private fun observeActiveProfile() {
        viewModelScope.launch {
            profileRepository.getActiveProfile().collect { profile ->
                if (profile != null) {
                    activeProfileId = profile.id
                    tts.applyProfile(profile.ttsVoiceName, profile.ttsRate, profile.ttsPitch)

                    _uiState.update { state ->
                        state.copy(
                            gridColumns = profile.gridConfig.columns,
                            showLabels = profile.gridConfig.showLabels
                        )
                    }

                    loadCategories(profile.id)
                    loadCoreButtons(profile.id)
                } else {
                    _uiState.update { it.copy(isLoading = false, error = "No active profile") }
                }
            }
        }
    }

    private fun loadCategories(profileId: Long) {
        viewModelScope.launch {
            categoryRepository.getCategoriesByProfile(profileId).collect { allCategories ->
                val fringeCategories = allCategories.filter {
                    it.vocabularyType == VocabularyType.FRINGE
                }

                _uiState.update { state ->
                    state.copy(categories = fringeCategories)
                }

                // Auto-select first category if none selected
                if (_uiState.value.currentCategory == null && fringeCategories.isNotEmpty()) {
                    selectCategory(fringeCategories.first())
                }

                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    private fun loadCoreButtons(profileId: Long) {
        coreButtonsJob?.cancel()
        coreButtonsJob = viewModelScope.launch {
            categoryRepository.getCategoriesByType(profileId, VocabularyType.CORE).collect { coreCategories ->
                val coreCategory = coreCategories.firstOrNull() ?: return@collect
                buttonRepository.getButtonsByCategory(coreCategory.id).collect { buttons ->
                    _uiState.update { state ->
                        state.copy(coreButtons = buttons)
                    }
                }
            }
        }
    }

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

    fun selectCategory(category: Category) {
        _uiState.update { it.copy(currentCategory = category) }

        // Cancel previous button collection and start new one
        buttonsJob?.cancel()
        buttonsJob = viewModelScope.launch {
            buttonRepository.getButtonsByCategory(category.id).collect { buttons ->
                _uiState.update { state ->
                    state.copy(buttons = buttons)
                }
            }
        }
    }

    fun onButtonTapped(button: AACButton) {
        viewModelScope.launch {
            val profileId = activeProfileId ?: return@launch

            selectButtonUseCase(
                button = button,
                previousButtonId = _uiState.value.lastTappedButtonId,
                profileId = profileId
            )

            val updatedParts = buildSentenceUseCase.addPart(button.phrase)

            _uiState.update { state ->
                state.copy(
                    sentenceParts = updatedParts,
                    lastTappedButtonId = button.id
                )
            }

            updatePredictions(profileId, button.id)
        }
    }

    fun speakSentence() {
        val sentence = _uiState.value.fullSentence
        if (sentence.isBlank()) return

        tts.speakPhrase(sentence)

        viewModelScope.launch {
            val profileId = activeProfileId ?: return@launch
            speakPhraseUseCase(sentence, profileId)
        }

        clearSentence()
    }

    fun speakButtonDirectly(button: AACButton) {
        tts.speakPhrase(button.phrase)

        viewModelScope.launch {
            val profileId = activeProfileId ?: return@launch
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

    fun removeLastPart() {
        val updatedParts = buildSentenceUseCase.removeLastPart()
        _uiState.update { state ->
            state.copy(sentenceParts = updatedParts)
        }
    }

    fun clearSentence() {
        val updatedParts = buildSentenceUseCase.clear()
        _uiState.update { state ->
            state.copy(
                sentenceParts = updatedParts,
                lastTappedButtonId = null
            )
        }
    }

    fun stopSpeaking() {
        tts.stop()
    }

    fun toggleEditMode() {
        _uiState.update { it.copy(isEditMode = !it.isEditMode) }
    }

    private fun updatePredictions(profileId: Long, lastButtonId: Long) {
        predictionsJob?.cancel()
        predictionsJob = viewModelScope.launch {
            getPredictedButtonsUseCase(profileId, lastButtonId).collect { predictions ->
                _uiState.update { it.copy(predictedButtons = predictions) }
            }
        }
    }
}
