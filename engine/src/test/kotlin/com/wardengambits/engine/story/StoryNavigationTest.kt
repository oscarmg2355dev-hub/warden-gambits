package com.wardengambits.engine.story

import com.wardengambits.engine.story.content.EncounterContent
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class StoryNavigationTest {
    private val engine = StoryEngine(fakeContent())

    private fun node(zone: String, level: Int, index: Int) = NodeId(zone, level, index)

    private fun walk(engine: StoryEngine, start: StoryProfile, until: NodeId): StoryProfile {
        var profile = start
        var steps = 0
        while (profile.run.node != until) {
            check(++steps < MAX_STEPS) { "walk did not reach $until" }
            profile = engine.reduce(profile, StoryAction.Choose(0).takeIf { engine.options(profile).size == 2 } ?: StoryAction.Continue).profile
        }
        return profile
    }

    @Test fun newGame() {
        val p = engine.newGame()
        assertEquals(RunState(1, 1, 1, 0, NodeId.START, listOf(NodeId.START), Phase.CHOOSING), p.run)
        assertEquals(emptyList(), p.history)
    }

    @Test fun newGameOnEncounterNodeIsCombatPending() {
        val withEncounter = StoryEngine(fakeContent(encounters = mapOf("bree_1_0" to EncounterContent(listOf(rival())))))
        assertEquals(Phase.COMBAT_PENDING, withEncounter.newGame().run.phase)
    }

    @Test fun chooseMovesAndRecordsPath() {
        val step = engine.reduce(engine.newGame(), StoryAction.Choose(1))
        assertEquals(node("bree", 2, 1), step.profile.run.node)
        assertEquals(listOf(node("bree", 1, 0), node("bree", 2, 1)), step.profile.run.path)
        assertEquals(listOf<StoryEvent>(StoryEvent.NodeEntered(node("bree", 2, 1))), step.events)
    }

    @Test fun optionsWithLabels() {
        assertEquals(
            listOf(
                Choice(node("bree", 2, 0), "C bree_2_0"),
                Choice(node("bree", 2, 1), "C bree_2_1"),
            ),
            engine.options(engine.newGame()),
        )
    }

    @Test fun continueOnSingleDestination() {
        val at = walk(engine, engine.newGame(), node("bree", 6, 0))
        assertEquals(listOf(Choice(node("bree", 7, 0), null)), engine.options(at))
        val rejected = engine.reduce(at, StoryAction.Choose(0))
        assertEquals(at, rejected.profile)
        assertEquals(listOf<StoryEvent>(StoryEvent.StoryRejected(StoryRejectReason.INVALID_OPTION)), rejected.events)
        assertEquals(node("bree", 7, 0), engine.reduce(at, StoryAction.Continue).profile.run.node)
    }

    @Test fun continueRejectedOnTwoDestinations() {
        val p = engine.newGame()
        val step = engine.reduce(p, StoryAction.Continue)
        assertEquals(p, step.profile)
        assertEquals(listOf<StoryEvent>(StoryEvent.StoryRejected(StoryRejectReason.INVALID_OPTION)), step.events)
    }

    @Test fun chooseOutOfRange() {
        val p = engine.newGame()
        for (option in listOf(2, -1)) {
            val step = engine.reduce(p, StoryAction.Choose(option))
            assertEquals(p, step.profile)
            assertEquals(listOf<StoryEvent>(StoryEvent.StoryRejected(StoryRejectReason.INVALID_OPTION)), step.events)
        }
    }

    @Test fun zoneChangeEmitsZoneEntered() {
        val at = walk(engine, engine.newGame(), node("bree", 7, 0))
        val step = engine.reduce(at, StoryAction.Continue)
        assertEquals(
            listOf<StoryEvent>(StoryEvent.NodeEntered(node("chet", 1, 0)), StoryEvent.ZoneEntered("chet")),
            step.events,
        )
        assertEquals(ZoneProgress(ZoneInfo("chet", "Bosque de Chet"), 1, 7), engine.zoneProgress(step.profile))
    }

    @Test fun cannotAdvanceWhileCombatPending() {
        val withEncounter = StoryEngine(fakeContent(encounters = mapOf("bree_2_0" to EncounterContent(listOf(rival())))))
        val inCombat = withEncounter.reduce(withEncounter.newGame(), StoryAction.Choose(0)).profile
        assertEquals(Phase.COMBAT_PENDING, inCombat.run.phase)
        for (action in listOf(StoryAction.Choose(0), StoryAction.Continue)) {
            val step = withEncounter.reduce(inCombat, action)
            assertEquals(inCombat, step.profile)
            assertEquals(listOf<StoryEvent>(StoryEvent.StoryRejected(StoryRejectReason.WRONG_PHASE)), step.events)
        }
    }

    @Test fun fullLapReachesOrodruin() {
        var profile = engine.newGame()
        var actions = 0
        var last: StoryStep? = null
        while (profile.run.node != NodeId.FINAL) {
            val action = if (engine.options(profile).size == 2) StoryAction.Choose(0) else StoryAction.Continue
            last = engine.reduce(profile, action)
            profile = last.profile
            actions++
        }
        assertEquals(161, actions)
        assertEquals("orodruin_final", profile.run.node.key)
        assertEquals(Phase.LAP_COMPLETE, profile.run.phase)
        assertTrue(StoryEvent.ZoneEntered("orodruin") in last!!.events)
        assertEquals(emptyList(), engine.options(profile))
        assertEquals(ZoneProgress(Route.zone("orodruin"), 1, 1), engine.zoneProgress(profile))
    }

    private companion object {
        const val MAX_STEPS = 500
    }
}
