package com.wardengambits.engine

import com.wardengambits.engine.action.CombatAction
import com.wardengambits.engine.ai.PatternBrain
import com.wardengambits.engine.model.CombatSetup
import com.wardengambits.engine.model.FighterSpec
import com.wardengambits.engine.model.PLAYER_ID
import com.wardengambits.engine.model.RivalSpec
import com.wardengambits.engine.rules.CombatEngine
import com.wardengambits.engine.rules.StepResult
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class CombatSimulationTest {
    private val maxTicks = 500

    private fun runFight(seed: Long, playerLevel: Int, vararg rivalLevels: Int): List<StepResult> {
        val engine = CombatEngine()
        val brain = PatternBrain(listOf("12", "31", "21"))
        val setup = CombatSetup(
            player = FighterSpec("Player", playerLevel),
            rivals = rivalLevels.mapIndexed { index, level -> RivalSpec("Rival ${index + 1}", level, brain) },
        )
        var state = engine.start(setup, seed)
        val results = mutableListOf(StepResult(state, emptyList()))
        repeat(maxTicks) {
            if (state.outcome != null) return results
            val player = state.player
            if (player.power >= 10 && player.queued == null) {
                state = engine.press(state, PLAYER_ID, "12")
                state = engine.reduce(state, CombatAction.Execute(PLAYER_ID)).state
            }
            val step = engine.reduce(state, CombatAction.Tick)
            results += step
            state = step.state
        }
        return results
    }

    @Test
    fun sameSeedSameFight() =
        assertEquals(runFight(99, 3, 3), runFight(99, 3, 3))

    @Test
    fun fightsTerminate() {
        (1..10).forEach { level ->
            assertNotNull(runFight(level.toLong(), level, level).last().state.outcome, "level $level")
        }
    }

    @Test
    fun fourRivalsTerminate() {
        assertNotNull(runFight(7, 5, 1, 1, 1, 1).last().state.outcome)
    }
}
