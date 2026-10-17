package com.wardengambits.app

import android.app.Application
import androidx.activity.ComponentActivity
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.navigation.NavHostController
import androidx.navigation.compose.ComposeNavigator
import androidx.navigation.testing.TestNavHostController
import androidx.test.core.app.ApplicationProvider
import com.wardengambits.app.audio.FakeSoundPlayer
import com.wardengambits.app.navigation.Routes
import com.wardengambits.app.navigation.WardenNavHost
import com.wardengambits.app.navigation.navigateSingle
import com.wardengambits.engine.story.NodeId
import com.wardengambits.engine.story.Phase
import com.wardengambits.engine.story.Route
import com.wardengambits.engine.story.TreeShape
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/** Real [WardenNavHost]: reaching the final node shows the ending screen, never a silent jump to Bree. */
@RunWith(RobolectricTestRunner::class)
class EndingRoutingTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private val container =
        AppContainer(ApplicationProvider.getApplicationContext<Application>(), audio = { FakeSoundPlayer() })
    private val engine = container.storyEngine

    private fun seed(node: NodeId, phase: Phase) {
        val p = engine.newGame()
        val seeded = p.copy(run = p.run.copy(node = node, path = listOf(node), phase = phase, level = 50))
        assertTrue(engine.isValid(seeded))
        runBlocking {
            container.saveRepository.save(seeded)
            container.session.load()
        }
    }

    private fun start(): NavHostController {
        lateinit var nav: NavHostController
        composeRule.setContent {
            nav = TestNavHostController(LocalContext.current).apply {
                navigatorProvider.addNavigator(ComposeNavigator())
            }
            WardenNavHost(container, nav)
        }
        composeRule.waitForIdle()
        return nav
    }

    private fun route(nav: NavHostController) = nav.currentBackStackEntry?.destination?.route

    private fun click(text: String) {
        composeRule.onNodeWithText(text).performClick()
        composeRule.waitForIdle()
    }

    private fun string(id: Int, vararg args: Any) = composeRule.activity.getString(id, *args)

    @Test fun continuingIntoTheFinalNodeOpensTheEnding() {
        val lastZone = Route.treeZones.last().id
        seed(NodeId(lastZone, TreeShape.LEVELS, 0), Phase.CHOOSING)
        val nav = start()
        composeRule.runOnUiThread {
            nav.navigateSingle(Routes.MAP)
            nav.navigateSingle(Routes.NODE)
        }
        composeRule.waitForIdle()
        click(string(R.string.node_continue))
        assertEquals(Phase.LAP_COMPLETE, container.session.profile.value.run.phase)
        assertEquals(Routes.ENDING, route(nav))
        assertEquals(listOf(Routes.MENU, Routes.MAP, Routes.ENDING), nav.currentBackStack.value.mapNotNull { it.destination.route })
        composeRule.onNodeWithText(string(R.string.ending_title)).assertExists()
    }

    @Test fun storyFromTheMenuWithACompletedLapOpensTheEnding() {
        seed(NodeId.FINAL, Phase.LAP_COMPLETE)
        val nav = start()
        click(string(R.string.menu_story))
        assertEquals(Routes.ENDING, route(nav))
    }

    @Test fun mapContinueWithACompletedLapOpensTheEnding() {
        seed(NodeId.FINAL, Phase.LAP_COMPLETE)
        val nav = start()
        composeRule.runOnUiThread { nav.navigateSingle(Routes.MAP) }
        composeRule.waitForIdle()
        // The map is generated off the main thread: wait for it before tapping Continuar.
        composeRule.waitUntil(20_000) {
            composeRule.onAllNodesWithText(string(R.string.map_continue)).fetchSemanticsNodes().isNotEmpty()
        }
        click(string(R.string.map_continue))
        assertEquals(Routes.ENDING, route(nav))
    }

    @Test fun backToMenuKeepsTheCompletedLap() {
        seed(NodeId.FINAL, Phase.LAP_COMPLETE)
        val nav = start()
        click(string(R.string.menu_story))
        click(string(R.string.ending_menu))
        assertEquals(Routes.MENU, route(nav))
        assertEquals(Phase.LAP_COMPLETE, container.session.profile.value.run.phase)
    }

    /** Road to Mordor §4: a save at the old final node now continues in the rest zone of Lothlórien. */
    @Test fun oldFinalSaveContinuesInLothlorien() {
        val node = NodeId("lothlorien", 1, 0)
        val p = engine.newGame()
        val old = p.copy(run = p.run.copy(node = node, path = listOf(node), phase = Phase.LAP_COMPLETE, level = 40))
        runBlocking {
            container.saveRepository.save(old)
            container.session.load()
        }
        val run = container.session.profile.value.run
        assertEquals(node, run.node)
        assertEquals(Phase.CHOOSING, run.phase)
        val nav = start()
        click(string(R.string.menu_story))
        assertEquals(Routes.MAP, route(nav))
    }

    @Test fun nextLapGoesToTheMap() {
        seed(NodeId.FINAL, Phase.LAP_COMPLETE)
        val nav = start()
        click(string(R.string.menu_story))
        click(string(R.string.node_next_lap, 2))
        assertEquals(Routes.MAP, route(nav))
        assertEquals(listOf(Routes.MENU, Routes.MAP), nav.currentBackStack.value.mapNotNull { it.destination.route })
        val run = container.session.profile.value.run
        assertEquals(2, run.lap)
        assertEquals(NodeId.START, run.node)
    }
}
