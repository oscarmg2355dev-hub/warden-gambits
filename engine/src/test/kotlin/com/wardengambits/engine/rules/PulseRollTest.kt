package com.wardengambits.engine.rules

import com.wardengambits.engine.action.CombatAction
import com.wardengambits.engine.ai.PatternBrain
import com.wardengambits.engine.combo.ComboTuning
import com.wardengambits.engine.combo.Combos
import com.wardengambits.engine.combo.TargetKind
import com.wardengambits.engine.event.CombatEvent
import com.wardengambits.engine.model.ActiveEffect
import com.wardengambits.engine.model.CombatSetup
import com.wardengambits.engine.model.CombatState
import com.wardengambits.engine.model.EffectKind
import com.wardengambits.engine.model.FighterId
import com.wardengambits.engine.model.FighterSpec
import com.wardengambits.engine.model.PLAYER_ID
import com.wardengambits.engine.model.RivalScaling
import com.wardengambits.engine.model.RivalSpec
import com.wardengambits.engine.press
import com.wardengambits.engine.random.GameRandom
import com.wardengambits.engine.random.SeededRandom
import com.wardengambits.engine.setup
import com.wardengambits.engine.withFighter
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.fail

/** Variable damage and heal per pulse (spec 2026-10-15 combat cards §1). */
class PulseRollTest {
    private val rival = FighterId(1)

    private object ThrowingRandom : GameRandom {
        override fun nextInt(bound: Int): Int = fail("the random must not be consumed")
        override val state: Long = 0L
    }

    /** Always returns [value] (clamped to the bound) and counts the calls. */
    private class StubRandom(private val value: Int) : GameRandom {
        var calls = 0
        override fun nextInt(bound: Int): Int {
            calls++
            return minOf(value, bound - 1)
        }
        override val state: Long = 0L
    }

    private fun queue(engine: CombatEngine, state: CombatState, seq: String): CombatState =
        engine.reduce(engine.press(state, PLAYER_ID, seq), CombatAction.Execute(PLAYER_ID)).state

    @Test fun rollStaysInRangeAndAveragesHundred() {
        val random = SeededRandom(7)
        val rolls = List(1000) { PulseRoll.roll(random, ComboTuning.DEFAULT) }
        assertTrue(rolls.all { it in 50..150 }, "all rolls in 50..150")
        assertTrue(rolls.average() in 95.0..105.0, "average ${rolls.average()}")
    }

    @Test fun fixedTuningNeverConsumesRandom() {
        assertEquals(100, PulseRoll.roll(ThrowingRandom, ComboTuning.FIXED))
    }

    @Test fun scaleRoundsAndKeepsMinimumOne() {
        assertEquals(5, PulseRoll.scale(10, 50))
        assertEquals(2, PulseRoll.scale(3, 50))
        assertEquals(1, PulseRoll.scale(1, 50))
        assertEquals(300, PulseRoll.scale(200, 150))
    }

    @Test fun overTimeAmountsVaryAcrossTicks() {
        val engine = CombatEngine(RivalScaling.IDENTITY, tuning = ComboTuning.DEFAULT)
        var state = queue(engine, engine.start(setup(1, 1), 11L), "121")
        val dots = mutableListOf<CombatEvent.EffectResolved>()
        repeat(3) {
            val result = engine.reduce(state, CombatAction.Tick)
            state = result.state
            dots += result.events.filterIsInstance<CombatEvent.EffectResolved>()
                .filter { it.target == rival && it.kind == EffectKind.DOT }
        }
        assertEquals(3, dots.size)
        assertTrue(dots.map { it.amount }.distinct().size > 1, "amounts ${dots.map { it.amount }}")
        assertTrue(dots.all { it.rollPct in 50..150 }, "rolls ${dots.map { it.rollPct }}")
    }

    @Test fun sameSeedSameEvents() {
        fun run(): List<CombatEvent> {
            val engine = CombatEngine()
            val combatSetup = CombatSetup(
                FighterSpec("Player", 3),
                listOf(RivalSpec("A", 3, PatternBrain(listOf("12", "3123", "31"))), RivalSpec("B", 3, PatternBrain(listOf("21", "123")))),
            )
            var state = engine.start(combatSetup, 42L)
            val script = listOf("121", "21", "3123", "123", "32")
            val events = mutableListOf<CombatEvent>()
            repeat(20) { i ->
                if (state.outcome == null && state.player.queued == null) state = queue(engine, state, script[i % script.size])
                val result = engine.reduce(state, CombatAction.Tick)
                state = result.state
                events += result.events
            }
            return events
        }
        assertEquals(run(), run())
    }

