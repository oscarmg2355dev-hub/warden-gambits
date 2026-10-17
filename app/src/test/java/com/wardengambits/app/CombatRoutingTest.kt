package com.wardengambits.app

import android.app.Application
import android.os.Looper
import androidx.activity.ComponentActivity
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavHostController
import androidx.navigation.compose.ComposeNavigator
import androidx.navigation.testing.TestNavHostController
import androidx.test.core.app.ApplicationProvider
import com.wardengambits.app.audio.FakeSoundPlayer
import com.wardengambits.app.navigation.Routes
import com.wardengambits.app.navigation.WardenNavHost
import com.wardengambits.app.navigation.navigateSingle
import com.wardengambits.app.ui.combat.CombatViewModel
import com.wardengambits.engine.combo.Input
import com.wardengambits.engine.model.Outcome
import com.wardengambits.engine.story.NodeId
import com.wardengambits.engine.story.Phase
import com.wardengambits.engine.story.StoryProfile
import com.wardengambits.engine.story.TreeShape
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import java.time.Duration

/** Real [WardenNavHost] with a real [AppContainer]: what a finished fight does to the route and the story. */
@RunWith(RobolectricTestRunner::class)
class CombatRoutingTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private val sounds = FakeSoundPlayer()
    private val container = AppContainer(ApplicationProvider.getApplicationContext<Application>(), audio = { sounds })
    private val engine = container.storyEngine

    private fun seed(node: NodeId, level: Int, path: List<NodeId> = listOf(node)) {
        val p = engine.newGame()
        val seeded: StoryProfile =
            p.copy(run = p.run.copy(node = node, path = path, phase = Phase.COMBAT_PENDING, level = level))
        assertTrue(engine.isValid(seeded))
        runBlocking {
            container.saveRepository.save(seeded)
            container.session.load()
        }
        assertEquals(seeded, container.session.profile.value)
    }

    private fun openCombat(): NavHostController {
        lateinit var nav: NavHostController
        composeRule.setContent {
            nav = TestNavHostController(LocalContext.current).apply {
                navigatorProvider.addNavigator(ComposeNavigator())
            }
            WardenNavHost(container, nav)
        }
        composeRule.runOnUiThread {
            nav.navigateSingle(Routes.MAP)
            nav.navigateSingle(Routes.NODE)
            nav.navigateSingle(Routes.COMBAT)
        }
        composeRule.waitForIdle()
        assertEquals(Routes.COMBAT, nav.currentBackStackEntry?.destination?.route)
        return nav
    }

    private fun combatViewModel(nav: NavHostController): CombatViewModel {
        val entry = checkNotNull(nav.currentBackStackEntry)
        val existingOnly = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T = error("CombatViewModel was not created by the route")
        }
        return ViewModelProvider(entry.viewModelStore, existingOnly)[CombatViewModel::class.java]
    }

    /** Fights with real one-second ticks (main looper time) until the engine reports an outcome. */
    private fun fightUntilOver(vm: CombatViewModel): Outcome {
        repeat(600) {
            vm.state.value.outcome?.let { return it }
            composeRule.runOnUiThread {
                vm.press(Input.SPEAR)
                vm.press(Input.SHIELD)
                vm.execute()
            }
            shadowOf(Looper.getMainLooper()).idleFor(Duration.ofSeconds(1))
            composeRule.waitForIdle()
        }
        error("Combat did not finish")
    }

    @Test fun victoryReturnsToNodeAndAdvancesPhase() {
        val node = TreeShape.nodesOf("bree").first { engine.node(it).encounter?.rivals?.size == 1 }
        seed(node, level = 30)
        val nav = openCombat()
        assertEquals(listOf("startAmbient"), sounds.calls)
        assertEquals(Outcome.VICTORY, fightUntilOver(combatViewModel(nav)))
        composeRule.waitForIdle()
        assertTrue(sounds.calls.containsAll(listOf("sfx:DEFEAT_ENEMY", "sfx:VICTORY", "stopAmbient")))
        assertEquals("ambience stops once", 1, sounds.calls.count { it == "stopAmbient" })
        composeRule.onNodeWithText(composeRule.activity.getString(R.string.combat_continue)).performClick()
        composeRule.waitForIdle()
        assertEquals(Routes.NODE, nav.currentBackStackEntry?.destination?.route)
        assertEquals(Phase.CHOOSING, container.session.profile.value.run.phase)
    }

    @Test fun stoppingTheActivityPausesTheAmbienceImmediately() {
        val node = TreeShape.nodesOf("bree").first { engine.node(it).encounter?.rivals?.size == 1 }
        seed(node, level = 30)
        openCombat()
        assertEquals(listOf("startAmbient"), sounds.calls)
        composeRule.activityRule.scenario.moveToState(Lifecycle.State.CREATED)
        assertEquals(listOf("startAmbient", "pauseAmbient"), sounds.calls)
    }

    private fun pressKeys() {
        listOf("btn_spear", "btn_shield", "btn_fist").forEach { tag ->
            composeRule.onNodeWithTag(tag).performClick()
            composeRule.waitForIdle()
        }
    }

    private fun keySounds() = sounds.calls.filter { it.startsWith("sfx:KEY_") }

    @Test fun keys123PlaySwordShieldAndShout() {
        val node = TreeShape.nodesOf("bree").first { engine.node(it).encounter?.rivals?.size == 1 }
        seed(node, level = 30)
        openCombat()
        pressKeys()
        assertEquals(listOf("sfx:KEY_SWORD", "sfx:KEY_SHIELD", "sfx:KEY_PUNCH"), keySounds())
    }

    @Test fun keysPlayNothingWithSoundOff() {
        val node = TreeShape.nodesOf("bree").first { engine.node(it).encounter?.rivals?.size == 1 }
        seed(node, level = 30)
        openCombat()
        container.soundPlayer.setEnabled(false)
        pressKeys()
        assertEquals(emptyList<String>(), keySounds())
    }

    @Test fun disabledKeysPlayNothing() {
        val node = TreeShape.nodesOf("bree").first { engine.node(it).encounter?.rivals?.size == 1 }
        seed(node, level = 30)
        val vm = combatViewModel(openCombat())
        composeRule.runOnUiThread { vm.togglePause() }
        composeRule.waitForIdle()
        assertFalse(vm.state.value.inputsEnabled)
        pressKeys()
        assertEquals(emptyList<String>(), keySounds())
    }

    /** A nimrodel fight far above a level-1 Warden, reached from its parent node. */
    private fun seedLostCause(): NodeId {
        val node = TreeShape.nodesOf("nimrodel").first { engine.node(it).encounter != null }
        val previous = TreeShape.nodesOf("nimrodel").first { node in TreeShape.destinations(it) }
        seed(node, level = 1, path = listOf(previous, node))
        return previous
    }

    @Test fun defeatReturnsToNodeScreenAtThePreviousNode() {
        val previous = seedLostCause()
        val nav = openCombat()
        assertEquals(Outcome.DEFEAT, fightUntilOver(combatViewModel(nav)))
        composeRule.waitForIdle()
        assertTrue(sounds.calls.containsAll(listOf("startAmbient", "sfx:DEFEAT", "stopAmbient")))
        composeRule.onNodeWithText(composeRule.activity.getString(R.string.combat_back_to_previous_node)).performClick()
        composeRule.waitForIdle()
        assertEquals(Routes.NODE, nav.currentBackStackEntry?.destination?.route)
        val run = container.session.profile.value.run
        assertEquals(previous, run.node)
        assertEquals(listOf(previous), run.path)
        assertEquals(Phase.CHOOSING, run.phase)
        assertEquals(1, run.level)
        assertEquals(1, run.deaths)
        assertTrue(container.session.profile.value.history.isEmpty())
    }

    @Test fun abandonReturnsToNodeScreenAtThePreviousNode() {
        val previous = seedLostCause()
        val nav = openCombat()
        composeRule.runOnUiThread { composeRule.activity.onBackPressedDispatcher.onBackPressed() }
        composeRule.waitForIdle()
        composeRule.onNodeWithText(composeRule.activity.getString(R.string.combat_abandon_confirm)).performClick()
        composeRule.waitForIdle()
        assertEquals(Routes.NODE, nav.currentBackStackEntry?.destination?.route)
        val run = container.session.profile.value.run
        assertEquals(previous, run.node)
        assertEquals(Phase.CHOOSING, run.phase)
        assertEquals(1, run.deaths)
    }
}
