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
import net.djrogers.aac4u.domain.usecase.grid.EnglishSuffixEngine
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

    private var buttonsJob: Job? = null
    private var coreButtonsJob: Job? = null
    private var categoriesJob: Job? = null
    private var predictionsJob: Job? = null

    private var activeProfileId: Long? = null

    init {
        observeActiveProfile()
        observeTtsState()
    }

    private fun observeActiveProfile() {
        viewModelScope.launch {
            profileRepository.getActiveProfile().collect { profile ->
                if (profile != null) {
                    val profileChanged = activeProfileId != null && activeProfileId != profile.id
                    activeProfileId = profile.id

                    launch { tts.applyProfile(profile.ttsVoiceName, profile.ttsRate, profile.ttsPitch) }

                    if (profileChanged) {
                        resetGridState()
                    }

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

    private fun resetGridState() {
        buttonsJob?.cancel()
        coreButtonsJob?.cancel()
        categoriesJob?.cancel()
        predictionsJob?.cancel()

        buildSentenceUseCase.clear()

        _uiState.value = GridUiState(
            isLoading = true,
            isTtsReady = _uiState.value.isTtsReady
        )
    }

    private fun loadCategories(profileId: Long) {
        categoriesJob?.cancel()
        categoriesJob = viewModelScope.launch {
            categoryRepository.getCategoriesByProfile(profileId).collect { allCategories ->
                val fringeCategories = allCategories.filter {
                    it.vocabularyType == VocabularyType.FRINGE
                }

                _uiState.update { state ->
                    state.copy(
                        categories = fringeCategories,
                        isLoading = false
                    )
                }

                val currentCategory = _uiState.value.currentCategory
                val shouldReselect = currentCategory == null ||
                        fringeCategories.none { it.id == currentCategory.id }

                if (shouldReselect && fringeCategories.isNotEmpty()) {
                    selectCategory(fringeCategories.first())
                } else if (shouldReselect && fringeCategories.isEmpty()) {
                    buttonsJob?.cancel()
                    _uiState.update { state ->
                        state.copy(currentCategory = null, buttons = emptyList())
                    }
                }
            }
        }
    }

    private fun loadCoreButtons(profileId: Long) {
        coreButtonsJob?.cancel()
        coreButtonsJob = viewModelScope.launch {
            categoryRepository.getCategoriesByType(profileId, VocabularyType.CORE).collect { coreCategories ->
                val coreCategory = coreCategories.firstOrNull()
                if (coreCategory != null) {
                    buttonRepository.getButtonsByCategory(coreCategory.id).collect { buttons ->
                        _uiState.update { state ->
                            state.copy(coreButtons = buttons)
                        }
                    }
                } else {
                    _uiState.update { state ->
                        state.copy(coreButtons = emptyList())
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

    fun selectCategory(category: Category) {
        _uiState.update { it.copy(currentCategory = category, buttons = emptyList()) }

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
        val updatedParts = buildSentenceUseCase.addPart(button.phrase)
        _uiState.update { state ->
            state.copy(
                sentenceParts = updatedParts,
                lastTappedButtonId = button.id,
                predictedButtons = emptyList()
            )
        }

        viewModelScope.launch {
            val profileId = activeProfileId ?: return@launch
            selectButtonUseCase(
                button = button,
                previousButtonId = _uiState.value.lastTappedButtonId,
                profileId = profileId
            )
            updatePredictions(profileId, button.id)
        }
    }

    fun onPredictionAccepted(button: AACButton) {
        val updatedParts = buildSentenceUseCase.addPart(button.phrase)
        _uiState.update { state ->
            state.copy(
                sentenceParts = updatedParts,
                lastTappedButtonId = button.id,
                predictedButtons = emptyList()
            )
        }

        viewModelScope.launch {
            val profileId = activeProfileId ?: return@launch
            selectButtonUseCase(
                button = button,
                previousButtonId = _uiState.value.lastTappedButtonId,
                profileId = profileId
            )
            updatePredictions(profileId, button.id)
        }
    }

    /**
     * Apply a suffix to the last word in the sentence.
     * Uses the EnglishSuffixEngine for proper morphology.
     */
    fun applySuffix(suffixType: String) {
        val currentParts = _uiState.value.sentenceParts
        if (currentParts.isEmpty()) return

        val lastWord = currentParts.last()
        val modifiedWord = when (suffixType) {
            "s" -> EnglishSuffixEngine.addPlural(lastWord)
            "ed" -> EnglishSuffixEngine.addPastTense(lastWord)
            "ing" -> EnglishSuffixEngine.addPresentParticiple(lastWord)
            "er" -> EnglishSuffixEngine.addComparative(lastWord)
            "est" -> EnglishSuffixEngine.addSuperlative(lastWord)
            "nt" -> EnglishSuffixEngine.addNegation(lastWord)
            else -> lastWord
        }

        if (modifiedWord != lastWord) {
            val updatedParts = buildSentenceUseCase.replaceLastPart(modifiedWord)
            _uiState.update { state ->
                state.copy(sentenceParts = updatedParts)
            }
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
            state.copy(sentenceParts = updatedParts, predictedButtons = emptyList())
        }
    }

    fun clearSentence() {
        val updatedParts = buildSentenceUseCase.clear()
        _uiState.update { state ->
            state.copy(
                sentenceParts = updatedParts,
                lastTappedButtonId = null,
                predictedButtons = emptyList()
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
