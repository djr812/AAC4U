package net.djrogers.aac4u.ui.navigation

import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import net.djrogers.aac4u.ui.grid.GridScreen
import net.djrogers.aac4u.ui.history.HistoryScreen
import net.djrogers.aac4u.ui.profiles.ProfileScreen
import net.djrogers.aac4u.ui.quickphrases.QuickPhrasesScreen
import net.djrogers.aac4u.ui.settings.SettingsScreen

@Composable
fun AAC4UNavHost(
    windowSizeClass: WindowSizeClass,
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Grid.route
    ) {
        composable(Screen.Grid.route) {
            GridScreen(
                windowSizeClass = windowSizeClass,
                onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
                onNavigateToProfiles = { navController.navigate(Screen.Profiles.route) },
                onNavigateToHistory = { navController.navigate(Screen.History.route) },
                onNavigateToQuickPhrases = { navController.navigate(Screen.QuickPhrases.route) }
            )
        }

        composable(Screen.QuickPhrases.route) {
            QuickPhrasesScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Settings.route) {
            SettingsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Profiles.route) {
            ProfileScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.History.route) {
            HistoryScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.Editor.route,
            arguments = listOf(navArgument("categoryId") { type = NavType.LongType })
        ) { backStackEntry ->
            val categoryId = backStackEntry.arguments?.getLong("categoryId") ?: return@composable
            // EditorScreen will be implemented in Phase 2
            // EditorScreen(categoryId = categoryId, onNavigateBack = { navController.popBackStack() })
        }
    }
}
