package com.wardengambits.app.ui.combat

import androidx.activity.ComponentActivity
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import com.wardengambits.app.ui.theme.WardenTheme
import com.wardengambits.engine.combo.Combos
import com.wardengambits.engine.event.CombatEvent
import com.wardengambits.engine.model.EffectKind
import com.wardengambits.engine.model.FighterId
import com.wardengambits.engine.model.PLAYER_ID
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/** A number appears over the right rival's card, rises and disappears; a paused fight freezes it. */
@RunWith(RobolectricTestRunner::class)
@Config(qualifiers = "es-w360dp-h640dp")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class FloatingNumbersScreenTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private fun rival(id: Int) = FighterUi(FighterId(id), "Orco $id", 2, 40, 50, 10, 20, emptyList(), isAlive = true, isTarget = id == 1)

    private val player = FighterUi(PLAYER_ID, "Warden", 3, 80, 100, 30, 60, emptyList(), true, false)

    private fun state(paused: Boolean) = CombatUiState(
        zoneName = "Bree", levelLabel = "3/7", tick = 5, paused = paused, rivals = listOf(rival(1), rival(2)), player = player,
        buffer = "", bufferMatch = Combos.find(""), outcome = null, levelBefore = 3, winsBefore = 2,
    )

    private val number = hasText("−4 SP2") and hasAnyAncestor(hasTestTag("rival_slot_1"))

    private fun show(paused: Boolean) {
        composeRule.mainClock.autoAdvance = false
        composeRule.setContent {
            WardenTheme {
                val numbers = rememberFloatingNumbers(running = !paused)
                LaunchedEffect(Unit) {
                    combatTextFor(CombatEvent.EffectResolved(FighterId(2), PLAYER_ID, "121", EffectKind.DOT, -4))
                        ?.let { numbers.add(listOf(it), { 50 }) }
                }
                CombatScreen(
                    state(paused), {}, {}, {}, {}, {}, {}, {},
                    floatingNumbers = numbers.state, numbersClock = numbers::now,
                )
            }
        }
        composeRule.mainClock.advanceTimeBy(100)
    }

    @Test fun numberAppearsOverTheRivalCardAndThenDisappears() {
        show(paused = false)
        composeRule.onNode(number, useUnmergedTree = true).assertExists()
        composeRule.mainClock.advanceTimeBy(700)
        composeRule.onNode(number, useUnmergedTree = true).assertExists()
        composeRule.mainClock.advanceTimeBy(1000)
        composeRule.onNode(number, useUnmergedTree = true).assertDoesNotExist()
    }

    @Test fun pausedFightFreezesTheNumbers() {
        show(paused = true)
        composeRule.mainClock.advanceTimeBy(3000)
        composeRule.onNode(number, useUnmergedTree = true).assertExists()
    }
}
