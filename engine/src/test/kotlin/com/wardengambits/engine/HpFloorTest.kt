package com.wardengambits.engine

import com.wardengambits.engine.action.CombatAction
import com.wardengambits.engine.ai.PatternBrain
import com.wardengambits.engine.combo.Combos
import com.wardengambits.engine.event.CombatEvent
import com.wardengambits.engine.model.CombatSetup
import com.wardengambits.engine.model.CombatState
import com.wardengambits.engine.model.FighterSpec
import com.wardengambits.engine.model.PLAYER_ID
import com.wardengambits.engine.model.RivalSpec
import com.wardengambits.engine.rules.CombatEngine
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class HpFloorTest {
    private val engine = CombatEngine()
    private val damageScript = listOf("12", "121", "13", "3123", "32", "212", "31232")

    private fun floorOf(maxHp: Int, pct: Int) = maxOf(1, maxHp * pct / 100)

    private fun strongRivals(count: Int) = List(count) { index ->
        RivalSpec("Rival ${index + 1}", 10, PatternBrain(listOf("12", "121", "3123", "13", "32")))
    }

    /** Runs [ticks] ticks, the player casting [script] when it can pay; returns the final state and all events. */
    private fun run(setup: CombatSetup, ticks: Int, script: List<String>, check: (CombatState) -> Unit = {}): Pair<CombatState, List<CombatEvent>> {
        var state = engine.start(setup, 5L)
        val events = mutableListOf<CombatEvent>()
        var next = 0
        repeat(ticks) {
            if (state.outcome != null) return@repeat
            if (script.isNotEmpty()) {
                val combo = Combos.find(script[next % script.size])!!
                if (state.player.power >= combo.cost && state.player.queued == null && state.player.isAlive) {
                    state = engine.press(state, PLAYER_ID, combo.sequence)
                    state = engine.reduce(state, CombatAction.Execute(PLAYER_ID)).state
                    next++
                }
            }
            val step = engine.reduce(state, CombatAction.Tick)
            state = step.state
            events += step.events
            check(state)
        }
        return state to events
    }

    private fun noEnd(events: List<CombatEvent>) {
        assertFalse(events.any { it is CombatEvent.FighterDefeated }, "no FighterDefeated expected")
        assertFalse(events.any { it is CombatEvent.CombatEnded }, "no CombatEnded expected")
    }

    @Test fun playerNeverDropsBelowFloor() {
        val setup = CombatSetup(FighterSpec("Player", 1), strongRivals(2), hpFloorPct = 10)
        val floor = floorOf(engine.start(setup, 1L).player.stats.maxHp, 10)
        var minHp = Int.MAX_VALUE
        val (state, events) = run(setup, 200, emptyList()) { minHp = minOf(minHp, it.player.hp) }
        assertTrue(minHp >= floor, "min hp $minHp < floor $floor")
        assertEquals(floor, state.player.hp)
        assertNull(state.outcome)
        noEnd(events)
    }

    @Test fun rivalsNeverDropBelowFloor() {
        val setup = CombatSetup(
            FighterSpec("Player", 10),
            listOf(RivalSpec("Dummy", 1, NoopBrain()), RivalSpec("Dummy 2", 1, NoopBrain())),
            hpFloorPct = 10,
        )
        val start = engine.start(setup, 1L)
        val floors = start.rivals.map { floorOf(it.stats.maxHp, 10) }
        val mins = IntArray(2) { Int.MAX_VALUE }
        val (state, events) = run(setup, 200, damageScript) {
            it.rivals.forEachIndexed { i, r -> mins[i] = minOf(mins[i], r.hp) }
        }
        mins.forEachIndexed { i, m -> assertTrue(m >= floors[i], "rival $i min $m < ${floors[i]}") }
        assertTrue(state.rivals.all { it.isAlive })
        assertNull(state.outcome)
        noEnd(events)
    }

    @Test fun floorHoldsUnderStackedDamage() {
        val setup = CombatSetup(FighterSpec("Player", 1), strongRivals(4), hpFloorPct = 5)
        val floor = floorOf(engine.start(setup, 1L).player.stats.maxHp, 5)
        var minHp = Int.MAX_VALUE
        val (_, events) = run(setup, 300, emptyList()) { minHp = minOf(minHp, it.player.hp) }
        assertTrue(minHp >= floor, "min hp $minHp < floor $floor")
        noEnd(events)
    }

    @Test fun noFloorByDefault() {
        val setup = CombatSetup(FighterSpec("Player", 1), strongRivals(2))
        val (state, events) = run(setup, 200, emptyList())
        assertNotNull(state.outcome)
        assertTrue(events.any { it is CombatEvent.CombatEnded })
    }
}
