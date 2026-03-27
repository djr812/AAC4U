package net.djrogers.aac4u.ui.editor

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import net.djrogers.aac4u.domain.model.Category
import net.djrogers.aac4u.domain.model.VocabularyType
import net.djrogers.aac4u.domain.repository.CategoryRepository
import net.djrogers.aac4u.domain.repository.ProfileRepository
import net.djrogers.aac4u.ui.theme.AACColors
import javax.inject.Inject

data class CategoryDialogState(
    val isVisible: Boolean = false,
    val isNewCategory: Boolean = true,
    val category: Category? = null,
    val editedName: String = "",
    val showDeleteConfirmation: Boolean = false,
    val selectedColorHex: String? = null,
    val availableColors: List<Pair<String, String>> = emptyList() // hex to label
)

@HiltViewModel
class CategoryEditorViewModel @Inject constructor(
    private val categoryRepository: CategoryRepository,
    private val profileRepository: ProfileRepository
) : ViewModel() {

    companion object {
        // Full palette of category colours (hex -> display name)
        val ALL_CATEGORY_COLORS = listOf(
            "#FFCDD2" to "Rose",
            "#F8BBD0" to "Pink",
            "#E1BEE7" to "Lavender",
            "#D1C4E9" to "Violet",
            "#C5CAE9" to "Indigo",
            "#BBDEFB" to "Sky Blue",
            "#B3E5FC" to "Light Blue",
            "#B2EBF2" to "Cyan",
            "#B2DFDB" to "Teal",
            "#C8E6C9" to "Green",
            "#DCEDC8" to "Lime",
            "#F0F4C3" to "Lemon",
            "#FFF9C4" to "Yellow",
            "#FFECB3" to "Amber",
            "#FFE0B2" to "Peach",
            "#FFCCBC" to "Coral",
            "#D7CCC8" to "Warm Grey",
            "#CFD8DC" to "Cool Grey",
            "#E0E0E0" to "Silver",
            "#BCAAA4" to "Mocha"
        )
    }

    private val _dialogState = MutableStateFlow(CategoryDialogState())
    val dialogState: StateFlow<CategoryDialogState> = _dialogState.asStateFlow()

    fun showAddDialog() {
        viewModelScope.launch {
            val profileId = profileRepository.getActiveProfile().first()?.id ?: return@launch
            val existingCategories = categoryRepository.getCategoriesByProfile(profileId).first()

            // Find colours already in use
            val usedColors = existingCategories.map { cat ->
                AACColors.forCategoryHex(cat.name)
            }.toSet()

            // Filter to unused colours and take up to 4
            val available = ALL_CATEGORY_COLORS.filter { (hex, _) ->
                hex.uppercase() !in usedColors.map { it.uppercase() }
            }.take(4)

            _dialogState.value = CategoryDialogState(
                isVisible = true,
                isNewCategory = true,
                editedName = "",
                availableColors = available,
                selectedColorHex = available.firstOrNull()?.first
            )
        }
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

    fun selectColor(hex: String) {
        _dialogState.update { it.copy(selectedColorHex = hex) }
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
                val existing = categoryRepository.getCategoriesByProfile(profileId).first()
                val nextOrder = (existing.maxOfOrNull { it.sortOrder } ?: -1) + 1

                // Register the chosen colour for this category name
                val colorHex = state.selectedColorHex
                if (colorHex != null) {
                    AACColors.registerCategoryColor(name, colorHex)
                }

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
                categoryRepository.updateCategory(category.copy(name = name))
            }
            dismissDialog()
        }
    }

    fun toggleVisibility() {
        val state = _dialogState.value
        val category = state.category ?: return
        if (state.isNewCategory) return

        viewModelScope.launch {
            categoryRepository.updateCategory(category.copy(isVisible = !category.isVisible))
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

    fun moveCategoryUp(category: Category, allCategories: List<Category>) {
        val index = allCategories.indexOfFirst { it.id == category.id }
        if (index <= 0) return
        viewModelScope.launch {
            val prev = allCategories[index - 1]
            categoryRepository.updateCategory(category.copy(sortOrder = prev.sortOrder))
            categoryRepository.updateCategory(prev.copy(sortOrder = category.sortOrder))
        }
    }

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
