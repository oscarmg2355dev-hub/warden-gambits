package com.wardengambits.app.ui.combat

import androidx.activity.ComponentActivity
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.assertContentDescriptionEquals
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.getBoundsInRoot
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.DpRect
import androidx.compose.ui.unit.height
import androidx.compose.ui.unit.width
import com.wardengambits.app.ui.theme.WardenTheme
import com.wardengambits.engine.combo.Combos
import com.wardengambits.engine.combo.Input
import com.wardengambits.app.ui.common.EffectBadgeUi
import com.wardengambits.engine.model.FighterId
import com.wardengambits.engine.model.PLAYER_ID
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.random.Random
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/** No-break space between a stat label and its number, so a wrapped stats line never splits them. */
private const val NBSP = ' '

/** Combat v2 layout (spec §4): fixed-size zones whatever the rivals and effects. */
@RunWith(RobolectricTestRunner::class)
@Config(qualifiers = "es-w360dp-h640dp")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class CombatLayoutTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private fun rival(id: Int, name: String = "Orco $id", boss: Boolean = false, badges: List<EffectBadgeUi> = emptyList()) =
        FighterUi(
            FighterId(id), name, 4, 40, 50, 10, 20, badges, isAlive = true, isTarget = id == 1,
            attack = 12, defense = 7, heal = 5, isBoss = boss,
        )

    private val player = FighterUi(
        PLAYER_ID, "Warden", 3, 80, 100, 30, 60, emptyList(), true, false,
        attack = 21, defense = 9, heal = 14,
    )

    private fun state(rivals: List<FighterUi> = listOf(rival(1)), buffer: String = "", p: FighterUi = player) = CombatUiState(
        zoneName = "Bree", levelLabel = "3/7", tick = 5, paused = false, rivals = rivals, player = p,
        buffer = buffer, bufferMatch = Combos.find(buffer), outcome = null, levelBefore = 3, winsBefore = 2,
    )

    private val holder = mutableStateOf(state())

    private fun show(
        initial: CombatUiState,
        onPress: (Input) -> Unit = {},
        onExecute: () -> Unit = {},
        floatingNumbers: FloatingNumbersState = FloatingNumbersState(),
    ) {
        holder.value = initial
        composeRule.setContent {
            WardenTheme {
                CombatScreen(
                    holder.value, onPress, onExecute, {}, {}, {}, {}, {}, floatingNumbers = floatingNumbers,
                )
            }
        }
    }

    private fun bounds(tag: String): DpRect = composeRule.onNodeWithTag(tag).getBoundsInRoot()

    @Test fun fourRivalCellsWithSameBoundsForOneAndFourRivals() {
        show(state(rivals = listOf(rival(1))))
        composeRule.onAllNodesWithTag("rival_slot_0").assertCountEquals(1)
        (1..3).forEach { composeRule.onNodeWithTag("rival_empty_$it").assertExists() }
        val one = (0..3).map { bounds("rival_cell_$it") }
        val playerOne = bounds("player_card")

        holder.value = state(rivals = (1..4).map { rival(it) })
        composeRule.waitForIdle()
        (0..3).forEach { composeRule.onNodeWithTag("rival_slot_$it").assertExists() }
        val four = (0..3).map { bounds("rival_cell_$it") }
        assertEquals(one, four)
        assertEquals(playerOne, bounds("player_card"))
        // the four cells are the same size (weights may differ by one pixel)
        four.forEach {
            assertEquals(four[0].width.value, it.width.value, 1f)
            assertEquals(four[0].height.value, it.height.value, 1f)
        }
    }

    @Test fun cardsKeepTheirSizeWithManyEffects() {
        show(state())
        val playerBefore = bounds("player_card")
        val rivalBefore = bounds("rival_slot_0")
        val all = (1..12).map { EffectBadgeUi("SP$it", it % 2 == 0, 0.5f) }
        holder.value = state(rivals = listOf(rival(1, badges = all)), p = player.copy(badges = all))
        composeRule.waitForIdle()
        assertEquals(playerBefore, bounds("player_card"))
        assertEquals(rivalBefore, bounds("rival_slot_0"))
    }

    @Test fun buttonRowHasFourEqualKeys() {
        val pressed = mutableListOf<Input>()
        var executes = 0
        show(state(), onPress = { pressed += it }, onExecute = { executes++ })
        composeRule.onNodeWithTag("btn_spear").assertExists()
        composeRule.onNodeWithTag("btn_spear").assertTextEquals("1")
        composeRule.onNodeWithTag("btn_shield").assertTextEquals("2")
        composeRule.onNodeWithTag("btn_fist").assertTextEquals("3")
        composeRule.onNodeWithTag("btn_execute").assertContentDescriptionEquals("Acción")
        val widths = listOf("btn_spear", "btn_shield", "btn_fist", "btn_execute").map { bounds(it).width }
        widths.forEach { assertEquals("widths $widths", widths[0].value, it.value, 1f) }
        assertEquals(72f, bounds("btn_execute").height.value, 0.5f)
        composeRule.onNodeWithTag("btn_spear").performClick()
        composeRule.onNodeWithTag("btn_shield").performClick()
        composeRule.onNodeWithTag("btn_fist").performClick()
        composeRule.onNodeWithTag("btn_execute").performClick()
        assertEquals(listOf(Input.SPEAR, Input.SHIELD, Input.FIST), pressed)
        assertEquals(1, executes)
        // the clear key lives at the end of the combo row
        assertTrue(bounds("btn_clear").top >= bounds("combo_row").top)
        assertTrue(bounds("btn_clear").bottom <= bounds("combo_row").bottom)
    }

    @Test fun comboRowShowsNameChainAndCost() {
        val match = Combos.find("121")!!
        show(state(buffer = "121").copy(chainContinues = true))
        composeRule.onNode(hasText("Lanza II ⛓") and hasAnyAncestor(hasTestTag("combo_row"))).assertExists()
        composeRule.onNode(hasText("${match.cost} power") and hasAnyAncestor(hasTestTag("combo_row"))).assertExists()
        composeRule.onAllNodes(hasText("1") and hasAnyAncestor(hasTestTag("combo_row"))).assertCountEquals(2)
    }

    @Test fun comboRowShowsDashForUnknownSequence() {
        show(state(buffer = "333"))
        composeRule.onNode(hasText("—") and hasAnyAncestor(hasTestTag("combo_row"))).assertExists()
    }

    @Test fun comboRowShowsQueuedWhenBufferEmpty() {
        val combo = Combos.find("12")!!
        show(state(p = player.copy(queued = combo)))
        composeRule.onNodeWithTag("combat_queued").assertExists()
        composeRule.onNode(hasText("En cola") and hasAnyAncestor(hasTestTag("combo_row"))).assertExists()
        composeRule.onNode(hasText("2") and hasAnyAncestor(hasTestTag("combat_queued"))).assertExists()
        composeRule.onNode(hasText("Lanza I") and hasAnyAncestor(hasTestTag("combo_row"))).assertExists()
    }

    @Test fun cardsShowStatsAndBossCrown() {
        show(state(rivals = listOf(rival(1), rival(2, name = "Rey Brujo", boss = true))))
        composeRule.onAllNodesWithText("Nv${NBSP}4 · ATQ${NBSP}12 · DEF${NBSP}7").assertCountEquals(2)
        composeRule.onNodeWithText("ATQ${NBSP}21 · CUR${NBSP}14 · DEF${NBSP}9").assertExists()
        composeRule.onAllNodesWithText("♛").assertCountEquals(1)
        composeRule.onNode(hasText("♛") and hasAnyAncestor(hasTestTag("rival_slot_1")), useUnmergedTree = true).assertExists()
    }

    @Test fun lateGameNumbersFitTheCards() {
        val boss = LateGameNumbers.boss
        val hero = LateGameNumbers.player
        val bossUi = FighterUi(
            FighterId(1), "Jefe de los arqueros orcos", 46, boss.maxHp, boss.maxHp, boss.maxPower, boss.maxPower,
            emptyList(), isAlive = true, isTarget = true, attack = boss.attack, defense = boss.defense, heal = boss.heal, isBoss = true,
        )
        val heroUi = player.copy(
            level = LateGameNumbers.PLAYER_LEVEL, hp = hero.maxHp, maxHp = hero.maxHp, power = hero.maxPower, maxPower = hero.maxPower,
            attack = hero.attack, defense = hero.defense, heal = hero.heal,
        )
        show(state(rivals = (1..4).map { if (it == 1) bossUi else rival(it) }, p = heroUi))
        fun inCard(text: String, card: String) =
            composeRule.onNode(hasText(text) and hasAnyAncestor(hasTestTag(card)), useUnmergedTree = true)
        // (text, card, lines it may take): the stats lines wrap after "·" when they do not fit on one line
        listOf(
            Triple("${boss.maxHp}/${boss.maxHp}", "rival_slot_0", 1),
            Triple("${boss.maxPower}/${boss.maxPower}", "rival_slot_0", 1),
            Triple("Nv${NBSP}46 · ATQ$NBSP${boss.attack} · DEF$NBSP${boss.defense}", "rival_slot_0", 3),
            Triple("Jefe de los arqueros orcos", "rival_slot_0", 2),
            Triple("${hero.maxHp}/${hero.maxHp}", "player_card", 1),
            Triple("ATQ$NBSP${hero.attack} · CUR$NBSP${hero.heal} · DEF$NBSP${hero.defense}", "player_card", 2),
        ).forEach { (text, card, lines) ->
            val node = inCard(text, card)
            assertTrue("'$text' needs ${node.textWidthPx()} px of ${node.availableWidthPx()} in $card", node.fitsIn(lines))
        }
    }

    @Test fun floatingNumbersGoToTheCardOfTheirFighter() {
        show(
            state(rivals = listOf(rival(1), rival(2))),
            floatingNumbers = FloatingNumbersState().add(
                listOf(CombatTextLine(FighterId(2), "−7 SP1", CombatTextTone.DAMAGE), CombatTextLine(PLAYER_ID, "+5 CU1", CombatTextTone.HEAL)),
                { 50 }, 0L, Random(1),
            ),
        )
        composeRule.onNode(hasText("−7 SP1") and hasAnyAncestor(hasTestTag("rival_slot_1")), useUnmergedTree = true).assertExists()
        composeRule.onNode(hasText("−7 SP1") and hasAnyAncestor(hasTestTag("rival_slot_0")), useUnmergedTree = true).assertDoesNotExist()
        composeRule.onNode(hasText("+5 CU1") and hasAnyAncestor(hasTestTag("player_card")), useUnmergedTree = true).assertExists()
    }
}
