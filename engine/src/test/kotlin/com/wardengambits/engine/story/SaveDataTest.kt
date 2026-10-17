package com.wardengambits.engine.story

import com.wardengambits.engine.model.Outcome
import com.wardengambits.engine.story.content.EncounterContent
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class SaveDataTest {
    private val engine = StoryEngine(fakeContent(mapOf("bree_1_0" to EncounterContent(listOf(rival())))))

    @Test fun roundTrip() {
        var p = engine.newGame()
        p = engine.reduce(p, StoryAction.CombatFinished(Outcome.VICTORY, 10L)).profile
        p = engine.reduce(p, StoryAction.Continue).profile
        p = engine.reduce(p, StoryAction.Choose(1)).profile
        // a death and a history record (with its own deaths) must survive the round trip
        val pending = p.copy(run = p.run.copy(phase = Phase.COMBAT_PENDING))
        p = engine.reduce(pending, StoryAction.CombatFinished(Outcome.DEFEAT, 99L)).profile
        assertEquals(1, p.run.deaths)
        p = p.copy(history = listOf(RunRecord(1, NodeId.FINAL, 15, 14, RunResult.COMPLETED, 5L, deaths = 4)))
        val decoded = SaveCodec.decode(SaveCodec.encode(p))
        assertNotNull(decoded)
        assertEquals(p, decoded.profile)
        assertEquals(1, decoded.version)
    }

    @Test fun oldSaveWithoutDeathsLoadsWithZero() {
        val old = """{"version":1,"profile":{"run":{"level":3,"lap":1,"lapStartLevel":1,"wins":2,""" +
            """"node":{"zoneId":"bree","level":2,"index":1},"path":[{"zoneId":"bree","level":1,"index":0},""" +
            """{"zoneId":"bree","level":2,"index":1}],"phase":"CHOOSING"},"history":[{"lap":1,""" +
            """"node":{"zoneId":"chet","level":3,"index":0},"level":4,"wins":3,"result":"DEFEATED","finishedAtMillis":7}]}}"""
        val decoded = assertNotNull(SaveCodec.decode(old))
        assertEquals(0, decoded.profile.run.deaths)
        assertEquals(0, decoded.profile.history.single().deaths)
        assertEquals(RunResult.DEFEATED, decoded.profile.history.single().result)
        assertEquals(NodeId("bree", 2, 1), decoded.profile.run.node)
        assertTrue(engine.isValid(decoded.profile))
    }

    @Test fun corruptJsonIsNull() {
        assertNull(SaveCodec.decode("{oops"))
        assertNull(SaveCodec.decode(""))
    }

    @Test fun newGameIsValid() = assertTrue(engine.isValid(engine.newGame()))

    @Test fun invalidProfiles() {
        val base = engine.newGame()
        fun run(f: (RunState) -> RunState) = base.copy(run = f(base.run))
        fun at(node: NodeId, phase: Phase = Phase.CHOOSING) =
            run { it.copy(node = node, path = listOf(node), phase = phase) }
        assertTrue(engine.isValid(at(NodeId("bree", 2, 1))))
        assertFalse(engine.isValid(at(NodeId("bree", 9, 0))))
        assertFalse(engine.isValid(at(NodeId("bree", 2, 5))))
        assertFalse(engine.isValid(at(NodeId("mordor", 1, 0))))
        assertFalse(engine.isValid(at(NodeId("bree", 2, 0), Phase.COMBAT_PENDING)))
        assertFalse(engine.isValid(at(NodeId("bree", 2, 0), Phase.LAP_COMPLETE)))
        assertFalse(engine.isValid(run { it.copy(level = 0) }))
        assertFalse(engine.isValid(run { it.copy(lap = 0) }))
        assertFalse(engine.isValid(run { it.copy(lapStartLevel = 0) }))
        assertFalse(engine.isValid(run { it.copy(wins = -1) }))
        assertFalse(engine.isValid(run { it.copy(deaths = -1) }))
        assertTrue(engine.isValid(run { it.copy(deaths = 7) }))
        assertFalse(engine.isValid(run { it.copy(path = emptyList()) }))
        assertFalse(engine.isValid(run { it.copy(path = listOf(NodeId("bree", 2, 0))) }))
    }

    @Test fun invalidHistoryRecords() {
        val base = engine.newGame()
        val good = RunRecord(1, NodeId("bree", 2, 0), 3, 2, RunResult.DEFEATED, 5L)
        fun withHistory(r: RunRecord) = base.copy(history = listOf(r))
        assertTrue(engine.isValid(withHistory(good)))
        assertFalse(engine.isValid(withHistory(good.copy(node = NodeId("mordor", 1, 0)))))
        assertFalse(engine.isValid(withHistory(good.copy(lap = 0))))
        assertFalse(engine.isValid(withHistory(good.copy(level = 0))))
        assertFalse(engine.isValid(withHistory(good.copy(wins = -1))))
        assertFalse(engine.isValid(withHistory(good.copy(deaths = -1))))
    }

    @Test fun finalNodeRequiresLapComplete() {
        val base = engine.newGame()
        fun atFinal(phase: Phase) =
            base.copy(run = base.run.copy(node = NodeId.FINAL, path = listOf(NodeId.FINAL), phase = phase))
        assertTrue(engine.isValid(atFinal(Phase.LAP_COMPLETE)))
        assertFalse(engine.isValid(atFinal(Phase.CHOOSING)))
    }
}
