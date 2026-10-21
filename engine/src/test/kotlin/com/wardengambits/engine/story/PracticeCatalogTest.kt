package com.wardengambits.engine.story

import com.wardengambits.engine.story.content.StoryContentLoader
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class PracticeCatalogTest {
    private val content = StoryContentLoader.loadFromResources()
    private val engine = StoryEngine(content)

    private fun profileAt(node: NodeId, phase: Phase, history: List<RunRecord> = emptyList()) =
        StoryProfile(RunState(5, 1, 1, 4, node, listOf(node), phase), history)

    @Test
    fun newGameUnlocksNothing() {
        val entries = engine.practiceEntries(engine.newGame())
        val expected = content.zones.sumOf { z -> z.nodes.count { it.encounter != null } }
        assertEquals(expected, entries.size)
        assertTrue(entries.none { it.unlocked })
        val order = entries.map { Triple(Route.indexOf(it.zoneId), it.node.level, it.node.index) }
        assertEquals(order.sortedWith(compareBy({ it.first }, { it.second }, { it.third })), order)
    }

    @Test
    fun midMoriaUnlocksEarlierZonesAndLowerLevels() {
        val current = NodeId("moria", 4, 1)
        val entries = engine.practiceEntries(profileAt(current, Phase.COMBAT_PENDING))
        val moria = Route.indexOf("moria")
        assertTrue(entries.any { it.zoneId == "bree" })
        for (e in entries) {
            val zone = Route.indexOf(e.zoneId)
            val expected = zone < moria || (zone == moria && e.node.level < 4)
            assertEquals(expected, e.unlocked, e.node.key)
        }
        assertTrue(entries.single { it.node == current }.unlocked.not())
    }

    @Test
    fun currentNodeUnlocksOnceWon() {
        val current = NodeId("moria", 4, 1)
        val entries = engine.practiceEntries(profileAt(current, Phase.CHOOSING))
        assertTrue(entries.single { it.node == current }.unlocked)
    }

    @Test
    fun lapCompleteUnlocksEverythingWithoutCrash() {
        val done = engine.practiceEntries(profileAt(NodeId.FINAL, Phase.LAP_COMPLETE))
        assertTrue(done.isNotEmpty() && done.all { it.unlocked })
        val record = RunRecord(1, NodeId.FINAL, 20, 30, RunResult.COMPLETED, 0L, 0)
        val again = engine.practiceEntries(profileAt(NodeId.START, Phase.CHOOSING, listOf(record)))
        assertTrue(again.all { it.unlocked })
    }

    @Test
    fun playerLevelIsTheExpectedLevel() {
        val levels = ExpectedLevels(content)
        for (e in engine.practiceEntries(engine.newGame())) assertEquals(levels.expectedLevel(e.node), e.playerLevel, e.node.key)
    }

    @Test
    fun practiceSetupUsesExpectedLevelAndFirstLapRivals() {
        val node = NodeId("moria", 7, 0)
        val setup = assertNotNull(engine.practiceSetup(node))
        assertEquals(ExpectedLevels(content).expectedLevel(node), setup.player.level)
        val story = assertNotNull(engine.combatSetupFor(profileAt(node, Phase.COMBAT_PENDING)))
        assertEquals(story.rivals.map { it.name to it.level }, setup.rivals.map { it.name to it.level })
        assertTrue(setup.rivals.first().isBoss)
        assertNull(engine.practiceSetup(NodeId.START))
    }
}
