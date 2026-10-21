package com.wardengambits.engine.story

import com.wardengambits.engine.story.content.StoryContentLoader
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ExpectedLevelsTest {
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
}
