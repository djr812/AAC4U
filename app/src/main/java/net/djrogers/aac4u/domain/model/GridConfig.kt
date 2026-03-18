package net.djrogers.aac4u.domain.model

/**
 * Grid layout configuration — controls how the communication grid appears.
 */
data class GridConfig(
    val columns: Int = 4,
    val rows: Int = 4,
    val buttonPaddingDp: Int = 4,
    val showLabels: Boolean = true,
    val labelPosition: LabelPosition = LabelPosition.BELOW
)

enum class LabelPosition {
    ABOVE, BELOW, HIDDEN
}

enum class InputMethod {
    TAP,        // Standard touch
    DWELL,      // Hover/hold to select
    SWITCH,     // External switch device
    SCANNING    // Auto-highlight sequential selection
}

enum class FeedbackMode {
    AUDITORY,   // Sound only
    VISUAL,     // Visual highlight only
    BOTH,       // Sound + visual
    NONE        // No feedback
}

enum class VocabularyType {
    CORE,   // High-frequency words — always visible (want, go, more, help, etc.)
    FRINGE  // Context-specific words — organised by category (food, places, feelings, etc.)
}
