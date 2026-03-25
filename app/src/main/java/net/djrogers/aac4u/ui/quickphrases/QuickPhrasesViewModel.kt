package net.djrogers.aac4u.ui.quickphrases

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import net.djrogers.aac4u.data.symbol.SymbolManager
import net.djrogers.aac4u.data.tts.AACTextToSpeech
import net.djrogers.aac4u.domain.model.AACButton
import net.djrogers.aac4u.domain.repository.ButtonRepository
import net.djrogers.aac4u.domain.repository.CategoryRepository
import net.djrogers.aac4u.domain.repository.ProfileRepository
import net.djrogers.aac4u.domain.model.VocabularyType
import javax.inject.Inject

data class QuickPhraseGroup(
    val name: String,
    val emoji: String,
    val phrases: List<AACButton> = emptyList()
)

data class QuickPhrasesState(
    val groups: List<QuickPhraseGroup> = emptyList(),
    val expandedGroupIndex: Int = 0,
    val isLoading: Boolean = true,
    val speakingPhraseId: Long? = null,
    val quickPhrasesCategoryId: Long? = null,

    // Add/edit dialog
    val showAddDialog: Boolean = false,
    val editingPhrase: AACButton? = null,
    val editLabel: String = "",
    val editPhraseText: String = "",
    val editGroupName: String = "",

    // Delete confirmation
    val showDeleteConfirmation: Boolean = false,
    val deletingPhrase: AACButton? = null
)

