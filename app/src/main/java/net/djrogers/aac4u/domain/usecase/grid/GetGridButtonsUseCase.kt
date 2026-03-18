package net.djrogers.aac4u.domain.usecase.grid

import kotlinx.coroutines.flow.Flow
import net.djrogers.aac4u.domain.model.AACButton
import net.djrogers.aac4u.domain.model.VocabularyType
import net.djrogers.aac4u.domain.repository.ButtonRepository
import net.djrogers.aac4u.domain.repository.CategoryRepository
import javax.inject.Inject

/**
 * Retrieves the buttons to display in the grid for a given category.
 * Filters out hidden buttons and respects sort order.
 */
class GetGridButtonsUseCase @Inject constructor(
    private val buttonRepository: ButtonRepository
) {
    operator fun invoke(categoryId: Long): Flow<List<AACButton>> {
        return buttonRepository.getButtonsByCategory(categoryId)
    }
}