    @Test fun fixedTuningReproducesNominalValues() {
        val engine = CombatEngine(RivalScaling.IDENTITY, tuning = ComboTuning.FIXED)
        val state = queue(engine, engine.start(setup(1, 1), 1L), "12")
        val magnitude = DamageFormula.spearDot(1, DamageFormula.spearUnit(state.player.stats.attack, ComboTuning.FIXED), false)
        val dot = engine.reduce(state, CombatAction.Tick).events.filterIsInstance<CombatEvent.EffectResolved>()
            .single { it.target == rival && it.kind == EffectKind.DOT }
        assertEquals(-DamageFormula.received(magnitude, false, false), dot.amount)
        assertEquals(100, dot.rollPct)
    }

    @Test fun lethalPulseIsClampedToRemainingHp() {
        val tuning = ComboTuning.DEFAULT
        val random = StubRandom(tuning.rollMaxPct - tuning.rollMinPct)
        val engine = CombatEngine(RivalScaling.IDENTITY, tuning = tuning) { random }
        val state = engine.start(setup(1, 1), 1L).withFighter(rival) {
            copy(hp = 5, effects = listOf(ActiveEffect(EffectKind.DOT, PLAYER_ID, "12", 100, 5)))
        }
        val result = engine.reduce(state, CombatAction.Tick)
        assertEquals(0, result.state.fighter(rival).hp)
        val dot = result.events.filterIsInstance<CombatEvent.EffectResolved>().single { it.target == rival }
        assertEquals(-5, dot.amount)
        assertEquals(150, dot.rollPct)
    }

    @Test fun drainTakesAtMostRemainingHp() {
        val tuning = ComboTuning.DEFAULT
        val random = StubRandom(tuning.rollMaxPct - tuning.rollMinPct)
        val engine = CombatEngine(RivalScaling.IDENTITY, tuning = tuning) { random }
        // Cast on tick 6 (activeFrom 7): the next tick, 11, is a pulse.
        val state = engine.start(setup(1, 1), 1L).copy(tick = 10)
            .withFighter(PLAYER_ID) {
                copy(hp = 1_000, effects = listOf(ActiveEffect(EffectKind.DRAIN, PLAYER_ID, "3123", 50, 30, activeFrom = 7)))
            }
            .withFighter(rival) { copy(hp = 3) }
        val result = engine.reduce(state, CombatAction.Tick)
        assertEquals(0, result.state.fighter(rival).hp)
        val drains = result.events.filterIsInstance<CombatEvent.EffectResolved>().filter { it.kind == EffectKind.DRAIN }
        assertEquals(-3, drains.single { it.target == rival }.amount)
        val heal = drains.single { it.target == PLAYER_ID }
        assertTrue(heal.amount <= 3, "heal ${heal.amount}")
        assertEquals(100, heal.rollPct)
    }

    @Test fun shieldsDazeAndMultipliersDoNotRoll() {
        val start = CombatEngine(RivalScaling.IDENTITY).start(setup(1, 1), 1L).copy(tick = 5)
        listOf("2132", "31", "11").forEach { seq ->
            val combo = Combos.find(seq)!!
            val targets = if (combo.target == TargetKind.SELF) listOf(PLAYER_ID) else listOf(rival)
            val counting = StubRandom(0)
            val varied = ComboApplier.apply(start, PLAYER_ID, targets, combo, false, ComboTuning.DEFAULT, counting)
            val fixed = ComboApplier.apply(start, PLAYER_ID, targets, combo, false, ComboTuning.FIXED, ThrowingRandom)
            assertEquals(0, counting.calls, "$seq consumed the random")
            assertEquals(fixed.events, varied.events, seq)
            assertEquals(fixed.state.fighters.map { it.effects }, varied.state.fighters.map { it.effects }, seq)
        }
    }
}
