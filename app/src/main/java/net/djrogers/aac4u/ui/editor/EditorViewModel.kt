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
    @ApplicationContext private val appContext: Context,
    private val buttonRepository: ButtonRepository,
    private val categoryRepository: CategoryRepository,
    private val profileRepository: ProfileRepository,
    private val symbolManager: SymbolManager
) : ViewModel() {

    private val _editState = MutableStateFlow(EditDialogState())
    val editState: StateFlow<EditDialogState> = _editState.asStateFlow()

    private val mainHandler = Handler(Looper.getMainLooper())

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
            showDeleteConfirmation = false
        )
    }

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

    fun saveButton() {
        val state = _editState.value
        val button = state.button ?: return
        val label = state.editedLabel.trim()
        val phrase = state.editedPhrase.trim()

        if (label.isBlank()) return

        val finalPhrase = phrase.ifBlank { label }

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
                        backgroundColor = state.editedColor,
                        imagePath = symbolPath,
                        sortOrder = nextOrder
                    )
                )

                if (symbolPath == null) {
                    launchSymbolDownload(insertedId, label, finalPhrase, state.editedColor, button.categoryId, nextOrder)
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
                        backgroundColor = state.editedColor,
                        imagePath = symbolPath
                    )
                )

                if (labelChanged && symbolPath == null) {
                    launchSymbolDownload(button.id, label, finalPhrase, state.editedColor, button.categoryId, button.sortOrder)
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
            buttonRepository.updateButton(
                button.copy(isVisible = !button.isVisible)
            )
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