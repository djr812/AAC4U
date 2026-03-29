package net.djrogers.aac4u.ui.grid.state

import net.djrogers.aac4u.domain.model.AACButton
import net.djrogers.aac4u.domain.model.Category

data class GridUiState(
    val categories: List<Category> = emptyList(),
    val currentCategory: Category? = null,
    val buttons: List<AACButton> = emptyList(),
    val coreButtons: List<AACButton> = emptyList(),
    val predictedButtons: List<AACButton> = emptyList(),

    // Sentence builder
    val sentenceParts: List<String> = emptyList(),
    val lastTappedButtonId: Long? = null,
    val selectedWordIndex: Int? = null,

    // Grid configuration
    val gridColumns: Int = 4,
    val showLabels: Boolean = true,

    // Accessibility
    val highContrastEnabled: Boolean = false,
    val largeTextEnabled: Boolean = false,
    val reducedAnimationsEnabled: Boolean = false,

    // TTS state
    val isSpeaking: Boolean = false,
    val isTtsReady: Boolean = false,

    // Word finder highlight
    val highlightedButtonId: Long? = null,
    val requestedCoreGroupIndex: Int? = null,

    // Scanning mode state (Phase 3)
    val scanningHighlightIndex: Int? = null,
    val isScanningActive: Boolean = false,

    // UI state
    val isLoading: Boolean = true,
    val isEditMode: Boolean = false,
    val error: String? = null
) {
    val fullSentence: String
        get() = sentenceParts.joinToString(" ").trim()

    val hasSentence: Boolean
        get() = sentenceParts.isNotEmpty()

    val hasSelectedWord: Boolean
        get() = selectedWordIndex != null && selectedWordIndex in sentenceParts.indices
}
