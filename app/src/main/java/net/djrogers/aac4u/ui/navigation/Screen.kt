package net.djrogers.aac4u.ui.navigation

/**
 * Sealed class defining all navigation routes in the app.
 * Single level of navigation — no nested sub-screens.
 */
sealed class Screen(val route: String) {
    data object Grid : Screen("grid")
    data object QuickPhrases : Screen("quick_phrases")
    data object Settings : Screen("settings")
    data object Profiles : Screen("profiles")
    data object History : Screen("history")
    data object Editor : Screen("editor/{categoryId}") {
        fun createRoute(categoryId: Long) = "editor/$categoryId"
    }
}
