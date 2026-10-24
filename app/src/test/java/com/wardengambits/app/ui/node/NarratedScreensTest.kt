package com.wardengambits.app.ui.node

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.wardengambits.app.audio.FakeNarrationPlayer
import com.wardengambits.app.i18n.AppLanguage
import com.wardengambits.app.i18n.LocalAppLanguage
import com.wardengambits.app.audio.NarrationController
import com.wardengambits.app.audio.NarrationState
import com.wardengambits.app.ui.ending.EndingUi
import com.wardengambits.app.ui.ending.NarratedEndingScreen
import com.wardengambits.app.ui.theme.WardenTheme
import com.wardengambits.engine.story.Choice
import com.wardengambits.engine.story.NodeId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class NarratedScreensTest {
    @get:Rule
    val composeRule = createComposeRule()

    private val player = FakeNarrationPlayer(setOf("es/bree_1_0", "en/bree_1_0", "es/orodruin_final"))
    private val controller = NarrationController(player)

    private fun ui(key: String = "bree_1_0", actions: NodeActions = NodeActions.Continue) =
        NodeUi(key, "bree", "Bree", 1, 7, "Titulo X", "Texto", actions)

    private fun show(ui: NodeUi, onChoose: (Int) -> Unit = {}, onFight: () -> Unit = {}) = composeRule.setContent {
        WardenTheme { NarratedNodeScreen(ui, controller, true, onFight, onChoose, {}, {}, {}) }
    }

    @Test fun stopButtonWhilePlayingThenReadButton() {
        show(ui())
        composeRule.onNodeWithText("■ Parar").assertIsDisplayed()
        composeRule.runOnIdle { player.finish() }
        composeRule.onNodeWithText("▶ Leer").assertIsDisplayed()
        composeRule.onNodeWithText("▶ Leer").performClick()
        composeRule.onNodeWithText("■ Parar").assertIsDisplayed()
    }

    @Test fun noAudioNoButton() {
        show(ui("bree_5_5"))
        composeRule.onNodeWithText("Titulo X").assertIsDisplayed()
        composeRule.onNodeWithText("■ Parar").assertDoesNotExist()
        composeRule.onNodeWithText("▶ Leer").assertDoesNotExist()
        assertTrue(player.calls.none { it.startsWith("play") })
    }

    @Test fun choosingStopsNarration() {
        var chosen = -1
        val choices = NodeActions.Choices(listOf(Choice(NodeId("bree", 2, 0), "Ir al norte")))
        show(ui(actions = choices), onChoose = { chosen = it })
        assertEquals(NarrationState.PLAYING, controller.state.value)
        composeRule.onNodeWithText("Ir al norte").performClick()
        assertEquals(0, chosen)
        assertEquals(NarrationState.IDLE, controller.state.value)
    }

    @Test fun fightingStopsNarration() {
        var fights = 0
        show(ui(actions = NodeActions.Fight(listOf(RivalUi("Orco", 3)))), onFight = { fights++ })
        composeRule.onNodeWithText("¡Luchar!").performClick()
        assertEquals(1, fights)
        assertEquals(NarrationState.IDLE, controller.state.value)
    }

    @Test fun languageChangeStopsInsteadOfRestarting() {
        var language by mutableStateOf(AppLanguage.ES)
        composeRule.setContent {
            CompositionLocalProvider(LocalAppLanguage provides language) {
                WardenTheme { NarratedNodeScreen(ui(), controller, true, {}, {}, {}, {}, {}) }
            }
        }
        assertEquals(NarrationState.PLAYING, controller.state.value)
        composeRule.runOnIdle { language = AppLanguage.EN }
        composeRule.waitForIdle()
        assertEquals(NarrationState.IDLE, controller.state.value)
        assertEquals(listOf("play es/bree_1_0"), player.calls.filter { it.startsWith("play") })
    }

    @Test fun noNarrationUntilTheSettingHasLoaded() {
        var enabled by mutableStateOf(false) // what the host passes while DataStore has not answered
        composeRule.setContent { WardenTheme { NarratedNodeScreen(ui(), controller, enabled, {}, {}, {}, {}, {}) } }
        assertTrue(player.calls.none { it.startsWith("play") })
        composeRule.runOnIdle { enabled = true }
        composeRule.waitForIdle()
        assertEquals(listOf("play es/bree_1_0"), player.calls.filter { it.startsWith("play") })
    }

    @Test fun endingNarratesFinalNode() {
        val ending = EndingUi(
            key = "orodruin_final", zoneId = "orodruin", zoneName = "Monte del Destino",
            title = "Fin", text = "Epilogo", lap = 1, nextLap = 2, level = 42, levelsGained = 41, wins = 40, deaths = 5,
        )
        composeRule.setContent { WardenTheme { NarratedEndingScreen(ending, controller, true, {}, {}) } }
        assertTrue(player.calls.contains("play es/orodruin_final"))
    }
}
