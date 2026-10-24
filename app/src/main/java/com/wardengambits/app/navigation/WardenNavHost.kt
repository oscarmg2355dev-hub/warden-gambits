package com.wardengambits.app.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.currentStateAsState
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.wardengambits.app.AppContainer
import com.wardengambits.app.R
import com.wardengambits.app.WardenApp
import com.wardengambits.app.audio.NarrationState
import com.wardengambits.app.i18n.LocalAppLanguage
import com.wardengambits.app.i18n.LocalScript
import com.wardengambits.app.ui.about.AboutScreen
import com.wardengambits.app.ui.character.CharacterScreen
import com.wardengambits.app.ui.character.CharacterViewModel
import com.wardengambits.app.ui.character.HistoryScreen
import com.wardengambits.app.ui.combat.CombatRoute
import com.wardengambits.app.ui.combat.CombatViewModel
import com.wardengambits.app.ui.combat.PracticeCombatSource
import com.wardengambits.app.ui.combat.StoryCombatSource
import com.wardengambits.app.ui.combat.TutorialCombatSource
import com.wardengambits.app.ui.ending.NarratedEndingScreen
import com.wardengambits.app.ui.ending.EndingViewModel
import com.wardengambits.app.ui.help.ComboDescriber
import com.wardengambits.app.ui.help.HelpScreen
import com.wardengambits.app.ui.map.MapScreen
import com.wardengambits.app.ui.map.MapViewModel
import com.wardengambits.app.ui.map.isNewGame
import com.wardengambits.app.ui.map.showTutorialCard
import com.wardengambits.app.ui.menu.MenuScreen
import com.wardengambits.app.ui.node.NodeActions
import com.wardengambits.app.ui.node.NarratedNodeScreen
import com.wardengambits.app.ui.node.NodeViewModel
import com.wardengambits.app.ui.options.OptionsScreen
import com.wardengambits.app.ui.practice.PracticeScreen
import com.wardengambits.app.ui.practice.PracticeViewModel
import com.wardengambits.app.ui.tutorial.TutorialListScreen
import com.wardengambits.app.ui.tutorial.TutorialTexts
import com.wardengambits.app.ui.tutorial.TutorialViewModel
import com.wardengambits.engine.story.NodeId
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
    const val PRACTICE = "practice"
    const val PRACTICE_COMBAT = "practice_combat/{zone}/{level}/{index}"
    const val TUTORIAL = "tutorial"
    const val TUTORIAL_COMBAT = "tutorial_combat/{lesson}"

    fun practiceCombat(node: NodeId) = "practice_combat/${node.zoneId}/${node.level}/${node.index}"

    fun tutorialCombat(lessonId: String) = "tutorial_combat/$lessonId"
}

/** Where "continue the story" leads: the ending screen once the lap is complete, otherwise [otherwise]. */
fun storyRouteFor(profile: StoryProfile, otherwise: String = Routes.NODE): String =
    if (profile.run.phase == Phase.LAP_COMPLETE) Routes.ENDING else otherwise

/** Forward navigation that ignores double taps (no duplicate destinations). */
fun NavHostController.navigateSingle(route: String) = navigate(route) { launchSingleTop = true }

