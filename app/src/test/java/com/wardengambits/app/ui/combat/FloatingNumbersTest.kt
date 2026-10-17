package com.wardengambits.app.ui.combat

import com.wardengambits.engine.event.CombatEvent
import com.wardengambits.engine.model.FighterId
import com.wardengambits.engine.model.PLAYER_ID
import kotlin.random.Random
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FloatingNumbersTest {
    private val r1 = FighterId(1)
    private val max = { _: FighterId -> 10000 }

    private fun line(target: FighterId = r1, text: String = "−1 SP1", amount: Int = -1, rollPct: Int = 100) =
        CombatTextLine(target, text, CombatTextTone.DAMAGE, amount, rollPct)

    @Test fun sizeIsMinForZeroAndMaxAtFifteenPercent() {
        assertEquals(13f, floatingNumberSize(0, 10000), 0f)
        assertEquals(36f, floatingNumberSize(1500, 10000), 0f)
        assertEquals(36f, floatingNumberSize(5000, 10000), 0f)
    }

    @Test fun sizeIsMonotonic() {
        val sizes = (0..2000 step 100).map { floatingNumberSize(it, 10000) }
        sizes.zipWithNext().forEach { (a, b) -> assertTrue(a <= b) }
    }

    @Test fun strongOnlyFromRoll130() {
        val state = FloatingNumbersState().add(listOf(line(rollPct = 129), line(rollPct = 130)), max, 0L, Random(1))
        assertEquals(listOf(false, true), state.forTarget(r1).map { it.strong })
    }

    @Test fun limitsEightPerCard() {
        val state = FloatingNumbersState().add(List(12) { line(text = "−$it") }, max, 0L, Random(1))
        assertEquals((4..11).map { "−$it" }, state.forTarget(r1).map { it.text })
    }

    @Test fun positionsStayInsideTheirCell() {
        var state = FloatingNumbersState(perCardLimit = 200)
        state = state.add(List(200) { line() }, max, 0L, Random(1), gridOf = { 3 to 2 })
        val numbers = state.forTarget(r1)
        assertEquals(200, numbers.size)
        numbers.forEach { n ->
            val col = n.cell % 3
            val row = n.cell / 3
            assertTrue("$n", n.cx in (col + 0.25f) / 3f..(col + 0.75f) / 3f)
            assertTrue("$n", n.cy in (row + 0.25f) / 2f..(row + 0.75f) / 2f)
        }
    }

    @Test fun sixNumbersInOneBatchUseSixCells() {
        val state = FloatingNumbersState().add(List(6) { line() }, max, 0L, Random(1), gridOf = { 3 to 2 })
        assertEquals(6, state.forTarget(r1).map { it.cell }.toSet().size)
    }

    @Test fun fullGridFallsBackToLeastUsed() {
        // Every cell taken once and cells 0 and 1 twice: the next number goes to one of the cells used once.
        val occupied = listOf(0, 1, 2, 3, 4, 5, 0, 1)
        repeat(50) { seed -> assertTrue(stageCell(3, 2, occupied, Random(seed)) in 2..5) }
        // Over many additions to a full grid, every number takes one of the least used cells at that moment.
        val cells = FloatingNumbersState(perCardLimit = 40)
            .add(List(40) { line() }, max, 0L, Random(1), gridOf = { 3 to 2 })
            .forTarget(r1).map { it.cell }
        cells.forEachIndexed { i, cell ->
            val counts = IntArray(6).also { c -> cells.take(i).forEach { c[it]++ } }
            assertEquals("number $i in cell $cell with ${counts.toList()}", counts.min(), counts[cell])
        }
    }

    @Test fun eachCardUsesItsOwnGrid() {
        val state = FloatingNumbersState().add(List(8) { line(target = PLAYER_ID) }, max, 0L, Random(1), gridOf = { 4 to 2 })
        assertEquals((0..7).toSet(), state.forTarget(PLAYER_ID).map { it.cell }.toSet())
        assertEquals(4 to 2, floatingNumberGrid(PLAYER_ID))
        assertEquals(3 to 2, floatingNumberGrid(r1))
    }

    @Test fun alphaIsOpaqueUntilSeventyPercent() {
        assertEquals(1f, floatingNumberAlpha(0.69f), 0f)
        assertEquals(1f, floatingNumberAlpha(0.70f), 0f)
        assertEquals(0.5f, floatingNumberAlpha(0.85f), 0.001f)
        assertEquals(0f, floatingNumberAlpha(1f), 0f)
    }

    @Test fun riseHappensWhileFading() {
        assertEquals(0f, floatingNumberRise(0.69f), 0f)
        assertEquals(13f, floatingNumberRise(0.85f), 0.01f)
        assertEquals(26f, floatingNumberRise(1f), 0f)
    }

    @Test fun pausedClockFreezesNumbers() {
        val controller = FloatingNumbersController()
        controller.add(listOf(line()), max, Random(1))
        assertEquals(1, controller.state.forTarget(r1).size)
        assertEquals(1, controller.state.prune(controller.now()).forTarget(r1).size)
        controller.advance(FLOATING_NUMBER_LIFE_MILLIS - 1)
        assertEquals(1, controller.state.forTarget(r1).size)
        controller.advance(1)
        assertEquals(0, controller.state.forTarget(r1).size)
    }

    @Test fun defeatedTargetKeepsLiveNumbers() {
        val state = FloatingNumbersState().add(listOf(line()), max, 0L, Random(1))
        assertEquals(null, combatTextFor(CombatEvent.FighterDefeated(r1)))
        assertEquals(1, state.prune(FLOATING_NUMBER_LIFE_MILLIS - 1).forTarget(r1).size)
        assertFalse(state.forTarget(PLAYER_ID).isNotEmpty())
        assertEquals(0, state.prune(FLOATING_NUMBER_LIFE_MILLIS).forTarget(r1).size)
    }

    @Test fun placementShrinksAndMovesInAWideNumber() {
        // 400 px of text in a 300 px card: shrunk so that its 1.25 peak is exactly the card's width.
        val p = floatingNumberPlacement(0.65f, 0.5f, 400f, 50f, 300f, 200f, maxRise = 30f)
        assertEquals(0.6f, p.fit, 0.0001f)
        val center = p.x + 200f
        assertEquals(0f, center - 400f * p.fit * 1.25f / 2f, 0.01f)
        assertEquals(300f, center + 400f * p.fit * 1.25f / 2f, 0.01f)
    }

    @Test fun placementKeepsANarrowNumberWhereItWasAsked() {
        val p = floatingNumberPlacement(0.3f, 0.5f, 60f, 20f, 300f, 200f, maxRise = 30f)
        assertEquals(1f, p.fit, 0f)
        assertEquals(90f, p.x, 0.001f)
        assertEquals(100f, p.y, 0.001f)
    }

    @Test fun placementLeavesRoomForThePeakAndTheRise() {
        val bottom = floatingNumberPlacement(0.65f, 0.8f, 100f, 40f, 300f, 100f, maxRise = 30f)
        assertTrue(bottom.y + 20f + 40f * 1.25f / 2f <= 100f + 0.001f)
        val top = floatingNumberPlacement(0.05f, 0.0f, 100f, 40f, 300f, 100f, maxRise = 30f)
        assertTrue(top.y - 30f >= -0.001f)
        assertTrue(top.y + 20f - 40f * 1.25f / 2f >= -0.001f)
    }

    @Test fun potionIsSizedAgainstMaxPowerAndHitsAgainstMaxHp() {
        val rival = FighterUi(r1, "Orco", 2, 900, 1000, 50, 300, emptyList(), isAlive = true, isTarget = true)
        val player = FighterUi(PLAYER_ID, "Warden", 3, 80, 100, 30, 60, emptyList(), true, false)
        val ui = CombatUiState("Bree", "1/7", 0, false, listOf(rival), player, "", null, outcome = null, levelBefore = 3, winsBefore = 0)
        assertEquals(300, maxVitalOf(ui, r1, CombatTextTone.POWER))
        assertEquals(1000, maxVitalOf(ui, r1, CombatTextTone.DAMAGE))
        assertEquals(100, maxVitalOf(ui, PLAYER_ID, CombatTextTone.HEAL))
        assertEquals(0, maxVitalOf(ui, FighterId(9), CombatTextTone.HEAL))
    }
}