@HiltViewModel
class QuickPhrasesViewModel @Inject constructor(
    @ApplicationContext private val appContext: Context,
    private val buttonRepository: ButtonRepository,
    private val categoryRepository: CategoryRepository,
    private val profileRepository: ProfileRepository,
    private val symbolManager: SymbolManager,
    private val tts: AACTextToSpeech
) : ViewModel() {

    companion object {
        val DEFAULT_GROUPS = listOf(
            "Needs" to "🆘",
            "Greetings" to "👋",
            "Feelings" to "😊",
            "Responses" to "💬",
            "Emergency" to "🚨",
            "Other" to "📋"
        )

        // Map phrases to their default groups based on content
        private val PHRASE_GROUP_MAP = mapOf(
            "I need help" to "Needs",
            "I'm hungry" to "Needs",
            "I'm thirsty" to "Needs",
            "I need the bathroom" to "Needs",
            "I don't feel well" to "Needs",
            "I'm in pain" to "Needs",
            "Can I have more?" to "Needs",
            "I want something else" to "Needs",
            "hello" to "Greetings",
            "goodbye" to "Greetings",
            "how are you?" to "Greetings",
            "good morning" to "Greetings",
            "good night" to "Greetings",
            "I'm happy" to "Feelings",
            "I'm tired" to "Feelings",
            "I'm sad" to "Feelings",
            "I'm scared" to "Feelings",
            "I'm excited" to "Feelings",
            "I don't understand" to "Responses",
            "Can you repeat that?" to "Responses",
            "I don't like that" to "Responses",
            "I'm finished" to "Responses",
            "yes please" to "Responses",
            "no thank you" to "Responses",
            "I want to go home" to "Emergency",
            "Leave me alone please" to "Emergency",
            "Call for help" to "Emergency",
            "I'm lost" to "Emergency"
        )
    }

    private val _state = MutableStateFlow(QuickPhrasesState())
    val state: StateFlow<QuickPhrasesState> = _state.asStateFlow()

    private val mainHandler = Handler(Looper.getMainLooper())

    init {
        loadQuickPhrases()
        observeTts()
    }

    private fun loadQuickPhrases() {
        viewModelScope.launch {
            val profile = profileRepository.getActiveProfile().first() ?: return@launch

            categoryRepository.getCategoriesByProfile(profile.id).collect { categories ->
                val quickCategory = categories.find { it.name == "Quick Phrases" }
                if (quickCategory != null) {
                    _state.update { it.copy(quickPhrasesCategoryId = quickCategory.id) }

                    buttonRepository.getButtonsByCategory(quickCategory.id).collect { buttons ->
                        val grouped = organiseIntoGroups(buttons)
                        _state.update { it.copy(groups = grouped, isLoading = false) }
                    }
                } else {
                    _state.update { it.copy(groups = createEmptyGroups(), isLoading = false) }
                }
            }
        }
    }

    private fun organiseIntoGroups(buttons: List<AACButton>): List<QuickPhraseGroup> {
        return DEFAULT_GROUPS.map { (groupName, emoji) ->
            val groupPhrases = buttons.filter { button ->
                val assignedGroup = button.backgroundColor ?: PHRASE_GROUP_MAP[button.label] ?: PHRASE_GROUP_MAP[button.label.lowercase()]
                (assignedGroup ?: "Other") == groupName
            }
            QuickPhraseGroup(name = groupName, emoji = emoji, phrases = groupPhrases)
        }
    }

    private fun createEmptyGroups(): List<QuickPhraseGroup> {
        return DEFAULT_GROUPS.map { (name, emoji) -> QuickPhraseGroup(name = name, emoji = emoji) }
    }

    private fun observeTts() {
        viewModelScope.launch {
            tts.isSpeaking.collect { speaking ->
                if (!speaking) _state.update { it.copy(speakingPhraseId = null) }
            }
        }
    }

    fun expandGroup(index: Int) {
        _state.update { it.copy(expandedGroupIndex = index) }
    }

    fun speakPhrase(button: AACButton) {
        _state.update { it.copy(speakingPhraseId = button.id) }
        tts.speakPhrase(button.phrase)
    }

    fun stopSpeaking() {
        tts.stop()
        _state.update { it.copy(speakingPhraseId = null) }
    }

    // ── Add/Edit Dialog ──

    fun showAddDialog(groupName: String) {
        _state.update {
            it.copy(
                showAddDialog = true,
                editingPhrase = null,
                editLabel = "",
                editPhraseText = "",
                editGroupName = groupName
            )
        }
    }

    fun showEditDialog(button: AACButton) {
        val groupName = button.backgroundColor ?: PHRASE_GROUP_MAP[button.label] ?: "Other"
        _state.update {
            it.copy(
                showAddDialog = true,
                editingPhrase = button,
                editLabel = button.label,
                editPhraseText = button.phrase,
                editGroupName = groupName
            )
        }
    }

    fun updateEditLabel(label: String) {
        _state.update { it.copy(editLabel = label) }
    }

    fun updateEditPhrase(phrase: String) {
        _state.update { it.copy(editPhraseText = phrase) }
    }

    fun updateEditGroup(groupName: String) {
        _state.update { it.copy(editGroupName = groupName) }
    }

    fun dismissDialog() {
        _state.update {
            it.copy(
                showAddDialog = false,
                editingPhrase = null,
                showDeleteConfirmation = false,
                deletingPhrase = null
            )
        }
    }

    fun savePhrase() {
        val s = _state.value
        val label = s.editLabel.trim()
        if (label.isBlank()) return

        val phrase = s.editPhraseText.trim().ifBlank { label }
        val categoryId = s.quickPhrasesCategoryId ?: return

        dismissDialog()

        viewModelScope.launch {
            if (s.editingPhrase != null) {
                // Update existing
                buttonRepository.updateButton(
                    s.editingPhrase.copy(
                        label = label,
                        phrase = phrase,
                        backgroundColor = s.editGroupName
                    )
                )
            } else {
                // Add new
                val symbolPath = symbolManager.getSymbolForWord(label)
                val nextOrder = buttonRepository.getButtonsByCategory(categoryId)
                    .first()
                    .let { buttons -> (buttons.maxOfOrNull { it.sortOrder } ?: -1) + 1 }

                buttonRepository.insertButton(
                    AACButton(
                        categoryId = categoryId,
                        label = label,
                        phrase = phrase,
                        imagePath = symbolPath,
                        sortOrder = nextOrder,
                        isQuickPhrase = true,
                        backgroundColor = s.editGroupName
                    )
                )
            }
        }
    }

    // ── Delete ──

    fun showDeleteConfirmation(button: AACButton) {
        _state.update { it.copy(showDeleteConfirmation = true, deletingPhrase = button) }
    }

    fun confirmDelete() {
        val button = _state.value.deletingPhrase ?: return
        dismissDialog()
        viewModelScope.launch {
            buttonRepository.deleteButton(button.id)
        }
    }
}
