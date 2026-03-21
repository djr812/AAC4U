package net.djrogers.aac4u.ui.editor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import net.djrogers.aac4u.domain.model.AACButton
import net.djrogers.aac4u.domain.model.Category
import net.djrogers.aac4u.domain.model.VocabularyType
import net.djrogers.aac4u.domain.repository.ButtonRepository
import net.djrogers.aac4u.domain.repository.CategoryRepository
import net.djrogers.aac4u.domain.repository.ProfileRepository
import javax.inject.Inject

data class CoreWordsState(
    val coreButtons: List<AACButton> = emptyList(),
    val coreCategoryId: Long? = null,
    val isLoading: Boolean = true
)

@HiltViewModel
class CoreWordsEditorViewModel @Inject constructor(
    private val buttonRepository: ButtonRepository,
    private val categoryRepository: CategoryRepository,
    private val profileRepository: ProfileRepository
) : ViewModel() {

    private val _state = MutableStateFlow(CoreWordsState())
    val state: StateFlow<CoreWordsState> = _state.asStateFlow()

    init {
        loadCoreWords()
    }

    private fun loadCoreWords() {
        viewModelScope.launch {
            val profile = profileRepository.getActiveProfile().first() ?: return@launch

            categoryRepository.getCategoriesByType(profile.id, VocabularyType.CORE).collect { coreCategories ->
                val coreCategory = coreCategories.firstOrNull()
                if (coreCategory != null) {
                    _state.update { it.copy(coreCategoryId = coreCategory.id) }

                    // Get ALL buttons including hidden ones
                    buttonRepository.getButtonsByCategory(coreCategory.id).collect { buttons ->
                        _state.update {
                            it.copy(
                                coreButtons = buttons,
                                isLoading = false
                            )
                        }
                    }
                } else {
                    _state.update {
                        it.copy(
                            coreButtons = emptyList(),
                            coreCategoryId = null,
                            isLoading = false
                        )
                    }
                }
            }
        }
    }

    /**
     * Create a core vocabulary category if one doesn't exist.
     */
    fun createCoreCategory() {
        viewModelScope.launch {
            val profile = profileRepository.getActiveProfile().first() ?: return@launch

            val categoryId = categoryRepository.insertCategory(
                Category(
                    profileId = profile.id,
                    name = "Core",
                    sortOrder = 0,
                    isVisible = true,
                    vocabularyType = VocabularyType.CORE
                )
            )

            _state.update { it.copy(coreCategoryId = categoryId) }
        }
    }
}
