package net.djrogers.aac4u.domain.usecase.editor

import net.djrogers.aac4u.domain.model.AACButton
import net.djrogers.aac4u.domain.repository.ButtonRepository
import javax.inject.Inject

/**
 * Updates a button's properties (label, phrase, image, colour, visibility).
 */
class UpdateButtonUseCase @Inject constructor(
    private val buttonRepository: ButtonRepository
) {
    suspend operator fun invoke(button: AACButton) {
        buttonRepository.updateButton(button)
    }
}

/**
 * Saves a new sort order after the user drags buttons around.
 */
class ReorderButtonsUseCase @Inject constructor(
    private val buttonRepository: ButtonRepository
) {
    suspend operator fun invoke(buttons: List<AACButton>) {
        val reordered = buttons.mapIndexed { index, button ->
            button.copy(sortOrder = index)
        }
        buttonRepository.updateSortOrder(reordered)
    }
}
