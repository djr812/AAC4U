package net.djrogers.aac4u.ui.navigation

import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.*
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import kotlinx.coroutines.launch
import net.djrogers.aac4u.ui.about.AboutScreen
import net.djrogers.aac4u.ui.editor.CoreWordsEditorScreen
import net.djrogers.aac4u.ui.grid.GridScreen
import net.djrogers.aac4u.ui.grid.GridViewModel
import net.djrogers.aac4u.ui.history.HistoryScreen
import net.djrogers.aac4u.ui.profiles.ProfileScreen
import net.djrogers.aac4u.ui.quickphrases.QuickPhrasesScreen
import net.djrogers.aac4u.ui.settings.SettingsScreen

@Composable
fun AAC4UNavHost(
    windowSizeClass: WindowSizeClass,
    navController: NavHostController = rememberNavController(),
    welcomeViewModel: WelcomeViewModel = hiltViewModel()
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val gesturesEnabled = currentRoute == Screen.Grid.route

    var isEditMode by remember { mutableStateOf(false) }

    // Phrase loaded from history — consumed by GridScreen on next composition
    var pendingPhrase by remember { mutableStateOf<String?>(null) }

    val activeProfile by welcomeViewModel.activeProfile.collectAsStateWithLifecycle()

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
                activeProfileName = activeProfile?.name,
                activeProfileAvatar = activeProfile?.avatar,
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
                val gridViewModel: GridViewModel = hiltViewModel()

                // Consume pending phrase from history
                LaunchedEffect(pendingPhrase) {
                    pendingPhrase?.let { phrase ->
                        gridViewModel.loadPhraseFromHistory(phrase)
                        pendingPhrase = null
                    }
                }

                GridScreen(
                    windowSizeClass = windowSizeClass,
                    isEditMode = isEditMode,
                    onToggleEditMode = { isEditMode = !isEditMode },
                    onOpenDrawer = {
                        scope.launch { drawerState.open() }
                    },
                    viewModel = gridViewModel
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
                    onNavigateBack = { navController.popBackStack() },
                    onLoadPhrase = { phrase ->
                        pendingPhrase = phrase
                    }
                )
            }

            composable(Screen.About.route) {
                AboutScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(Screen.CoreWordsEditor.route) {
                CoreWordsEditorScreen(
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
