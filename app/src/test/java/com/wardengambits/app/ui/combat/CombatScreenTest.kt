package com.wardengambits.app.ui.combat

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.wardengambits.app.ui.theme.WardenTheme
import com.wardengambits.engine.combo.Combos
import com.wardengambits.engine.combo.Input
import com.wardengambits.app.ui.common.EffectBadgeUi
import com.wardengambits.engine.model.FighterId
import com.wardengambits.engine.model.Outcome
import com.wardengambits.engine.model.PLAYER_ID
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(qualifiers = "es-w360dp-h640dp")
class CombatScreenTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private fun rival(id: Int, name: String, level: Int, target: Boolean = false, badges: List<EffectBadgeUi> = emptyList(), alive: Boolean = true) =
        FighterUi(FighterId(id), name, level, if (alive) 40 else 0, 50, if (alive) 10 else 0, 20, badges, isAlive = alive, isTarget = target)

    private val player = FighterUi(PLAYER_ID, "Warden", 3, 80, 100, 30, 60, listOf(EffectBadgeUi("FU", false, 1f)), true, false)

    private fun state(
        rivals: List<FighterUi> = listOf(
            rival(1, "Orco", 2, target = true, badges = listOf(EffectBadgeUi("SP1", true, 0.5f))),
            rival(2, "Trasgo", 4),
            rival(3, "Uruk", 5),
        ),
        buffer: String = "",
        outcome: Outcome? = null,
        paused: Boolean = false,
        inputsEnabled: Boolean = true,
        player: FighterUi = this.player,
    ) = CombatUiState(
        zoneName = "Bree", levelLabel = "3/7", tick = 5, paused = paused, rivals = rivals, player = player,
        buffer = buffer, bufferMatch = Combos.find(buffer), outcome = outcome, levelBefore = 3, winsBefore = 2,
        inputsEnabled = inputsEnabled,
    )

    private fun show(
        state: CombatUiState,
        onPress: (Input) -> Unit = {},
        onExecute: () -> Unit = {},
        onClear: () -> Unit = {},
        onSelectTarget: (FighterId) -> Unit = {},
        onTogglePause: () -> Unit = {},
        onFinish: () -> Unit = {},
        onAbandon: () -> Unit = {},
        message: String? = null,
        floatingNumbers: FloatingNumbersState = FloatingNumbersState(),
    ) = composeRule.setContent {
        WardenTheme {
            CombatScreen(
                state, onPress, onExecute, onClear, onSelectTarget, onTogglePause, onFinish, onAbandon,
                message = message, floatingNumbers = floatingNumbers,
            )
        }
    }

    @Test fun disabledInputsGreyOutAndSwallowClicks() {
        var calls = 0
        show(state(inputsEnabled = false), onPress = { calls++ }, onExecute = { calls++ }, onClear = { calls++ })
        listOf("btn_spear", "btn_shield", "btn_fist", "btn_execute", "btn_clear").forEach { tag ->
            composeRule.onNodeWithTag(tag).assertIsNotEnabled()
            composeRule.onNodeWithTag(tag).performClick()
        }
        assertEquals(0, calls)
    }

    @Test fun dazedPlayerShowsCountdownStarInsteadOfText() {
        show(state(inputsEnabled = false, player = player.copy(dazedTicksLeft = 3)))
        val overlay = composeRule.onNode(hasTestTag("daze_overlay") and hasAnyAncestor(hasTestTag("player_card")), useUnmergedTree = true)
        overlay.assertIsDisplayed()
        composeRule.onNode(hasText("3") and hasAnyAncestor(hasTestTag("daze_overlay")), useUnmergedTree = true).assertIsDisplayed()
        composeRule.onAllNodesWithText("Aturdido").assertCountEquals(0)
        composeRule.onAllNodesWithTag("combat_dazed").assertCountEquals(0)
        val card = composeRule.onNodeWithTag("player_card").getUnclippedBoundsInRoot()
        val star = overlay.getUnclippedBoundsInRoot()
        assertTrue("star $star outside card $card", star.left >= card.left && star.right <= card.right && star.top >= card.top && star.bottom <= card.bottom)
        listOf("btn_spear", "btn_shield", "btn_fist", "btn_execute", "btn_clear").forEach { composeRule.onNodeWithTag(it).assertIsNotEnabled() }
    }

    @Test fun noDazeOverlayWhenNotDazed() {
        show(state())
        composeRule.onAllNodesWithTag("daze_overlay", useUnmergedTree = true).assertCountEquals(0)
    }

    @Test fun enabledInputsInvokeCallbacks() {
        var calls = 0
        show(state(), onPress = { calls++ }, onExecute = { calls++ }, onClear = { calls++ })
        listOf("btn_spear", "btn_shield", "btn_fist", "btn_execute", "btn_clear").forEach { tag ->
            composeRule.onNodeWithTag(tag).assertIsEnabled()
            composeRule.onNodeWithTag(tag).performClick()
        }
        assertEquals(5, calls)
    }

    @Test fun showsRivalCardsAndPlayer() {
        show(state())
        composeRule.onNodeWithText("Orco").assertExists()
        composeRule.onNodeWithText("Trasgo").assertExists()
        composeRule.onNodeWithText("Uruk").assertExists()
        composeRule.onNodeWithText("Nv 2 · ", substring = true).assertExists()
        composeRule.onNodeWithText("Nv 4 · ", substring = true).assertExists()
        composeRule.onNodeWithText("Nv 5 · ", substring = true).assertExists()
        composeRule.onNodeWithText("Warden · Nv 3").assertExists()
        composeRule.onNodeWithTag("rival_empty_3").assertExists()
        composeRule.onNodeWithTag("rival_slot_2").assertExists()
        composeRule.onAllNodesWithTag("rival_slot_3").assertCountEquals(0)
        // exactly one target marker, on the first rival; the empty 4th slot has no card
        composeRule.onAllNodesWithText("◎").assertCountEquals(1)
        composeRule.onNodeWithTag("target_marker_1", useUnmergedTree = true).assertExists()
        composeRule.onNodeWithText("SP1").assertExists()
        composeRule.onNodeWithText("FU").assertExists()
    }

    @Test fun effectBadgeIsShownAt640dpWithFourRivals() {
        val badge = listOf(EffectBadgeUi("SP1", true, 0.5f))
        show(state(rivals = (1..4).map { rival(it, "R$it", 2, target = it == 1, badges = badge) }))
        composeRule.onAllNodesWithText("SP1").assertCountEquals(4)
        composeRule.onAllNodesWithText("SP1")[3].assertIsDisplayed()
    }

    @Test fun effectBadgeIsShownAt640dpWithTwoRivals() {
        val badge = listOf(EffectBadgeUi("SP1", true, 0.5f))
        show(state(rivals = (1..2).map { rival(it, "R$it", 2, target = it == 1, badges = badge) }))
        composeRule.onAllNodesWithText("SP1").assertCountEquals(2)
    }

    @Test fun defeatedRivalShowsDefeatedTextAndNoBadges() {
        show(state(rivals = listOf(rival(1, "Orco", 2, alive = false, badges = listOf(EffectBadgeUi("SP1", true, 0.5f))), rival(2, "Trasgo", 4))))
        composeRule.onNodeWithText("Derrotado").assertExists()
        composeRule.onAllNodesWithText("Derrotado").assertCountEquals(1)
        composeRule.onAllNodesWithText("SP1").assertCountEquals(0)
    }

    @Test fun buttonsInvokeCallbacks() {
        val pressed = mutableListOf<Input>()
        var executes = 0
        var clears = 0
        show(state(), onPress = { pressed += it }, onExecute = { executes++ }, onClear = { clears++ })
        composeRule.onNodeWithTag("btn_spear").performClick()
        composeRule.onNodeWithTag("btn_shield").performClick()
        composeRule.onNodeWithTag("btn_fist").performClick()
        composeRule.onNodeWithTag("btn_execute").performClick()
        composeRule.onNodeWithTag("btn_clear").performClick()
        assertEquals(listOf(Input.SPEAR, Input.SHIELD, Input.FIST), pressed)
        assertEquals(1, executes)
        assertEquals(1, clears)
    }

    @Test fun tapRivalSelectsTarget() {
        var selected: FighterId? = null
        show(state(), onSelectTarget = { selected = it })
        composeRule.onNodeWithText("Trasgo").performClick()
        assertEquals(FighterId(2), selected)
    }

    @Test fun victoryOverlay() {
        var finished = 0
        show(state(outcome = Outcome.VICTORY), onFinish = { finished++ })
        composeRule.onNodeWithText("Nivel 3 → 4").assertIsDisplayed()
        composeRule.onNodeWithText("Continuar").performClick()
        assertEquals(1, finished)
    }

    /** Regression: behind the victory overlay the defeated rivals' HP bars read 0/max. */
    @Test fun victoryOverlayShowsRivalsAtZeroHp() {
        val dead = listOf(
            rival(1, "Orco", 2).copy(hp = 0, isAlive = false),
            rival(2, "Trasgo", 4).copy(hp = 0, isAlive = false),
        )
        show(state(rivals = dead, outcome = Outcome.VICTORY))
        composeRule.onAllNodesWithText("0/50").assertCountEquals(2)
        composeRule.onAllNodesWithText("40/50").assertCountEquals(0)
    }

    @Test fun defeatOverlay() {
        var finished = 0
        show(state(outcome = Outcome.DEFEAT), onFinish = { finished++ })
        composeRule.onNodeWithText("Has caído").assertIsDisplayed()
        composeRule.onNodeWithText("Bree · Nivel 3 · 2 victorias").assertIsDisplayed()
        composeRule.onNodeWithText("Volver al nodo anterior").performClick()
        assertEquals(1, finished)
    }

    @Test fun defeatSummaryPluralisesOneVictory() {
        show(state(outcome = Outcome.DEFEAT).copy(winsBefore = 1))
        composeRule.onNodeWithText("Bree · Nivel 3 · 1 victoria").assertIsDisplayed()
    }

    @Test fun bufferShowsMatch() {
        val match = Combos.find("121")!!
        show(state(buffer = "121"))
        composeRule.onAllNodesWithText("1").assertCountEquals(3) // two mini-keys + the "1" button
        composeRule.onNodeWithText("Lanza II").assertExists()
        composeRule.onNodeWithText("${match.cost} power").assertExists()
    }

    @Test fun bufferShowsNameAndChainMark() {
        show(state(buffer = "121").copy(chainContinues = true))
        composeRule.onNode(hasText("Lanza II ⛓", substring = true)).assertExists()
    }

    @Test fun bufferWithoutChainHasNoMark() {
        show(state(buffer = "12").copy(chainContinues = false))
        composeRule.onNode(hasText("Lanza I", substring = true)).assertExists()
        composeRule.onAllNodes(hasText("⛓", substring = true)).assertCountEquals(0)
    }

    @Test fun pausedOverlayResumes() {
        var toggles = 0
        show(state(paused = true), onTogglePause = { toggles++ })
        composeRule.onNodeWithText("En pausa").assertIsDisplayed()
        composeRule.onNodeWithText("Reanudar").performClick()
        assertEquals(1, toggles)
    }

    @Test fun noPauseOverlayAfterOutcome() {
        show(state(paused = true, outcome = Outcome.VICTORY))
        composeRule.onAllNodesWithText("En pausa").assertCountEquals(0)
    }

    @Test fun showsMessageAndCombatText() {
        show(
            state(),
            message = "Sin power",
            floatingNumbers = FloatingNumbersState().add(
                listOf(CombatTextLine(FighterId(2), "−12 GD", CombatTextTone.DAMAGE)), { 50 }, 0L, kotlin.random.Random(1),
            ),
        )
        composeRule.onNodeWithText("Sin power").assertExists()
        composeRule.onNodeWithText("−12 GD", useUnmergedTree = true).assertExists()
    }

    private fun back() {
        composeRule.runOnUiThread { composeRule.activity.onBackPressedDispatcher.onBackPressed() }
        composeRule.waitForIdle()
    }

    @Test fun backAsksToAbandonAndConfirmAbandons() {
        var abandons = 0
        show(state(), onAbandon = { abandons++ })
        back()
        composeRule.onNodeWithText("¿Abandonar el combate?").assertIsDisplayed()
        composeRule.onNodeWithText("Contará como derrota: volverás al nodo anterior.").assertIsDisplayed()
        composeRule.onNodeWithText("Abandonar").performClick()
        assertEquals(1, abandons)
        composeRule.onAllNodesWithText("¿Abandonar el combate?").assertCountEquals(0)
    }

    @Test fun cancelDialogDoesNotAbandon() {
        var abandons = 0
        show(state(), onAbandon = { abandons++ })
        back()
        composeRule.onNodeWithText("Cancelar").performClick()
        assertEquals(0, abandons)
        composeRule.onAllNodesWithText("¿Abandonar el combate?").assertCountEquals(0)
    }

    @Test fun backWithOutcomeFinishes() {
        var finished = 0
        var abandons = 0
        show(state(outcome = Outcome.VICTORY), onFinish = { finished++ }, onAbandon = { abandons++ })
        back()
        assertEquals(1, finished)
        assertEquals(0, abandons)
        composeRule.onAllNodesWithText("¿Abandonar el combate?").assertCountEquals(0)
    }

    @Test fun openingAbandonDialogPausesAndCancelResumes() {
        var toggles = 0
        show(state(), onTogglePause = { toggles++ })
        back()
        assertEquals(1, toggles)
        composeRule.onNodeWithText("Cancelar").performClick()
        assertEquals(2, toggles)
    }

    @Test fun cancelDoesNotResumeWhenAlreadyPaused() {
        var toggles = 0
        show(state(paused = true), onTogglePause = { toggles++ })
        back()
        composeRule.onNodeWithText("Cancelar").performClick()
        assertEquals(0, toggles)
    }

    @Test fun confirmAbandonDoesNotResume() {
        var toggles = 0
        var abandons = 0
        show(state(), onTogglePause = { toggles++ }, onAbandon = { abandons++ })
        back()
        composeRule.onNodeWithText("Abandonar").performClick()
        assertEquals(1, toggles)
        assertEquals(1, abandons)
    }

    @Test fun showsQueuedCombo() {
        val combo = Combos.find("12")!!
        show(state().copy(player = player.copy(queued = combo)))
        composeRule.onNodeWithTag("combat_queued").assertIsDisplayed()
        composeRule.onNodeWithText("En cola").assertExists()
        composeRule.onNodeWithText("Lanza I").assertExists()
    }

    @Test fun noQueuedLabelWhenEmpty() {
        show(state())
        composeRule.onAllNodesWithTag("combat_queued").assertCountEquals(0)
    }
}
