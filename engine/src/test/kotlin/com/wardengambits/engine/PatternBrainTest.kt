package com.wardengambits.engine

import com.wardengambits.engine.ai.PatternBrain
import com.wardengambits.engine.model.FighterId
import com.wardengambits.engine.rules.CombatEngine
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

class PatternBrainTest {
    private val rival = FighterId(1)
    private val random = FixedRandom(0)
    private val start = CombatEngine().start(setup(1, 1), 1L)

    @Test
    fun cyclesByCastCount() {
        val brain = PatternBrain(listOf("12", "21"))
        val expected = listOf("12", "21", "12")
        expected.forEachIndexed { count, sequence ->
            val state = start.withFighter(rival) { copy(castCount = count, power = 100) }
            assertEquals(sequence, brain.decide(state, rival, random)?.sequence)
        }
    }

    @Test
    fun waitsOnCadence() {
        val brain = PatternBrain(listOf("12"), everyNTicks = 2)
        val ready = start.withFighter(rival) { copy(power = 100) }
        assertNull(brain.decide(ready.copy(tick = 3), rival, random))
        assertEquals("12", brain.decide(ready.copy(tick = 4), rival, random)?.sequence)
    }

    @Test
    fun waitsForPower() {
        val brain = PatternBrain(listOf("12"))
        val state = start.withFighter(rival) { copy(power = 5) }
        assertNull(brain.decide(state, rival, random))
    }

    @Test
    fun rejectsUnknownCombo() {
        assertFailsWith<IllegalArgumentException> { PatternBrain(listOf("999")) }
    }

    /** The combo due this tick regardless of power (used by the rival potion rule); null off cadence. */
    @Test
    fun nextCastIgnoresPowerButNotCadence() {
        val brain = PatternBrain(listOf("12", "1212"), everyNTicks = 2)
        val poor = start.withFighter(rival) { copy(castCount = 1, power = 0) }
        assertEquals("1212", brain.nextCast(poor.copy(tick = 2), rival)?.sequence)
        assertNull(brain.nextCast(poor.copy(tick = 3), rival))
    }
}
