package net.djrogers.aac4u.ui.navigation

import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.*
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import kotlinx.coroutines.launch
import net.djrogers.aac4u.ui.about.AboutScreen
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
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val gesturesEnabled = currentRoute == Screen.Grid.route

    // Edit mode state — lives here so it persists across drawer open/close
    var isEditMode by remember { mutableStateOf(false) }

    // Exit edit mode when navigating away from grid
    LaunchedEffect(currentRoute) {
        if (currentRoute != Screen.Grid.route) {
            isEditMode = false
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = gesturesEnabled,
        drawerContent = {
            DrawerContent(
                currentRoute = currentRoute,
                isEditMode = isEditMode,
                onNavigate = { screen ->
                    if (screen.route != currentRoute) {
                        navController.navigate(screen.route) {
                            popUpTo(Screen.Grid.route) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                onToggleEditMode = {
                    isEditMode = !isEditMode
                },
                onClose = {
                    scope.launch { drawerState.close() }
                }
            )
        }
    ) {
        NavHost(
            navController = navController,
            startDestination = Screen.Grid.route
        ) {
            composable(Screen.Grid.route) {
                GridScreen(
                    windowSizeClass = windowSizeClass,
                    isEditMode = isEditMode,
                    onToggleEditMode = { isEditMode = !isEditMode },
                    onOpenDrawer = {
                        scope.launch { drawerState.open() }
                    }
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

            composable(Screen.About.route) {
                AboutScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(
                route = Screen.Editor.route,
                arguments = listOf(navArgument("categoryId") { type = NavType.LongType })
            ) { backStackEntry ->
                val categoryId = backStackEntry.arguments?.getLong("categoryId") ?: return@composable
            }
        }
    }
}
