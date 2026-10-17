package com.wardengambits.app.ui.combat

import androidx.compose.foundation.layout.width
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import com.wardengambits.app.ui.theme.WardenTheme
import com.wardengambits.engine.combo.Input
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import kotlin.random.Random

@RunWith(RobolectricTestRunner::class)
class ActionButtonsTest {
    @get:Rule
    val composeRule = createComposeRule()

    private val keyTags = listOf("btn_spear", "btn_shield", "btn_fist")

    private fun show(
        enabled: Boolean = true,
        ready: Boolean = false,
        running: Boolean = true,
        onPress: (Input) -> Unit = {},
        onExecute: () -> Unit = {},
    ) {
        composeRule.setContent {
            WardenTheme {
                ActionButtons(onPress, onExecute, enabled, ready, running, Modifier.width(360.dp))
            }
        }
    }

    private fun stateDescriptionOf(tag: String): String? =
        composeRule.onNodeWithTag(tag).fetchSemanticsNode().config.getOrNull(SemanticsProperties.StateDescription)

    private fun liveSparks(): Int =
        composeRule.onNodeWithTag(ACTION_BUTTONS_TAG).fetchSemanticsNode().config[LiveSparkCount]

    @Test fun pressingKeysCallsCallbacks() {
        val pressed = mutableListOf<Input>()
        var executes = 0
        show(onPress = { pressed += it }, onExecute = { executes++ })
        keyTags.forEach { composeRule.onNodeWithTag(it).performClick() }
        composeRule.onNodeWithTag("btn_execute").performClick()
        assertEquals(listOf(Input.SPEAR, Input.SHIELD, Input.FIST), pressed)
        assertEquals(1, executes)
    }

    @Test fun disabledKeysDoNothing() {
        val pressed = mutableListOf<Input>()
        var executes = 0
        show(enabled = false, onPress = { pressed += it }, onExecute = { executes++ })
        (keyTags + "btn_execute").forEach { composeRule.onNodeWithTag(it).performClick() }
        assertTrue(pressed.isEmpty())
        assertEquals(0, executes)
        assertEquals(0, liveSparks())
    }

    @Test fun readyStateIsExposed() {
        show(ready = true)
        assertEquals("listo", stateDescriptionOf("btn_execute"))
    }

    @Test fun notReadyHasNoReadyState() {
        show(ready = false)
        assertNull(stateDescriptionOf("btn_execute"))
    }

    @Test fun disabledReadyDoesNotPulse() {
        show(enabled = false, ready = true)
        assertNull(stateDescriptionOf("btn_execute"))
    }

    @Test fun sparkBurstHasCountAndSpeedRange() {
        val ten = sparkBurst(10, Random(1))
        assertEquals(10, ten.size)
        ten.forEach { assertTrue("speed ${it.speedDpPerSec}", it.speedDpPerSec in 120f..260f) }
        assertEquals(18, sparkBurst(18, Random(2)).size)
    }

    @Test fun pressSpawnsSparksThatDieOut() {
        show()
        composeRule.mainClock.autoAdvance = false
        composeRule.onNodeWithTag("btn_spear").performClick()
        composeRule.mainClock.advanceTimeByFrame()
        assertEquals(10, liveSparks())
        composeRule.mainClock.advanceTimeBy(600)
        assertEquals(0, liveSparks())
    }

    @Test fun readyPressSpawnsBiggerBurst() {
        show(ready = true)
        composeRule.mainClock.autoAdvance = false
        composeRule.onNodeWithTag("btn_execute").performClick()
        composeRule.mainClock.advanceTimeByFrame()
        assertEquals(18, liveSparks())
    }

    @Test fun rapidPressesKeepCallbacksAndCapSparks() {
        var presses = 0
        show(onPress = { presses++ })
        composeRule.mainClock.autoAdvance = false
        var most = 0
        repeat(30) {
            composeRule.onNodeWithTag("btn_spear").performClick()
            composeRule.mainClock.advanceTimeByFrame()
            val live = liveSparks()
            assertTrue("live sparks $live", live <= MAX_LIVE_SPARKS)
            most = maxOf(most, live)
        }
        assertEquals(30, presses)
        assertEquals(MAX_LIVE_SPARKS, most)
    }
}
