package net.djrogers.aac4u.ui.editor

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import net.djrogers.aac4u.domain.model.AACButton
import net.djrogers.aac4u.domain.repository.ButtonRepository
import net.djrogers.aac4u.domain.repository.CategoryRepository
import net.djrogers.aac4u.domain.repository.ProfileRepository
import net.djrogers.aac4u.data.symbol.SymbolManager
import net.djrogers.aac4u.ui.grid.components.CoreWordGroups
import javax.inject.Inject

data class EditDialogState(
    val isVisible: Boolean = false,
    val button: AACButton? = null,
    val editedLabel: String = "",
    val editedPhrase: String = "",
    val editedColor: String? = null,
    val isNewButton: Boolean = false,
    val showDeleteConfirmation: Boolean = false,
    val isCoreWord: Boolean = false,
    val selectedWordType: String? = null,
    val duplicateWarning: String? = null
)

@HiltViewModel
class EditorViewModel @Inject constructor(
    @ApplicationContext private val appContext: Context,
    private val buttonRepository: ButtonRepository,
    private val categoryRepository: CategoryRepository,
    private val profileRepository: ProfileRepository,
    private val symbolManager: SymbolManager
) : ViewModel() {

    private val _editState = MutableStateFlow(EditDialogState())
    val editState: StateFlow<EditDialogState> = _editState.asStateFlow()

    private val mainHandler = Handler(Looper.getMainLooper())

    // Cache of current core buttons for duplicate checking
    private var currentCoreButtons: List<AACButton> = emptyList()

    private fun showSystemToast(message: String) {
        mainHandler.post {
            Toast.makeText(appContext, message, Toast.LENGTH_SHORT).show()
        }
    }

    fun editButton(button: AACButton) {
        _editState.value = EditDialogState(
            isVisible = true,
            button = button,
            editedLabel = button.label,
            editedPhrase = button.phrase,
            editedColor = button.backgroundColor,
            isNewButton = false,
            showDeleteConfirmation = false,
            isCoreWord = false,
            selectedWordType = null,
            duplicateWarning = null
        )
    }

    fun addNewButton(categoryId: Long) {
        _editState.value = EditDialogState(
            isVisible = true,
            button = AACButton(categoryId = categoryId, label = "", phrase = ""),
            editedLabel = "",
            editedPhrase = "",
            editedColor = null,
            isNewButton = true,
            showDeleteConfirmation = false,
            isCoreWord = false,
            selectedWordType = null,
            duplicateWarning = null
        )
    }

    fun addNewCoreWord(categoryId: Long) {
        // Load current core buttons for duplicate checking
        viewModelScope.launch {
            currentCoreButtons = buttonRepository.getButtonsByCategory(categoryId).first()

            _editState.value = EditDialogState(
                isVisible = true,
                button = AACButton(categoryId = categoryId, label = "", phrase = ""),
                editedLabel = "",
                editedPhrase = "",
                editedColor = null,
                isNewButton = true,
                showDeleteConfirmation = false,
                isCoreWord = true,
                selectedWordType = null,
                duplicateWarning = null
            )
        }
    }

    fun updateLabel(label: String) {
        val warning = if (_editState.value.isCoreWord && _editState.value.isNewButton) {
            checkDuplicate(label.trim())
        } else null

        _editState.update { it.copy(editedLabel = label, duplicateWarning = warning) }
    }

    fun updatePhrase(phrase: String) {
        _editState.update { it.copy(editedPhrase = phrase) }
    }

    fun updateColor(color: String?) {
        _editState.update { it.copy(editedColor = color) }
    }

    fun updateWordType(wordType: String?) {
        _editState.update { it.copy(selectedWordType = wordType) }
    }

    fun showDeleteConfirmation() {
        _editState.update { it.copy(showDeleteConfirmation = true) }
    }

    fun hideDeleteConfirmation() {
        _editState.update { it.copy(showDeleteConfirmation = false) }
    }

    /**
     * Check if a word already exists as a core word.
     * Returns a user-friendly message if found, null if not a duplicate.
     */
    private fun checkDuplicate(word: String): String? {
        if (word.isBlank()) return null

        val existing = currentCoreButtons.find {
            it.label.equals(word, ignoreCase = true)
        }

        if (existing != null) {
            // Find which group it belongs to
            val group = CoreWordGroups.groupForButton(existing.label, existing.backgroundColor)

            return if (group != null) {
                val triggerWord = group.words.firstOrNull() ?: ""
                "\"${existing.label}\" already exists — it's in the ${group.name} group (under \"$triggerWord\")"
            } else {
                "\"${existing.label}\" already exists as a core word"
            }
        }

        return null
    }

    fun saveButton() {
        val state = _editState.value
        val button = state.button ?: return
        val label = state.editedLabel.trim()
        val phrase = state.editedPhrase.trim()

        if (label.isBlank()) return

        // Block save if duplicate core word
        if (state.isCoreWord && state.isNewButton && state.duplicateWarning != null) return

        // For core words, require a word type
        if (state.isCoreWord && state.isNewButton && state.selectedWordType == null) return

        val finalPhrase = phrase.ifBlank { label }

        val finalColor = if (state.isCoreWord && state.selectedWordType != null) {
            CoreWordTypeColors.getHexColor(state.selectedWordType)
        } else {
            state.editedColor
        }

        dismissDialog()

        viewModelScope.launch {
            if (state.isNewButton) {
                val symbolPath = symbolManager.getSymbolForWord(label)

                val nextOrder = buttonRepository.getButtonsByCategory(button.categoryId)
                    .first()
                    .let { buttons -> (buttons.maxOfOrNull { it.sortOrder } ?: -1) + 1 }

                val insertedId = buttonRepository.insertButton(
                    button.copy(
                        label = label,
                        phrase = finalPhrase,
                        backgroundColor = finalColor,
                        imagePath = symbolPath,
                        sortOrder = nextOrder
                    )
                )

                if (symbolPath == null) {
                    launchSymbolDownload(insertedId, label, finalPhrase, finalColor, button.categoryId, nextOrder)
                }
            } else {
                val labelChanged = label != button.label
                var symbolPath = button.imagePath

                if (labelChanged) {
                    symbolPath = symbolManager.getSymbolForWord(label)
                }

                buttonRepository.updateButton(
                    button.copy(
                        label = label,
                        phrase = finalPhrase,
                        backgroundColor = finalColor,
                        imagePath = symbolPath
                    )
                )

                if (labelChanged && symbolPath == null) {
                    launchSymbolDownload(button.id, label, finalPhrase, finalColor, button.categoryId, button.sortOrder)
                }
            }
        }
    }

    private fun launchSymbolDownload(
        buttonId: Long,
        label: String,
        phrase: String,
        backgroundColor: String?,
        categoryId: Long,
        sortOrder: Int
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val downloadedPath = symbolManager.searchAndDownloadSymbol(label)
                if (downloadedPath != null) {
                    buttonRepository.updateButton(
                        AACButton(
                            id = buttonId,
                            categoryId = categoryId,
                            label = label,
                            phrase = phrase,
                            imagePath = downloadedPath,
                            backgroundColor = backgroundColor,
                            sortOrder = sortOrder
                        )
                    )
                    showSystemToast("✓ Symbol found for \"$label\"")
                } else {
                    showSystemToast("No symbol available for \"$label\"")
                }
            } catch (e: Exception) {
                showSystemToast("Could not download symbol — check internet")
            }
        }
    }

    fun toggleVisibility() {
        val state = _editState.value
        val button = state.button ?: return
        if (state.isNewButton) return

        viewModelScope.launch {
            buttonRepository.updateButton(button.copy(isVisible = !button.isVisible))
            dismissDialog()
        }
    }

    fun deleteButton() {
        val state = _editState.value
        val button = state.button ?: return
        if (state.isNewButton) return

        viewModelScope.launch {
            buttonRepository.deleteButton(button.id)
            dismissDialog()
        }
    }

    fun reorderButtons(buttons: List<AACButton>, fromIndex: Int, toIndex: Int) {
        if (fromIndex == toIndex) return
        val mutableList = buttons.toMutableList()
        val moved = mutableList.removeAt(fromIndex)
        mutableList.add(toIndex, moved)
        val reordered = mutableList.mapIndexed { index, button -> button.copy(sortOrder = index) }
        viewModelScope.launch { buttonRepository.updateSortOrder(reordered) }
    }

    fun dismissDialog() {
        _editState.value = EditDialogState()
    }
}

object CoreWordTypeColors {
    private val typeToHex = mapOf(
        "Pronoun" to "#BBDEFB",
        "Verb" to "#C8E6C9",
        "Adjective" to "#FFF9C4",
        "Helper" to "#E1BEE7",
        "Preposition" to "#B2EBF2",
        "Question" to "#E0E0E0",
        "Social" to "#FFCDD2",
        "Determiner" to "#FFE0B2",
        "Adverb" to "#FFCCBC",
        "Conjunction" to "#DCEDC8"
    )

    val allTypes = typeToHex.keys.toList()

    fun getHexColor(type: String?): String? = typeToHex[type]
}
