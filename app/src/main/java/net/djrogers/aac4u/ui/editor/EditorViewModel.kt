package net.djrogers.aac4u.ui.editor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import net.djrogers.aac4u.domain.model.AACButton
import net.djrogers.aac4u.domain.repository.ButtonRepository
import net.djrogers.aac4u.domain.repository.CategoryRepository
import net.djrogers.aac4u.domain.repository.ProfileRepository
import javax.inject.Inject

data class EditDialogState(
    val isVisible: Boolean = false,
    val button: AACButton? = null,
    val editedLabel: String = "",
    val editedPhrase: String = "",
    val editedColor: String? = null,
    val isNewButton: Boolean = false,
    val showDeleteConfirmation: Boolean = false
)

@HiltViewModel
class EditorViewModel @Inject constructor(
    private val buttonRepository: ButtonRepository,
    private val categoryRepository: CategoryRepository,
    private val profileRepository: ProfileRepository
) : ViewModel() {

    private val _editState = MutableStateFlow(EditDialogState())
    val editState: StateFlow<EditDialogState> = _editState.asStateFlow()

    /**
     * Open the edit dialog for an existing button.
     */
    fun editButton(button: AACButton) {
        _editState.value = EditDialogState(
            isVisible = true,
            button = button,
            editedLabel = button.label,
            editedPhrase = button.phrase,
            editedColor = button.backgroundColor,
            isNewButton = false,
            showDeleteConfirmation = false
        )
    }

    /**
     * Open the edit dialog for creating a new button in a category.
     */
    fun addNewButton(categoryId: Long) {
        _editState.value = EditDialogState(
            isVisible = true,
            button = AACButton(
                categoryId = categoryId,
                label = "",
                phrase = ""
            ),
            editedLabel = "",
            editedPhrase = "",
            editedColor = null,
            isNewButton = true,
            showDeleteConfirmation = false
        )
    }

    fun updateLabel(label: String) {
        _editState.update { it.copy(editedLabel = label) }
    }

    fun updatePhrase(phrase: String) {
        _editState.update { it.copy(editedPhrase = phrase) }
    }

    fun updateColor(color: String?) {
        _editState.update { it.copy(editedColor = color) }
    }

    fun showDeleteConfirmation() {
        _editState.update { it.copy(showDeleteConfirmation = true) }
    }

    fun hideDeleteConfirmation() {
        _editState.update { it.copy(showDeleteConfirmation = false) }
    }

    /**
     * Save the edited or new button.
     */
    fun saveButton() {
        val state = _editState.value
        val button = state.button ?: return
        val label = state.editedLabel.trim()
        val phrase = state.editedPhrase.trim()

        if (label.isBlank()) return

        // If phrase is empty, use the label as the phrase
        val finalPhrase = phrase.ifBlank { label }

        viewModelScope.launch {
            if (state.isNewButton) {
                // Get the next sort order
                buttonRepository.getButtonsByCategory(button.categoryId).first().let { buttons ->
                    val nextOrder = (buttons.maxOfOrNull { it.sortOrder } ?: -1) + 1
                    buttonRepository.insertButton(
                        button.copy(
                            label = label,
                            phrase = finalPhrase,
                            backgroundColor = state.editedColor,
                            sortOrder = nextOrder
                        )
                    )
                }
            } else {
                buttonRepository.updateButton(
                    button.copy(
                        label = label,
                        phrase = finalPhrase,
                        backgroundColor = state.editedColor
                    )
                )
            }
            dismissDialog()
        }
    }

    /**
     * Toggle button visibility (hide/show).
     */
    fun toggleVisibility() {
        val state = _editState.value
        val button = state.button ?: return
        if (state.isNewButton) return

        viewModelScope.launch {
            buttonRepository.updateButton(
                button.copy(isVisible = !button.isVisible)
            )
            dismissDialog()
        }
    }

    /**
     * Delete the button permanently.
     */
    fun deleteButton() {
        val state = _editState.value
        val button = state.button ?: return
        if (state.isNewButton) return

        viewModelScope.launch {
            buttonRepository.deleteButton(button.id)
            dismissDialog()
        }
    }

    /**
     * Reorder buttons — move a button from one position to another.
     */
    fun reorderButtons(buttons: List<AACButton>, fromIndex: Int, toIndex: Int) {
        if (fromIndex == toIndex) return

        val mutableList = buttons.toMutableList()
        val moved = mutableList.removeAt(fromIndex)
        mutableList.add(toIndex, moved)

        val reordered = mutableList.mapIndexed { index, button ->
            button.copy(sortOrder = index)
        }

        viewModelScope.launch {
            buttonRepository.updateSortOrder(reordered)
        }
    }

    fun dismissDialog() {
        _editState.value = EditDialogState()
    }
}
