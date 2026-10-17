package com.wardengambits.engine.story

import kotlin.test.Test
import kotlin.test.assertEquals

class SaveMigrationTest {
    private val history = listOf(RunRecord(1, NodeId("lothlorien", 1, 0), 30, 40, RunResult.COMPLETED, 5L, deaths = 3))

    private fun profileAt(node: NodeId, phase: Phase) = StoryProfile(
        RunState(level = 42, lap = 2, lapStartLevel = 30, wins = 55, node = node, path = listOf(node), phase = phase, deaths = 4),
        history,
    )

    @Test
    fun migrateOldFinal() {
        val old = profileAt(NodeId("lothlorien", 1, 0), Phase.LAP_COMPLETE)
        val migrated = SaveMigration.migrate(old)
        assertEquals(Phase.CHOOSING, migrated.run.phase)
        assertEquals(old.copy(run = old.run.copy(phase = Phase.CHOOSING)), migrated)
        assertEquals(42, migrated.run.level)
        assertEquals(55, migrated.run.wins)
        assertEquals(2, migrated.run.lap)
        assertEquals(4, migrated.run.deaths)
        assertEquals(history, migrated.history)
    }

    @Test
    fun migrateLeavesOthers() {
        val other = profileAt(NodeId("nimrodel", 3, 1), Phase.CHOOSING)
        assertEquals(other, SaveMigration.migrate(other))
        val choosingAtLothlorien = profileAt(NodeId("lothlorien", 1, 0), Phase.CHOOSING)
        assertEquals(choosingAtLothlorien, SaveMigration.migrate(choosingAtLothlorien))
    }
}
