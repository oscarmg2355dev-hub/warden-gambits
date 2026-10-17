package com.wardengambits.sim

import com.wardengambits.engine.story.Route
import com.wardengambits.engine.story.content.StoryContentLoader
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/** Adventures follow the story rule of 2026-10-13: a defeat sends the Warden back to the previous node. */
class AdventureTest {
    private val content = StoryContentLoader.loadFromResources()

    @Test
    fun `a defeat does not end the adventure`() {
        val runs = Simulator(content).simulateAdventures(Skill.MEDIO, 20, firstSeed = 14_000_000L)
        // Under the old rule a run with a death never completed.
        assertTrue(runs.any { it.deaths > 0 && it.completed }, runs.map { it.deaths to it.completed }.toString())
        runs.forEach { r ->
            assertEquals(r.deaths, r.deathsByZone.values.sum())
            assertEquals(r.deaths, r.deathsByNode.values.sum())
            assertTrue(r.deathsByZone.keys.all { it in r.reachedZones })
            assertTrue(r.completed || r.abandoned)
            assertFalse(r.completed && r.abandoned)
        }
    }

    @Test
    fun simAdventureReachesOrodruin() {
        val run = Simulator(content).simulateAdventures(Skill.MEDIO, 1, firstSeed = 14_000_000L).single()
        assertTrue(run.completed)
        assertEquals(Route.zones.map { it.id }, run.reachedZones)
        assertEquals("orodruin", run.reachedZones.last())
    }

    @Test
    fun `adventures are deterministic`() {
        val sim = Simulator(content)
        assertEquals(sim.simulateAdventures(Skill.MEDIO, 5, 42L), sim.simulateAdventures(Skill.MEDIO, 5, 42L))
    }

    @Test
    fun `an unbeatable boss is abandoned after the death cap`() {
        val run = Simulator(withBoss(content, "bree", offset = 80)).simulateAdventures(Skill.EXPERTO, 1, 1L).single()
        assertTrue(run.abandoned)
        assertFalse(run.completed)
        assertEquals(MAX_DEATHS_PER_ADVENTURE, run.deaths)
        assertEquals(MAX_DEATHS_PER_ADVENTURE, run.deathsByNode.getValue("bree_7_0"))
        assertEquals(listOf("bree"), run.reachedZones)
    }

    @Test
    fun `aggregate counts deaths, deathless runs and abandoned runs`() {
        fun run(deaths: Map<String, Int>, completed: Boolean = true) = AdventureRunSummary(
            reachedZones = listOf("bree", "chet"),
            entryLevels = mapOf("bree" to 1, "chet" to 3),
            completed = completed,
            abandoned = !completed,
            deathsByNode = deaths,
            capped = 0,
        )
        val stats = aggregate(
            Skill.MEDIO,
            listOf(run(emptyMap()), run(mapOf("bree_7_0" to 2, "chet_3_0" to 1)), run(mapOf("chet_7_0" to 5), completed = false)),
        )
        assertEquals(3, stats.runs)
        assertEquals(2, stats.completed)
        assertEquals(1, stats.abandoned)
        assertEquals(1, stats.deathless)
        assertEquals(8, stats.deaths)
        assertEquals(mapOf("bree" to 2, "chet" to 6), stats.deathsByZone)
        assertEquals(mapOf("bree_7_0" to 2, "chet_3_0" to 1, "chet_7_0" to 5), stats.deathsByNode)
    }
}
