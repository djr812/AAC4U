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

object AACColors {
    val Feelings    = Color(0xFFFFCDD2)
    val Actions     = Color(0xFFC8E6C9)
    val FoodDrink   = Color(0xFFFFE0B2)
    val People      = Color(0xFFF8BBD0)
    val Places      = Color(0xFFE1BEE7)
    val Things      = Color(0xFFFFF9C4)
    val Descriptions = Color(0xFFB2EBF2)
    val Time        = Color(0xFFDCEDC8)
    val Questions   = Color(0xFFD7CCC8)
    val Social      = Color(0xFFBBDEFB)
    val QuickPhrases = Color(0xFFFFCCBC)
    val Core        = Color(0xFFCFD8DC)
    val PressedOverlay = Color(0x22000000)

    // Built-in category name → hex mapping
    private val builtInCategoryHex = mapOf(
        "feelings" to "#FFCDD2",
        "actions" to "#C8E6C9",
        "food & drink" to "#FFE0B2",
        "people" to "#F8BBD0",
        "places" to "#E1BEE7",
        "things" to "#FFF9C4",
        "descriptions" to "#B2EBF2",
        "time" to "#DCEDC8",
        "questions" to "#D7CCC8",
        "social" to "#BBDEFB",
        "quick phrases" to "#FFCCBC",
        "core" to "#CFD8DC"
    )

    // Dynamically registered colours for user-created categories
    private val customCategoryColors = mutableMapOf<String, String>()

    /**
     * Register a colour for a user-created category.
     */
    fun registerCategoryColor(categoryName: String, hex: String) {
        customCategoryColors[categoryName.lowercase()] = hex
    }

    /**
     * Look up category colour by name.
     * Checks built-in categories first, then user-registered ones.
     */
    fun forCategory(categoryName: String): Color {
        val key = categoryName.lowercase()

        // Check built-in
        val builtIn = builtInCategoryHex[key]
        if (builtIn != null) {
            return try { Color(android.graphics.Color.parseColor(builtIn)) } catch (_: Exception) { Grey100 }
        }

        // Check user-registered
        val custom = customCategoryColors[key]
        if (custom != null) {
            return try { Color(android.graphics.Color.parseColor(custom)) } catch (_: Exception) { Grey100 }
        }

        return Grey100
    }

    /**
     * Get the hex string for a category's colour.
     * Used for checking which colours are already in use.
     */
    fun forCategoryHex(categoryName: String): String {
        val key = categoryName.lowercase()
        return builtInCategoryHex[key]
            ?: customCategoryColors[key]
            ?: "#F5F5F5"
    }

    fun pressed(color: Color): Color {
        return color.copy(
            red = (color.red * 0.85f).coerceIn(0f, 1f),
            green = (color.green * 0.85f).coerceIn(0f, 1f),
            blue = (color.blue * 0.85f).coerceIn(0f, 1f)
        )
    }

    fun textOn(backgroundColor: Color): Color {
        return Color(0xFF37474F)
    }
}
