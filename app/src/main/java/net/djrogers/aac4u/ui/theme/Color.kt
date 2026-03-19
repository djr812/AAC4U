package net.djrogers.aac4u.ui.theme

import androidx.compose.ui.graphics.Color

// ── Blues (Primary) ──
val Blue100 = Color(0xFFBBDEFB)
val Blue300 = Color(0xFF64B5F6)
val Blue600 = Color(0xFF1E88E5)
val Blue800 = Color(0xFF1565C0)
val Blue900 = Color(0xFF0D47A1)

// ── Greens (Secondary) ──
val Green100 = Color(0xFFC8E6C9)
val Green300 = Color(0xFF81C784)
val Green600 = Color(0xFF43A047)
val Green800 = Color(0xFF2E7D32)
val Green900 = Color(0xFF1B5E20)

// ── Greys (Surfaces) ──
val Grey100 = Color(0xFFF5F5F5)
val Grey300 = Color(0xFFE0E0E0)
val Grey700 = Color(0xFF616161)
val Grey800 = Color(0xFF424242)
val Grey900 = Color(0xFF212121)

// ── Reds (Errors) ──
val Red300 = Color(0xFFE57373)
val Red600 = Color(0xFFE53935)
val Red900 = Color(0xFFB71C1C)

// ── High Contrast ──
val Black = Color(0xFF000000)
val White = Color(0xFFFFFFFF)
val Yellow = Color(0xFFFFFF00)

// ── Soft Pastel Category Colours ──
// Gentle, child-friendly pastels for button backgrounds.
// Each category gets its own colour so users build visual association
// between colour and meaning (e.g. "pink = feelings").
object AACColors {
    val Feelings    = Color(0xFFFFCDD2) // Soft pink
    val Actions     = Color(0xFFC8E6C9) // Soft green
    val FoodDrink   = Color(0xFFFFE0B2) // Soft peach/orange
    val People      = Color(0xFFF8BBD0) // Soft rose
    val Places      = Color(0xFFE1BEE7) // Soft lavender
    val Things      = Color(0xFFFFF9C4) // Soft yellow
    val Descriptions = Color(0xFFB2EBF2) // Soft cyan
    val Time        = Color(0xFFDCEDC8) // Soft lime
    val Questions   = Color(0xFFD7CCC8) // Soft warm grey
    val Social      = Color(0xFFBBDEFB) // Soft sky blue
    val QuickPhrases = Color(0xFFFFCCBC) // Soft coral

    // Core vocabulary gets a distinctive look — slightly blue-grey
    // to set it apart from the colourful fringe categories
    val Core        = Color(0xFFCFD8DC) // Blue-grey pastel

    // Pressed/selected state — slightly darker overlay
    val PressedOverlay = Color(0x22000000) // 13% black overlay

    /**
     * Look up category colour by name.
     * Falls back to a neutral grey if no match found.
     */
    fun forCategory(categoryName: String): Color {
        return when (categoryName.lowercase()) {
            "feelings" -> Feelings
            "actions" -> Actions
            "food & drink" -> FoodDrink
            "people" -> People
            "places" -> Places
            "things" -> Things
            "descriptions" -> Descriptions
            "time" -> Time
            "questions" -> Questions
            "social" -> Social
            "quick phrases" -> QuickPhrases
            "core" -> Core
            else -> Grey100
        }
    }

    /**
     * Get a slightly darker version of a colour for pressed states.
     */
    fun pressed(color: Color): Color {
        return color.copy(
            red = (color.red * 0.85f).coerceIn(0f, 1f),
            green = (color.green * 0.85f).coerceIn(0f, 1f),
            blue = (color.blue * 0.85f).coerceIn(0f, 1f)
        )
    }

    /**
     * Get appropriate text colour for a given background.
     * All our pastels are light, so we always use dark text.
     */
    fun textOn(backgroundColor: Color): Color {
        return Color(0xFF37474F) // Dark blue-grey — readable on all pastels
    }
}
