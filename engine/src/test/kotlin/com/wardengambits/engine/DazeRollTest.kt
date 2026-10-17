package com.wardengambits.engine

import com.wardengambits.engine.action.CombatAction
import com.wardengambits.engine.combo.ComboTuning
import com.wardengambits.engine.event.CombatEvent
import com.wardengambits.engine.model.ActiveEffect
import com.wardengambits.engine.model.CombatState
import com.wardengambits.engine.model.EffectKind
import com.wardengambits.engine.model.FighterId
import com.wardengambits.engine.model.PLAYER_ID
import com.wardengambits.engine.model.RivalScaling
import com.wardengambits.engine.random.GameRandom
import com.wardengambits.engine.rules.CombatEngine
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/** Returns [rolls] in order and records every bound it was asked for (ticks use [ComboTuning.FIXED]: only Daze rolls). */
private class CountingRandom(private vararg val rolls: Int) : GameRandom {
    val bounds = mutableListOf<Int>()

    override val state: Long = 0L

    override fun nextInt(bound: Int): Int {
        val roll = rolls[bounds.size % rolls.size]
        bounds += bound
        return roll
    }
}

class DazeRollTest {
    private val r1 = FighterId(1)
    private val r2 = FighterId(2)

    private fun chance(pct: Int, combo: String = "31", source: FighterId = PLAYER_ID) =
        ActiveEffect(EffectKind.DAZE_CHANCE, source, combo, pct, 10, activeFrom = 1)

    private fun tick(state: CombatState, random: CountingRandom, tuning: ComboTuning = ComboTuning.FIXED) =
        CombatEngine(RivalScaling.IDENTITY, tuning = tuning) { random }.reduce(state, CombatAction.Tick)

    private fun started(vararg rivalLevels: Int = intArrayOf(1)): CombatState =
        CombatEngine(RivalScaling.IDENTITY).start(setup(1, *rivalLevels), 1L)

    private fun withChances(vararg chances: ActiveEffect, id: FighterId = r1) =
        started().withFighter(id) { copy(effects = chances.toList()) }

    @Test fun rollBelowChanceDazesForTenTicksFromNextTick() {
        val random = CountingRandom(4)
        val result = tick(withChances(chance(5)), random)
        val rival = result.state.fighter(r1)
        assertEquals(listOf(100), random.bounds)
        val dazed = rival.effects.single()
        assertEquals(EffectKind.DAZED, dazed.kind)
        assertEquals(10, dazed.totalTicks)
        assertEquals(2, dazed.activeFrom)
        assertEquals(10, dazed.remainingTicks)
        assertFalse(rival.isActive(EffectKind.DAZED, 1))
        assertTrue(CombatEvent.EffectApplied(r1, EffectKind.DAZED, 10) in result.events)
    }

    @Test fun enteringDazeRemovesEveryDazeChance() {
        val result = tick(withChances(chance(5), chance(10, "3131"), chance(5, "31", r2)), CountingRandom(0))
        assertTrue(result.state.fighter(r1).effects.none { it.kind == EffectKind.DAZE_CHANCE })
    }

    @Test fun rollAtChanceDoesNothing() {
        val result = tick(withChances(chance(5)), CountingRandom(5))
        assertFalse(result.state.fighter(r1).has(EffectKind.DAZED))
        assertEquals(1, result.state.fighter(r1).effects.count { it.kind == EffectKind.DAZE_CHANCE })
        assertTrue(result.events.none { it is CombatEvent.EffectApplied })
    }

    @Test fun chanceIsSummedAndCappedAt55() {
        val sum = withChances(chance(10), chance(5, "313"), chance(10, "3131"))
        assertTrue(tick(sum, CountingRandom(24)).state.fighter(r1).has(EffectKind.DAZED))
        assertFalse(tick(sum, CountingRandom(25)).state.fighter(r1).has(EffectKind.DAZED))

        val capped = withChances(chance(35, "31313"), chance(35, "31313", r2))
        assertTrue(tick(capped, CountingRandom(54)).state.fighter(r1).has(EffectKind.DAZED))
        assertFalse(tick(capped, CountingRandom(55)).state.fighter(r1).has(EffectKind.DAZED))
    }

