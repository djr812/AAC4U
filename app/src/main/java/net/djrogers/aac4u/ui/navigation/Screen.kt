package net.djrogers.aac4u.ui.navigation

sealed class Screen(val route: String) {
    data object Grid : Screen("grid")
    data object QuickPhrases : Screen("quick_phrases")
    data object Settings : Screen("settings")
    data object Profiles : Screen("profiles")
    data object History : Screen("history")
    data object About : Screen("about")
    data object CoreWordsEditor : Screen("core_words_editor")
    data object Editor : Screen("editor/{categoryId}") {
        fun createRoute(categoryId: Long) = "editor/$categoryId"
    }
}
