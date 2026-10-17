package com.wardengambits.app.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.wardengambits.app.AppContainer
import com.wardengambits.app.WardenApp
import com.wardengambits.app.i18n.LocalAppLanguage
import com.wardengambits.app.i18n.LocalScript
import com.wardengambits.app.ui.about.AboutScreen
import com.wardengambits.app.ui.character.CharacterScreen
import com.wardengambits.app.ui.character.CharacterViewModel
import com.wardengambits.app.ui.character.HistoryScreen
import com.wardengambits.app.ui.combat.CombatRoute
import com.wardengambits.app.ui.combat.CombatViewModel
import com.wardengambits.app.ui.ending.EndingScreen
import com.wardengambits.app.ui.ending.EndingViewModel
import com.wardengambits.app.ui.help.ComboDescriber
import com.wardengambits.app.ui.help.HelpScreen
import com.wardengambits.app.ui.map.MapScreen
import com.wardengambits.app.ui.map.MapViewModel
import com.wardengambits.app.ui.menu.MenuScreen
import com.wardengambits.app.ui.node.NodeActions
import com.wardengambits.app.ui.node.NodeScreen
import com.wardengambits.app.ui.node.NodeViewModel
import com.wardengambits.app.ui.options.OptionsScreen
import com.wardengambits.engine.story.Phase
import com.wardengambits.engine.story.StoryProfile

object Routes {
    const val MENU = "menu"
    const val MAP = "map"
    const val NODE = "node"
    const val COMBAT = "combat"
    const val CHARACTER = "character"
    const val HISTORY = "history"
    const val HELP = "help"
    const val ENDING = "ending"
    const val OPTIONS = "options"
    const val ABOUT = "about"
}

/** Where "continue the story" leads: the ending screen once the lap is complete, otherwise [otherwise]. */
fun storyRouteFor(profile: StoryProfile, otherwise: String = Routes.NODE): String =
    if (profile.run.phase == Phase.LAP_COMPLETE) Routes.ENDING else otherwise

/** Forward navigation that ignores double taps (no duplicate destinations). */
fun NavHostController.navigateSingle(route: String) = navigate(route) { launchSingleTop = true }

/** Back that never pops the start destination (which would leave an empty NavHost). */
fun NavHostController.safeBack() {
    if (previousBackStackEntry != null) popBackStack()
}

@Composable
fun WardenNavHost(
    container: AppContainer = (LocalContext.current.applicationContext as WardenApp).container,
    navController: NavHostController = rememberNavController(),
) {
    // The story's texts follow the app language (the composition below reads them through the view models).
    val language = LocalAppLanguage.current
    LaunchedEffect(language) { container.showStoryIn(language) }
    NavHost(navController = navController, startDestination = Routes.MENU) {
        composable(Routes.MENU) {
            MenuScreen(
                // A completed lap goes straight back to its ending screen.
                onStory = { navController.navigateSingle(storyRouteFor(container.session.profile.value, Routes.MAP)) },
                onCharacter = { navController.navigateSingle(Routes.CHARACTER) },
                onHelp = { navController.navigateSingle(Routes.HELP) },
                onNewAdventure = container.session::newAdventure,
                onOptions = { navController.navigateSingle(Routes.OPTIONS) },
                onAbout = { navController.navigateSingle(Routes.ABOUT) },
            )
        }
        composable(Routes.OPTIONS) {
            val soundEnabled by container.soundEnabled.collectAsStateWithLifecycle()
            OptionsScreen(
                soundEnabled = soundEnabled,
                language = LocalAppLanguage.current,
                script = LocalScript.current,
                onToggleSound = container::toggleSound,
                onToggleLanguage = container::toggleLanguage,
                onToggleScript = container::toggleScript,
                onBack = { navController.safeBack() },
            )
        }
        composable(Routes.MAP) {
            val vm: MapViewModel = viewModel(
                factory = viewModelFactory { initializer { MapViewModel(container.session, container.storyTexts) } },
            )
            val rows by vm.rows.collectAsStateWithLifecycle()
            MapScreen(
                rows = rows,
                onContinue = { navController.navigateSingle(storyRouteFor(container.session.profile.value)) },
                onBack = { navController.safeBack() },
            )
        }
        composable(Routes.NODE) {
            val vm: NodeViewModel = viewModel(
                factory = viewModelFactory {
                    initializer { NodeViewModel(container.session, container.storyTexts, container.clock) }
                },
            )
            val ui by vm.node.collectAsStateWithLifecycle()
            if (ui.actions is NodeActions.NextLap) {
                // Entering the final node completes the lap: show the ending instead of the plain node.
                LaunchedEffect(Unit) {
                    navController.navigate(Routes.ENDING) {
                        popUpTo(Routes.NODE) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            } else {
                NodeScreen(
                    ui = ui,
                    onFight = { navController.navigateSingle(Routes.COMBAT) },
                    onChoose = vm::choose,
                    onContinue = vm::continueOn,
                    onNextLap = vm::startNextLap,
                    onMap = { navController.safeBack() },
                )
            }
        }
        composable(Routes.ENDING) {
            val vm: EndingViewModel = viewModel(
                factory = viewModelFactory {
                    initializer { EndingViewModel(container.session, container.storyTexts, container.clock) }
                },
            )
            val ui by vm.ui.collectAsStateWithLifecycle()
            // Double taps, back or the exit transition must navigate only once.
            var handled by remember { mutableStateOf(false) }
            val toMenu = {
                if (!handled) {
                    handled = true
                    navController.popBackStack(Routes.MENU, inclusive = false)
                }
            }
            BackHandler { toMenu() }
            val current = ui
            if (current == null) {
                // No completed lap (e.g. a new adventure started elsewhere): nothing to celebrate.
                LaunchedEffect(Unit) { toMenu() }
            } else {
                EndingScreen(
                    ui = current,
                    onNextLap = {
                        if (!handled) {
                            handled = true
                            vm.startNextLap()
                            navController.navigate(Routes.MAP) {
                                popUpTo(Routes.MENU)
                                launchSingleTop = true
                            }
                        }
                    },
                    onMenu = { toMenu() },
                )
            }
        }
        composable(Routes.COMBAT) {
            val vm: CombatViewModel = viewModel(
                factory = viewModelFactory {
                    initializer {
                        CombatViewModel(
                            container.session,
                            container.storyEngine,
                            container.storyTexts,
                            container.combatEngine,
                            container.clock,
                        )
                    }
                },
            )
            CombatRoute(
                viewModel = vm,
                soundPlayer = container.soundPlayer,
                onBackToNode = { navController.safeBack() },
            )
        }
        composable(Routes.CHARACTER) {
            val vm: CharacterViewModel = viewModel(
                factory = viewModelFactory { initializer { CharacterViewModel(container.session, container.storyTexts) } },
            )
            val ui by vm.ui.collectAsStateWithLifecycle()
            CharacterScreen(
                ui = ui,
                onBack = { navController.safeBack() },
                onHistory = { navController.navigateSingle(Routes.HISTORY) },
            )
        }
        composable(Routes.HISTORY) {
            val vm: CharacterViewModel = viewModel(
                factory = viewModelFactory { initializer { CharacterViewModel(container.session, container.storyTexts) } },
            )
            val ui by vm.ui.collectAsStateWithLifecycle()
            HistoryScreen(history = ui.history, onBack = { navController.safeBack() })
        }
        composable(Routes.ABOUT) {
            AboutScreen(onBack = { navController.safeBack() })
        }
        composable(Routes.HELP) {
            HelpScreen(groups = ComboDescriber.grouped(), onBack = { navController.safeBack() })
        }
    }
}
