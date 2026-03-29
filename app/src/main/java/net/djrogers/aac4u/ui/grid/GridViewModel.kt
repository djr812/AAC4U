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
import net.djrogers.aac4u.ui.grid.components.CoreWordGroups
import net.djrogers.aac4u.ui.grid.components.WordFinderResult
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

    private val _wordFinderResults = MutableStateFlow<List<WordFinderResult>>(emptyList())
    val wordFinderResults: StateFlow<List<WordFinderResult>> = _wordFinderResults.asStateFlow()

    private val _wordFinderQuery = MutableStateFlow("")
    val wordFinderQuery: StateFlow<String> = _wordFinderQuery.asStateFlow()

    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()

    private var buttonsJob: Job? = null
    private var coreButtonsJob: Job? = null
    private var categoriesJob: Job? = null
    private var predictionsJob: Job? = null
    private var searchJob: Job? = null
    private var activeProfileId: Long? = null

    private var allCategories: List<Category> = emptyList()

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
                    if (profileChanged) resetGridState()
                    _uiState.update {
                        it.copy(
                            gridColumns = profile.gridConfig.columns,
                            showLabels = profile.gridConfig.showLabels,
                            highContrastEnabled = profile.highContrastEnabled,
                            largeTextEnabled = profile.largeTextEnabled,
                            reducedAnimationsEnabled = profile.reducedAnimationsEnabled
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
        buttonsJob?.cancel(); coreButtonsJob?.cancel(); categoriesJob?.cancel(); predictionsJob?.cancel()
        buildSentenceUseCase.clear()
        _uiState.value = GridUiState(isLoading = true, isTtsReady = _uiState.value.isTtsReady)
    }

    private fun loadCategories(profileId: Long) {
        categoriesJob?.cancel()
        categoriesJob = viewModelScope.launch {
            categoryRepository.getCategoriesByProfile(profileId).collect { categories ->
                allCategories = categories
                val fringeCategories = categories.filter { it.vocabularyType == VocabularyType.FRINGE }
                _uiState.update { it.copy(categories = fringeCategories, isLoading = false) }
                val currentCategory = _uiState.value.currentCategory
                val shouldReselect = currentCategory == null || fringeCategories.none { it.id == currentCategory.id }
                if (shouldReselect && fringeCategories.isNotEmpty()) selectCategory(fringeCategories.first())
                else if (shouldReselect && fringeCategories.isEmpty()) {
                    buttonsJob?.cancel()
                    _uiState.update { it.copy(currentCategory = null, buttons = emptyList()) }
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
                        _uiState.update { it.copy(coreButtons = buttons) }
                    }
                } else _uiState.update { it.copy(coreButtons = emptyList()) }
            }
        }
    }

    private fun observeTtsState() {
        viewModelScope.launch { tts.isSpeaking.collect { speaking -> _uiState.update { it.copy(isSpeaking = speaking) } } }
        viewModelScope.launch { tts.isReady.collect { ready -> _uiState.update { it.copy(isTtsReady = ready) } } }
    }

    fun selectCategory(category: Category) {
        _uiState.update { it.copy(currentCategory = category, buttons = emptyList(), highlightedButtonId = null) }
        buttonsJob?.cancel()
        buttonsJob = viewModelScope.launch {
            buttonRepository.getButtonsByCategory(category.id).collect { buttons ->
                _uiState.update { it.copy(buttons = buttons) }
            }
        }
    }

    // ═══════════════════════════════════════
    // WORD FINDER
    // ═══════════════════════════════════════

    fun updateWordFinderQuery(query: String) {
        _wordFinderQuery.value = query

        if (query.isBlank()) {
            _wordFinderResults.value = emptyList()
            _isSearching.value = false
            return
        }

        val profileId = activeProfileId ?: return
        _isSearching.value = true

        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            buttonRepository.searchButtons(profileId, query).collect { buttons ->
                val results = buttons.mapNotNull { button ->
                    val category = allCategories.find { it.id == button.categoryId }
                    if (category != null) {
                        val isCoreWord = category.vocabularyType == VocabularyType.CORE

                        // For core words, find the group name
                        val displayCategoryName = if (isCoreWord) {
                            val group = CoreWordGroups.groupForButton(button.label, button.backgroundColor)
                            group?.name ?: "Core"
                        } else {
                            category.name
                        }

                        WordFinderResult(
                            button = button,
                            categoryName = displayCategoryName,
                            categoryId = category.id,
                            isCoreWord = isCoreWord
                        )
                    } else null
                }
                _wordFinderResults.value = results
                _isSearching.value = false
            }
        }
    }

    fun clearWordFinder() {
        _wordFinderQuery.value = ""
        _wordFinderResults.value = emptyList()
        _isSearching.value = false
        searchJob?.cancel()
    }

    fun navigateToWord(result: WordFinderResult) {
        if (result.isCoreWord) {
            // Find which group index this core word belongs to
            val group = CoreWordGroups.groupForButton(result.button.label, result.button.backgroundColor)
            if (group != null) {
                val groupIndex = CoreWordGroups.ALL_GROUPS.indexOf(group)
                if (groupIndex >= 0) {
                    _uiState.update {
                        it.copy(
                            requestedCoreGroupIndex = groupIndex,
                            highlightedButtonId = result.button.id
                        )
                    }
                }
            }
        } else {
            // Fringe word — switch to category and highlight
            val category = allCategories.find { it.id == result.categoryId } ?: return
            _uiState.update { it.copy(highlightedButtonId = result.button.id) }
            selectCategory(category)
        }

        // Clear highlight after 3 seconds
        viewModelScope.launch {
            kotlinx.coroutines.delay(3000)
            _uiState.update { it.copy(highlightedButtonId = null) }
        }
    }

    /**
     * Called by GridScreen after consuming the requestedCoreGroupIndex.
     */
    fun clearCoreGroupRequest() {
        _uiState.update { it.copy(requestedCoreGroupIndex = null) }
    }

    // ═══════════════════════════════════════
    // SENTENCE WORD SELECTION
    // ═══════════════════════════════════════

    fun toggleWordSelection(index: Int) {
        val currentSelection = _uiState.value.selectedWordIndex
        _uiState.update { it.copy(selectedWordIndex = if (currentSelection == index) null else index) }
    }

    fun clearWordSelection() {
        _uiState.update { it.copy(selectedWordIndex = null) }
    }

    fun onButtonTapped(button: AACButton) {
        _uiState.update { it.copy(highlightedButtonId = null) }
        val selectedIndex = _uiState.value.selectedWordIndex
        if (selectedIndex != null && selectedIndex in _uiState.value.sentenceParts.indices) {
            val updatedParts = buildSentenceUseCase.replacePartAt(selectedIndex, button.phrase)
            _uiState.update { it.copy(sentenceParts = updatedParts, lastTappedButtonId = button.id, selectedWordIndex = null, predictedButtons = emptyList()) }
        } else {
            val updatedParts = buildSentenceUseCase.addPart(button.phrase)
            _uiState.update { it.copy(sentenceParts = updatedParts, lastTappedButtonId = button.id, selectedWordIndex = null, predictedButtons = emptyList()) }
        }
        viewModelScope.launch {
            val profileId = activeProfileId ?: return@launch
            selectButtonUseCase(button = button, previousButtonId = _uiState.value.lastTappedButtonId, profileId = profileId)
            updatePredictions(profileId, button.id)
        }
    }

    fun onPredictionAccepted(button: AACButton) {
        val updatedParts = buildSentenceUseCase.addPart(button.phrase)
        _uiState.update { it.copy(sentenceParts = updatedParts, lastTappedButtonId = button.id, selectedWordIndex = null, predictedButtons = emptyList()) }
        viewModelScope.launch {
            val profileId = activeProfileId ?: return@launch
            selectButtonUseCase(button = button, previousButtonId = _uiState.value.lastTappedButtonId, profileId = profileId)
            updatePredictions(profileId, button.id)
        }
    }

    fun addTypedWord(word: String) {
        val trimmed = word.trim()
        if (trimmed.isBlank()) return
        val selectedIndex = _uiState.value.selectedWordIndex
        if (selectedIndex != null && selectedIndex in _uiState.value.sentenceParts.indices) {
            val updatedParts = buildSentenceUseCase.replacePartAt(selectedIndex, trimmed)
            _uiState.update { it.copy(sentenceParts = updatedParts, selectedWordIndex = null, predictedButtons = emptyList()) }
        } else {
            val updatedParts = buildSentenceUseCase.addPart(trimmed)
            _uiState.update { it.copy(sentenceParts = updatedParts, selectedWordIndex = null, predictedButtons = emptyList()) }
        }
    }

    fun addTypedSentence(sentence: String) {
        val words = sentence.trim().split("\\s+".toRegex()).filter { it.isNotBlank() }
        if (words.isEmpty()) return
        val selectedIndex = _uiState.value.selectedWordIndex
        if (selectedIndex != null && selectedIndex in _uiState.value.sentenceParts.indices) {
            buildSentenceUseCase.replacePartAt(selectedIndex, words.first())
            for (word in words.drop(1)) buildSentenceUseCase.addPart(word)
            _uiState.update { it.copy(sentenceParts = buildSentenceUseCase.getCurrentParts(), selectedWordIndex = null, predictedButtons = emptyList()) }
        } else {
            var updatedParts = _uiState.value.sentenceParts
            for (word in words) updatedParts = buildSentenceUseCase.addPart(word)
            _uiState.update { it.copy(sentenceParts = updatedParts, selectedWordIndex = null, predictedButtons = emptyList()) }
        }
    }

    fun loadPhraseFromHistory(phrase: String) {
        buildSentenceUseCase.clear()
        val words = phrase.trim().split("\\s+".toRegex()).filter { it.isNotBlank() }
        var updatedParts = emptyList<String>()
        for (word in words) updatedParts = buildSentenceUseCase.addPart(word)
        _uiState.update { it.copy(sentenceParts = updatedParts, selectedWordIndex = null, lastTappedButtonId = null, predictedButtons = emptyList()) }
    }

    fun applySuffix(suffixType: String) {
        val currentParts = _uiState.value.sentenceParts
        if (currentParts.isEmpty()) return
        val targetIndex = _uiState.value.selectedWordIndex ?: (currentParts.size - 1)
        if (targetIndex !in currentParts.indices) return
        val targetWord = currentParts[targetIndex]
        val modifiedWord = when (suffixType) {
            "s" -> EnglishSuffixEngine.addPlural(targetWord)
            "ed" -> EnglishSuffixEngine.addPastTense(targetWord)
            "ing" -> EnglishSuffixEngine.addPresentParticiple(targetWord)
            "er" -> EnglishSuffixEngine.addComparative(targetWord)
            "est" -> EnglishSuffixEngine.addSuperlative(targetWord)
            "nt" -> EnglishSuffixEngine.addNegation(targetWord)
            else -> targetWord
        }
        if (modifiedWord != targetWord) {
            val updatedParts = buildSentenceUseCase.replacePartAt(targetIndex, modifiedWord)
            _uiState.update { it.copy(sentenceParts = updatedParts, selectedWordIndex = null) }
        }
    }

    fun speakSentence() {
        val sentence = _uiState.value.fullSentence
        if (sentence.isBlank()) return
        _uiState.update { it.copy(selectedWordIndex = null) }
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
            selectButtonUseCase(button = button, previousButtonId = _uiState.value.lastTappedButtonId, profileId = profileId)
            _uiState.update { it.copy(lastTappedButtonId = button.id) }
            updatePredictions(profileId, button.id)
        }
    }

    fun removeLastPart() {
        val selectedIndex = _uiState.value.selectedWordIndex
        if (selectedIndex != null && selectedIndex in _uiState.value.sentenceParts.indices) {
            val updatedParts = buildSentenceUseCase.removePartAt(selectedIndex)
            _uiState.update { it.copy(sentenceParts = updatedParts, selectedWordIndex = null, predictedButtons = emptyList()) }
        } else {
            val updatedParts = buildSentenceUseCase.removeLastPart()
            _uiState.update { it.copy(sentenceParts = updatedParts, predictedButtons = emptyList()) }
        }
    }

    fun clearSentence() {
        val updatedParts = buildSentenceUseCase.clear()
        _uiState.update { it.copy(sentenceParts = updatedParts, lastTappedButtonId = null, selectedWordIndex = null, predictedButtons = emptyList()) }
    }

    fun stopSpeaking() { tts.stop() }
    fun toggleEditMode() { _uiState.update { it.copy(isEditMode = !it.isEditMode) } }

    private fun updatePredictions(profileId: Long, lastButtonId: Long) {
        predictionsJob?.cancel()
        predictionsJob = viewModelScope.launch {
            getPredictedButtonsUseCase(profileId, lastButtonId).collect { predictions ->
                _uiState.update { it.copy(predictedButtons = predictions) }
            }
        }
    }
}
