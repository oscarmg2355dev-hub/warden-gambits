package com.wardengambits.sim

import com.wardengambits.engine.ai.PatternBrain
import com.wardengambits.engine.model.CombatSetup
import com.wardengambits.engine.model.FighterSpec
import com.wardengambits.engine.model.RivalSpec
import com.wardengambits.engine.story.ExpectedLevels
import com.wardengambits.engine.story.NodeId
import com.wardengambits.engine.story.Route
import com.wardengambits.engine.story.content.StoryContentLoader
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SimulationTest {
    private val levels = ExpectedLevels(StoryContentLoader.loadFromResources())

    @Test
    fun `expected level starts at 1 and grows by the previous zone encounters`() {
        assertEquals(0.0, levels.expectedWinsBefore(NodeId("bree", 1, 0)))
        assertEquals(1, levels.expectedLevel(NodeId("bree", 3, 0)))
        val bree = levels.averageEncountersPerPath("bree")
        assertTrue(bree in 2.0..4.0)
        assertEquals(bree, levels.expectedWinsBefore(NodeId("chet", 1, 0)), 1e-9)
        Route.REST_ZONES.filter { id -> Route.treeZones.any { it.id == id } }
            .forEach { assertEquals(0.0, levels.averageEncountersPerPath(it), it) }
    }

    @Test
    fun `fights are deterministic and a much stronger player wins`() {
        val setup = CombatSetup(FighterSpec("P", 10), listOf(RivalSpec("R", 1, PatternBrain(listOf("12")))))
        val a = FightRunner.fight(setup, Skill.MEDIO, 42L)
        assertEquals(a, FightRunner.fight(setup, Skill.MEDIO, 42L))
        assertTrue(a.victory)
    }
}
