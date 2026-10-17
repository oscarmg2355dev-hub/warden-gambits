package com.wardengambits.app.ui.combat

import androidx.compose.foundation.layout.width
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertHeightIsEqualTo
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.wardengambits.app.ui.theme.WardenTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class GameBarTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test fun barTrailOnDamageIsGoldAndFillFirst() {
        assertEquals(BarTrail(Color(0xFFF3E0A0), fillFirst = true), barTrail(0.8f, 0.5f))
    }

    @Test fun barTrailOnHealIsGreenAndGhostFirst() {
        assertEquals(false, barTrail(0.5f, 0.8f).fillFirst)
    }

    @Test fun gameBarShowsValueText() {
        composeRule.setContent { WardenTheme { GameBar(65, 100, GameBarKind.HP, running = true) } }
        composeRule.onNodeWithText("65/100").assertIsDisplayed()
    }

    @Test fun powerRegenDoesNotFlash() {
        assertEquals(false, barFlashes(100, 105, 300, GameBarKind.POWER))
    }

    @Test fun bigPowerChangeFlashes() {
        assertEquals(true, barFlashes(100, 115, 300, GameBarKind.POWER))
    }

    @Test fun hpAlwaysFlashes() {
        assertEquals(true, barFlashes(100, 99, 300, GameBarKind.HP))
    }

    @Test fun barIs22dpWithReadableText() {
        composeRule.setContent { WardenTheme { GameBar(65, 100, GameBarKind.HP, running = true) } }
        composeRule.onNodeWithTag("gameBar").assertHeightIsEqualTo(22.dp)
        composeRule.onNodeWithText("65/100").assertIsDisplayed()
    }

    @Test fun largeFontDoesNotClip() {
        composeRule.setContent {
            WardenTheme {
                CompositionLocalProvider(LocalDensity provides Density(1f, fontScale = 1.3f)) {
                    GameBar(165600, 165600, GameBarKind.HP, running = true, modifier = Modifier.width(160.dp))
                }
            }
        }
        val node = composeRule.onNodeWithText("165600/165600").assertIsDisplayed().fetchSemanticsNode()
        assertTrue("text is ${node.size.width}px wide", node.size.width <= 160)
    }

    @Test fun fittedFontShrinksUntilItFitsButNotBelowMinimum() {
        // Fake measurer: text is 10 px wide per sp.
        assertEquals(10f, fitFontSize(13f, 9f, 100f) { it * 10f }, 0.01f)
        assertEquals(9f, fitFontSize(13f, 9f, 50f) { it * 10f }, 0.01f)
        assertEquals(13f, fitFontSize(13f, 9f, 500f) { it * 10f }, 0.01f)
    }
}
