package net.djrogers.aac4u.ui.grid.state

import net.djrogers.aac4u.domain.model.AACButton
import net.djrogers.aac4u.domain.model.Category

/**
 * Represents the complete UI state for the main communication grid.
 * The GridScreen composable renders purely from this state — no side effects.
 */
data class GridUiState(
    // Active content
    val categories: List<Category> = emptyList(),
    val currentCategory: Category? = null,
    val buttons: List<AACButton> = emptyList(),
    val coreButtons: List<AACButton> = emptyList(),
    val predictedButtons: List<AACButton> = emptyList(),

    // Sentence builder
    val sentenceParts: List<String> = emptyList(),
    val lastTappedButtonId: Long? = null,

    // Grid configuration
    val gridColumns: Int = 4,
    val showLabels: Boolean = true,

    // TTS state
    val isSpeaking: Boolean = false,
    val isTtsReady: Boolean = false,

    // Scanning mode state (Phase 3)
    val scanningHighlightIndex: Int? = null,
    val isScanningActive: Boolean = false,

    // UI state
    val isLoading: Boolean = true,
    val isEditMode: Boolean = false,
    val error: String? = null
) {
    /**
     * The full sentence assembled from parts, ready for TTS.
     */
    val fullSentence: String
        get() = sentenceParts.joinToString(" ").trim()

    /**
     * Whether there's a sentence ready to speak.
     */
    val hasSentence: Boolean
        get() = sentenceParts.isNotEmpty()
}
