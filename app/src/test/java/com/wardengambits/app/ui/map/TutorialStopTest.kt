package com.wardengambits.app.ui.map

import android.app.Application
import androidx.activity.ComponentActivity
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.navigation.NavHostController
import androidx.navigation.compose.ComposeNavigator
import androidx.navigation.testing.TestNavHostController
import androidx.test.core.app.ApplicationProvider
import com.wardengambits.app.AppContainer
import com.wardengambits.app.audio.FakeSoundPlayer
import com.wardengambits.app.navigation.Routes
import com.wardengambits.app.navigation.WardenNavHost
import com.wardengambits.app.navigation.navigateSingle
import com.wardengambits.app.ui.common.PortraitSprites
import com.wardengambits.app.ui.theme.WardenTheme
import com.wardengambits.engine.story.Route
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class TutorialStopTest {
    @get:Rule
    val composeRule = createComposeRule()

    private val lessonIds = listOf("attack", "defense", "daze", "drain", "finisher")

    private val rows = Route.zones.mapIndexed { i, z ->
        if (i == 0) ZoneRowUi(z.id, z.name, ZoneStatus.CURRENT, 0, 7) else ZoneRowUi(z.id, z.name, ZoneStatus.LOCKED, null, null)
    }

    private fun show(showCard: Boolean, onTutorial: () -> Unit = {}, onSkip: () -> Unit = {}) {
        composeRule.setContent {
            WardenTheme {
                MapScreen(
                    rows = rows,
                    onContinue = {},
                    onBack = {},
                    showTutorialCard = showCard,
                    onTutorial = onTutorial,
                    onSkipTutorial = onSkip,
                )
            }
        }
        composeRule.waitUntil(20_000) { composeRule.onAllNodesWithTag(MAP_WORLD_TAG).fetchSemanticsNodes().isNotEmpty() }
    }

    @Test
    fun mapShowsTheTrainingGroundStopAndTappingItOpensTheTutorial() {
        var opened = 0
        show(showCard = false, onTutorial = { opened++ })
        composeRule.onNodeWithText("Campo de entrenamiento").assertIsDisplayed()
        composeRule.onNodeWithTag(TUTORIAL_STOP_TAG).assertIsDisplayed().performClick()
        assertEquals(1, opened)
    }

    @Test
    fun newGameCardStartsOrSkipsTheTutorial() {
        var opened = 0
        var skipped = 0
        show(showCard = true, onTutorial = { opened++ }, onSkip = { skipped++ })
        composeRule.onNodeWithText("Aprende a combatir en 5 lecciones").assertIsDisplayed()
        composeRule.onNodeWithText("Empezar entrenamiento").assertIsDisplayed().performClick()
        assertEquals(1, opened)
        composeRule.onNodeWithText("Saltar").assertIsDisplayed().performClick()
        assertEquals(1, skipped)
    }

    @Test
    fun noCardWhenNotRequested() {
        show(showCard = false)
        composeRule.onNodeWithText("Aprende a combatir en 5 lecciones").assertDoesNotExist()
        composeRule.onNodeWithText("Empezar entrenamiento").assertDoesNotExist()
        composeRule.onNodeWithText("Saltar").assertDoesNotExist()
    }

    @Test
    fun cardOnlyWhileLessonsArePendingAndNotSkipped() {
        assertTrue(showTutorialCard(emptySet(), dismissed = false, lessonIds, newGame = true))
        assertTrue(showTutorialCard(setOf("attack", "daze"), dismissed = false, lessonIds, newGame = true))
        assertFalse(showTutorialCard(lessonIds.toSet(), dismissed = false, lessonIds, newGame = true))
        assertFalse(showTutorialCard(emptySet(), dismissed = true, lessonIds, newGame = true))
        // Until DataStore delivers either setting the card stays hidden, so it never flashes.
        assertFalse(showTutorialCard(emptySet(), dismissed = null, lessonIds, newGame = true))
        assertFalse(showTutorialCard(null, dismissed = false, lessonIds, newGame = true))
        assertFalse(showTutorialCard(null, dismissed = null, lessonIds, newGame = true))
        // Past the first zone the card does not pull the map away from the current zone.
        assertFalse(showTutorialCard(emptySet(), dismissed = false, lessonIds, newGame = false))
    }

    @Test
    fun aNewGameIsStillInTheFirstZone() {
        assertTrue(isNewGame(rows))
        val later = Route.zones.mapIndexed { i, z ->
            when {
                i < 3 -> ZoneRowUi(z.id, z.name, ZoneStatus.DONE, null, null)
                i == 3 -> ZoneRowUi(z.id, z.name, ZoneStatus.CURRENT, 1, 7)
                else -> ZoneRowUi(z.id, z.name, ZoneStatus.LOCKED, null, null)
            }
        }
        assertFalse(isNewGame(later))
        assertFalse(isNewGame(emptyList()))
    }

    @Test
    fun dummySpriteExists() {
        assertEquals("dummy", PortraitSprites.baseType("dummy"))
        val sprite = PortraitSprites.spriteFor("dummy")
        assertEquals(16, sprite.rows.size)
        sprite.rows.forEach { row ->
            assertEquals(16, row.length)
            row.filter { it != '.' }.forEach { assertTrue("'$it' not in palette", it in sprite.palette) }
        }
    }
}

/** Real [WardenNavHost]: a new game opens the map with the tutorial card; «Saltar» hides it for good. */
@RunWith(RobolectricTestRunner::class)
class TutorialStopNavTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private val container =
        AppContainer(ApplicationProvider.getApplicationContext<Application>(), audio = { FakeSoundPlayer() })

    private fun start(): NavHostController {
        lateinit var nav: NavHostController
        composeRule.setContent {
            nav = TestNavHostController(LocalContext.current).apply { navigatorProvider.addNavigator(ComposeNavigator()) }
            WardenNavHost(container, nav)
        }
        composeRule.waitForIdle()
        composeRule.runOnUiThread { nav.navigateSingle(Routes.MAP) }
        composeRule.waitUntil(20_000) { composeRule.onAllNodesWithTag(MAP_WORLD_TAG).fetchSemanticsNodes().isNotEmpty() }
        return nav
    }

    private fun cardShown() = composeRule.onAllNodesWithTag(TUTORIAL_CARD_TAG).fetchSemanticsNodes().isNotEmpty()

    @Test
    fun skipHidesTheCardAndKeepsTheGame() {
        val before = container.session.profile.value
        start()
        composeRule.waitUntil(5_000) { cardShown() }
        composeRule.onNodeWithText("Saltar").performClick()
        composeRule.waitUntil(5_000) { !cardShown() }
        assertEquals(true, container.tutorialDismissed.value)
        composeRule.onNodeWithTag(TUTORIAL_STOP_TAG).assertExists()
        assertEquals(before, container.session.profile.value)
    }

    @Test
    fun startTrainingOpensTheLessonList() {
        val nav = start()
        composeRule.waitUntil(5_000) { cardShown() }
        composeRule.onNodeWithText("Empezar entrenamiento").performClick()
        composeRule.waitForIdle()
        assertEquals(Routes.TUTORIAL, nav.currentBackStackEntry?.destination?.route)
    }
}
