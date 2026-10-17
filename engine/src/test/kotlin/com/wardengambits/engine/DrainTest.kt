package com.wardengambits.engine

import com.wardengambits.engine.action.CombatAction
import com.wardengambits.engine.combo.Combos
import com.wardengambits.engine.event.CombatEvent
import com.wardengambits.engine.model.CombatState
import com.wardengambits.engine.model.EffectKind
import com.wardengambits.engine.model.FighterId
import com.wardengambits.engine.model.PLAYER_ID
import com.wardengambits.engine.model.RivalScaling
import com.wardengambits.engine.rules.CombatEngine
import com.wardengambits.engine.rules.StepResult
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/** Level 1 player with [TEST_TUNING]: CUR = 800 × 25 / 100 = 200 per rival and pulse (max HP 10000). */
class DrainTest {
    private val engine = CombatEngine(RivalScaling.IDENTITY, tuning = TEST_TUNING) { FixedRandom(99) }
    private val r1 = FighterId(1)
    private val r2 = FighterId(2)

    private fun step(state: CombatState, seq: String? = null): StepResult {
        val queued = if (seq == null) state else engine.reduce(engine.press(state, PLAYER_ID, seq), CombatAction.Execute(PLAYER_ID)).state
        return engine.reduce(queued, CombatAction.Tick)
    }

    private fun drained(target: FighterId, amount: Int, combo: String = "3123") =
        CombatEvent.EffectResolved(target, PLAYER_ID, combo, EffectKind.DRAIN, amount)

    private fun wounded(): CombatState = engine.start(setup(1, 1, 1), 1L)
        .withFighter(PLAYER_ID) { copy(hp = 5_000) }
        .withFighter(r1) { copy(hp = 5_000) }
        .withFighter(r2) { copy(hp = 5_000) }

    @Test fun pulsesSixTimesEveryFiveTicks() {
        var r = step(wounded(), "3123")
        val pulseTicks = mutableListOf<Int>()
        repeat(35) {
            r = step(r.state)
            if (r.events.any { it is CombatEvent.EffectResolved && it.target == PLAYER_ID }) pulseTicks += r.state.tick
        }
        assertEquals(listOf(6, 11, 16, 21, 26, 31), pulseTicks)
        assertEquals(3_800, r.state.fighter(r1).hp)
        assertEquals(3_800, r.state.fighter(r2).hp)
        assertEquals(7_400, r.state.player.hp)
        assertFalse(r.state.player.has(EffectKind.DRAIN))
    }

    @Test fun pulseEvents() {
        var r = step(wounded(), "3123")
        repeat(5) { r = step(r.state) }
        assertEquals(6, r.state.tick)
        assertEquals(
            listOf(
                drained(r1, -200),
                drained(r2, -200),
                drained(PLAYER_ID, 400),
            ),
            r.events.filterIsInstance<CombatEvent.EffectResolved>(),
        )
    }

    @Test fun pulseCappedByRivalHpAndCasterMaxHp() {
        val state = wounded()
            .withFighter(PLAYER_ID) { copy(hp = 9_900) }
            .withFighter(r1) { copy(hp = 100) }
        var r = step(state, "3123")
        repeat(5) { r = step(r.state) }
        assertEquals(0, r.state.fighter(r1).hp)
        assertEquals(4_800, r.state.fighter(r2).hp)
        assertEquals(10_000, r.state.player.hp)
        assertTrue(drained(r1, -100) in r.events)
        assertTrue(CombatEvent.FighterDefeated(r1) in r.events)
        assertTrue(drained(PLAYER_ID, 100) in r.events)
    }

    @Test fun chainedTierTwoDoublesDrain() {
        var r = step(wounded(), "3123")
        r = step(r.state, "31232")
        val drains = r.state.player.effects.filter { it.kind == EffectKind.DRAIN }
        assertEquals(mapOf("3123" to 200, "31232" to 400), drains.associate { it.sourceCombo to it.magnitude })
        repeat(5) { r = step(r.state) }
        assertEquals(7, r.state.tick)
        assertTrue(drained(r1, -400, "31232") in r.events)
        assertTrue(drained(PLAYER_ID, 800, "31232") in r.events)
    }

    @Test fun unchainedTierTwoDrainsOnce() {
        var r = step(wounded(), "31232")
        assertEquals(200, r.state.player.effects.single { it.kind == EffectKind.DRAIN }.magnitude)
        repeat(5) { r = step(r.state) }
        assertTrue(drained(r1, -200, "31232") in r.events)
    }

    @Test fun rivalDrainsThePlayer() {
        val state = wounded().withFighter(r1) { copy(queued = Combos.find("3123")) }
        var r = step(state)
        repeat(5) { r = step(r.state) }
        assertEquals(4_800, r.state.player.hp)
        assertEquals(5_200, r.state.fighter(r1).hp)
        assertEquals(5_000, r.state.fighter(r2).hp)
    }
}