    @Test fun noDrawWithoutChance() {
        val random = CountingRandom(0)
        tick(started(1, 1), random)
        assertEquals(emptyList(), random.bounds)
    }

    @Test fun rollsInFighterIndexOrder() {
        val state = started(1, 1)
            .withFighter(PLAYER_ID) { copy(effects = listOf(chance(5, source = r1))) }
            .withFighter(r2) { copy(effects = listOf(chance(5))) }
        val random = CountingRandom(0, 99)
        val result = tick(state, random)
        assertEquals(listOf(100, 100), random.bounds)
        assertTrue(result.state.player.has(EffectKind.DAZED))
        assertFalse(result.state.fighter(r2).has(EffectKind.DAZED))
        assertFalse(result.state.fighter(r1).has(EffectKind.DAZED))
    }

    /** User decision (descripcion.md «impide entrar en modo daze»): Escudo táctico gives full Daze immunity by default. */
    @Test fun tacticalShieldGivesImmunityByDefault() {
        assertEquals(0, ComboTuning.DEFAULT.tacticsDazePct)
        assertEquals(0, ComboTuning.FIXED.tacticsDazePct)
        val shield = ActiveEffect(EffectKind.SHIELD_TACTICS, r1, "2131", 0, 10, activeFrom = 1)
        val random = CountingRandom(0)
        val result = tick(withChances(chance(55, "31313"), shield), random)
        assertEquals(emptyList(), random.bounds)
        assertFalse(result.state.fighter(r1).has(EffectKind.DAZED))
    }

    /**
     * Chance stored before the shield went up does not roll while it is active; as Aturdir stores nothing on a shielded
     * target (ComboApplierTest), no chance gained under the shield is left to roll in full when it expires.
     */
    @Test fun noChanceGainedUnderImmunityRollsAfterTheShieldExpires() {
        val engine = CombatEngine(RivalScaling.IDENTITY, tuning = ComboTuning.FIXED) { CountingRandom(0) }
        val shield = ActiveEffect(EffectKind.SHIELD_TACTICS, r1, "2131", 0, 3, activeFrom = 1)
        var state = withChances(shield).withFighter(PLAYER_ID) { copy(power = stats.maxPower) }
        state = engine.reduce(engine.press(state, PLAYER_ID, "31"), CombatAction.Execute(PLAYER_ID)).state
        repeat(5) { state = engine.reduce(state, CombatAction.Tick).state }
        val rival = state.fighter(r1)
        assertFalse(rival.has(EffectKind.SHIELD_TACTICS))
        assertFalse(rival.has(EffectKind.DAZED))
        assertTrue(rival.effects.none { it.kind == EffectKind.DAZE_CHANCE })
    }

    /** With tacticsDazePct > 0 the shield only scales the roll (the rival-healing rule, kept as a tuning option). */
    @Test fun partialTacticalShieldScalesDazeChance() {
        val half = ComboTuning.FIXED.copy(tacticsDazePct = 50)
        val shield = ActiveEffect(EffectKind.SHIELD_TACTICS, r1, "2131", 0, 10, activeFrom = 1)
        // 30 % chance x 50 % = 15 %.
        val hit = CountingRandom(14)
        assertTrue(tick(withChances(chance(30), shield), hit, half).state.fighter(r1).has(EffectKind.DAZED))
        assertEquals(listOf(100), hit.bounds)
        assertFalse(tick(withChances(chance(30), shield), CountingRandom(15), half).state.fighter(r1).has(EffectKind.DAZED))
    }

    @Test fun alreadyDazedFighterDoesNotRoll() {
        val dazed = ActiveEffect(EffectKind.DAZED, r1, "", 0, 5, activeFrom = 1)
        val random = CountingRandom(0)
        val result = tick(withChances(chance(30), dazed), random)
        assertEquals(emptyList(), random.bounds)
        assertEquals(4, result.state.fighter(r1).effects.single { it.kind == EffectKind.DAZED }.remainingTicks)
    }

    @Test fun deadFighterDoesNotRoll() {
        val state = withChances(chance(30)).withFighter(r1) {
            copy(hp = 1, effects = effects + ActiveEffect(EffectKind.DOT, PLAYER_ID, "12", 5, 10))
        }
        val random = CountingRandom(0)
        tick(state, random)
        assertEquals(emptyList(), random.bounds)
    }
}