/** Back to the map when it is in the back stack (the tutorial is opened from it), otherwise one step back. */
fun NavHostController.backToMap() {
    if (!popBackStack(Routes.MAP, inclusive = false)) safeBack()
}

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
    // Ambient music: follows the route, the "Música" option and the app being in the foreground.
    val route = navController.currentBackStackEntryAsState().value?.destination?.route
    val musicEnabled by container.musicEnabled.collectAsStateWithLifecycle()
    val lifecycleState by LocalLifecycleOwner.current.lifecycle.currentStateAsState()
    val foreground = lifecycleState.isAtLeast(Lifecycle.State.STARTED)
    val narrationSetting by container.narrationEnabled.collectAsStateWithLifecycle()
    val narrationEnabled = narrationSetting == true
    val narrationState by container.narration.state.collectAsStateWithLifecycle()
    val narrating = narrationState == NarrationState.PLAYING
    LaunchedEffect(route, musicEnabled, narrating, foreground) {
        container.musicController.update(route, musicEnabled, narrating = narrating, foreground = foreground)
    }
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
                onPractice = { navController.navigateSingle(Routes.PRACTICE) },
            )
        }
        composable(Routes.OPTIONS) {
            val musicEnabled by container.musicEnabled.collectAsStateWithLifecycle()
            val sfxEnabled by container.sfxEnabled.collectAsStateWithLifecycle()
            OptionsScreen(
                musicEnabled = musicEnabled,
                sfxEnabled = sfxEnabled,
                narrationEnabled = narrationSetting ?: true,
                language = LocalAppLanguage.current,
                script = LocalScript.current,
                onToggleMusic = container::toggleMusic,
                onToggleSfx = container::toggleSfx,
                onToggleNarration = container::toggleNarration,
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
            val tutorialDone by container.tutorialDone.collectAsStateWithLifecycle()
            val tutorialDismissed by container.tutorialDismissed.collectAsStateWithLifecycle()
            val lessonIds = remember(container) { container.tutorialLessons.map { it.id } }
            MapScreen(
                rows = rows,
                onContinue = { navController.navigateSingle(storyRouteFor(container.session.profile.value)) },
                onBack = { navController.safeBack() },
                showTutorialCard = showTutorialCard(tutorialDone, tutorialDismissed, lessonIds, newGame = isNewGame(rows)),
                onTutorial = { navController.navigateSingle(Routes.TUTORIAL) },
                onSkipTutorial = container::dismissTutorial,
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
                NarratedNodeScreen(
                    ui = ui,
                    narration = container.narration,
                    narrationEnabled = narrationEnabled,
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
                NarratedEndingScreen(
                    ui = current,
                    narration = container.narration,
                    narrationEnabled = narrationEnabled,
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
                            StoryCombatSource(container.session, container.storyEngine),
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
        composable(Routes.PRACTICE) {
            val vm: PracticeViewModel = viewModel(
                factory = viewModelFactory { initializer { PracticeViewModel(container.session, container.storyTexts) } },
            )
            val zones by vm.zones.collectAsStateWithLifecycle()
            PracticeScreen(
                zones = zones,
                initialZoneIndex = vm.currentZoneIndex,
                onFight = { navController.navigateSingle(Routes.practiceCombat(it)) },
                onBack = { navController.safeBack() },
            )
        }
        composable(
            Routes.PRACTICE_COMBAT,
            arguments = listOf(
                navArgument("zone") { type = NavType.StringType },
                navArgument("level") { type = NavType.IntType },
                navArgument("index") { type = NavType.IntType },
            ),
        ) { entry ->
            val node = NodeId(
                entry.arguments?.getString("zone").orEmpty(),
                entry.arguments?.getInt("level") ?: 1,
                entry.arguments?.getInt("index") ?: 0,
            )
            val vm: CombatViewModel = viewModel(
                factory = viewModelFactory {
                    initializer {
                        CombatViewModel(
                            PracticeCombatSource(node, container.storyEngine),
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
        composable(Routes.TUTORIAL) {
            val vm: TutorialViewModel = viewModel(
                factory = viewModelFactory {
                    initializer { TutorialViewModel(container.tutorialLessons, container.tutorialDone) }
                },
            )
            val lessons by vm.lessons.collectAsStateWithLifecycle()
            TutorialListScreen(
                lessons = lessons,
                onLesson = { navController.navigateSingle(Routes.tutorialCombat(it)) },
                onBack = { navController.backToMap() },
            )
        }
        composable(
            Routes.TUTORIAL_COMBAT,
            arguments = listOf(navArgument("lesson") { type = NavType.StringType }),
        ) { entry ->
            val lessonId = entry.arguments?.getString("lesson").orEmpty()
            val lessonName = TutorialTexts.of(lessonId)?.let { stringResource(it.name) }.orEmpty()
            val dummyName = stringResource(R.string.tutorial_dummy)
            val vm: CombatViewModel = viewModel(
                factory = viewModelFactory {
                    initializer {
                        CombatViewModel(
                            TutorialCombatSource(lessonId, container.tutorialLessons, lessonName, dummyName, container::markTutorialDone),
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
                onBackToMap = { navController.backToMap() },
                onNextLesson = { next ->
                    navController.navigate(Routes.tutorialCombat(next)) {
                        popUpTo(Routes.TUTORIAL_COMBAT) { inclusive = true }
                        launchSingleTop = true
                    }
                },
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
