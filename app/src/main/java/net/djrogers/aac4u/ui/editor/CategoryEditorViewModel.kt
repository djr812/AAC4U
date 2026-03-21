package net.djrogers.aac4u.ui.editor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import net.djrogers.aac4u.domain.model.Category
import net.djrogers.aac4u.domain.model.VocabularyType
import net.djrogers.aac4u.domain.repository.CategoryRepository
import net.djrogers.aac4u.domain.repository.ProfileRepository
import javax.inject.Inject

data class CategoryDialogState(
    val isVisible: Boolean = false,
    val isNewCategory: Boolean = true,
    val category: Category? = null,
    val editedName: String = "",
    val showDeleteConfirmation: Boolean = false
)

@HiltViewModel
class CategoryEditorViewModel @Inject constructor(
    private val categoryRepository: CategoryRepository,
    private val profileRepository: ProfileRepository
) : ViewModel() {

    private val _dialogState = MutableStateFlow(CategoryDialogState())
    val dialogState: StateFlow<CategoryDialogState> = _dialogState.asStateFlow()

    fun showAddDialog() {
        _dialogState.value = CategoryDialogState(
            isVisible = true,
            isNewCategory = true,
            editedName = ""
        )
    }

    fun showEditDialog(category: Category) {
        _dialogState.value = CategoryDialogState(
            isVisible = true,
            isNewCategory = false,
            category = category,
            editedName = category.name
        )
    }

    fun updateName(name: String) {
        _dialogState.update { it.copy(editedName = name) }
    }

    fun showDeleteConfirmation() {
        _dialogState.update { it.copy(showDeleteConfirmation = true) }
    }

    fun hideDeleteConfirmation() {
        _dialogState.update { it.copy(showDeleteConfirmation = false) }
    }

    fun saveCategory() {
        val state = _dialogState.value
        val name = state.editedName.trim()
        if (name.isBlank()) return

        viewModelScope.launch {
            if (state.isNewCategory) {
                val profileId = profileRepository.getActiveProfile().first()?.id ?: return@launch

                // Get current categories to determine sort order
                val existing = categoryRepository.getCategoriesByProfile(profileId).first()
                val nextOrder = (existing.maxOfOrNull { it.sortOrder } ?: -1) + 1

                categoryRepository.insertCategory(
                    Category(
                        profileId = profileId,
                        name = name,
                        sortOrder = nextOrder,
                        isVisible = true,
                        vocabularyType = VocabularyType.FRINGE
                    )
                )
            } else {
                val category = state.category ?: return@launch
                categoryRepository.updateCategory(
                    category.copy(name = name)
                )
            }
            dismissDialog()
        }
    }

    fun toggleVisibility() {
        val state = _dialogState.value
        val category = state.category ?: return
        if (state.isNewCategory) return

        viewModelScope.launch {
            categoryRepository.updateCategory(
                category.copy(isVisible = !category.isVisible)
            )
            dismissDialog()
        }
    }

    fun deleteCategory() {
        val state = _dialogState.value
        val category = state.category ?: return
        if (state.isNewCategory) return

        viewModelScope.launch {
            categoryRepository.deleteCategory(category.id)
            dismissDialog()
        }
    }

    /**
     * Move a category earlier in the tab order.
     */
    fun moveCategoryUp(category: Category, allCategories: List<Category>) {
        val index = allCategories.indexOfFirst { it.id == category.id }
        if (index <= 0) return

        viewModelScope.launch {
            val prev = allCategories[index - 1]
            categoryRepository.updateCategory(category.copy(sortOrder = prev.sortOrder))
            categoryRepository.updateCategory(prev.copy(sortOrder = category.sortOrder))
        }
    }

    /**
     * Move a category later in the tab order.
     */
    fun moveCategoryDown(category: Category, allCategories: List<Category>) {
        val index = allCategories.indexOfFirst { it.id == category.id }
        if (index < 0 || index >= allCategories.size - 1) return

        viewModelScope.launch {
            val next = allCategories[index + 1]
            categoryRepository.updateCategory(category.copy(sortOrder = next.sortOrder))
            categoryRepository.updateCategory(next.copy(sortOrder = category.sortOrder))
        }
    }

    fun dismissDialog() {
        _dialogState.value = CategoryDialogState()
    }
}
