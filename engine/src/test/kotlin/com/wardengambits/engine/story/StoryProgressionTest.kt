package com.wardengambits.engine.story

import com.wardengambits.engine.ai.PatternBrain
import com.wardengambits.engine.model.Outcome
import com.wardengambits.engine.story.content.EncounterContent
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class StoryProgressionTest {
    private val engine = StoryEngine(fakeContent())

    private fun pendingAtStart(
        vararg rivals: com.wardengambits.engine.story.content.RivalContent,
        baseLevels: Map<String, Int> = emptyMap(),
    ) = StoryEngine(
        fakeContent(mapOf("bree_1_0" to EncounterContent(rivals.toList())), baseLevels),
    )

    @Test fun combatSetupLap1() {
        val e = pendingAtStart(rival("Lobo", -1), rival("Huargo", 2), baseLevels = mapOf("bree" to 4))
        val setup = e.combatSetupFor(e.newGame())!!
        assertEquals(1, setup.player.level)
        assertEquals("Warden", setup.player.name)
        assertEquals(listOf(3, 6), setup.rivals.map { it.level })
        assertEquals(listOf("Lobo", "Huargo"), setup.rivals.map { it.name })
        assertEquals(listOf(PatternBrain(listOf("12"), 1), PatternBrain(listOf("12"), 1)), setup.rivals.map { it.brain })
    }

    @Test fun combatSetupMarksOnlyTheLevel7FirstRivalAsBoss() {
        val bossNode = NodeId("bree", 7, 0)
        val commonNode = NodeId("bree", 3, 0)
        val e = StoryEngine(
            fakeContent(
                mapOf(
                    bossNode.key to EncounterContent(listOf(rival("Jefe", 2), rival("Lobo", -4), rival("Bandido", -4))),
                    commonNode.key to EncounterContent(listOf(rival("Lobo", -2), rival("Bandido", -2))),
                ),
            ),
        )
        fun pendingAt(node: NodeId) = StoryProfile(RunState(5, 1, 1, 4, node, listOf(node), Phase.COMBAT_PENDING), emptyList())
        assertEquals(listOf(true, false, false), e.combatSetupFor(pendingAt(bossNode))!!.rivals.map { it.isBoss })
        assertEquals(listOf(false, false), e.combatSetupFor(pendingAt(commonNode))!!.rivals.map { it.isBoss })
    }

    @Test fun combatSetupCarriesPortrait() {
        val e = pendingAtStart(rival("Lobo", 0, "wolf"), rival("Huargo", 0, "warg"))
        val setup = e.combatSetupFor(e.newGame())!!
        assertEquals(listOf("wolf", "warg"), setup.rivals.map { it.portrait })
        val state = com.wardengambits.engine.rules.CombatEngine().start(setup, 1L)
        assertEquals(listOf("warden", "wolf", "warg"), state.fighters.map { it.portrait })
    }

    @Test fun combatSetupClampsToOne() {
        val e = pendingAtStart(rival("Lobo", -3), baseLevels = mapOf("bree" to 1))
        assertEquals(1, e.combatSetupFor(e.newGame())!!.rivals.single().level)
    }

    @Test fun combatSetupNullWhenNotPending() = assertNull(engine.combatSetupFor(engine.newGame()))

    @Test fun victoryLevelsUp() {
        val e = pendingAtStart(rival())
        val step = e.reduce(e.newGame(), StoryAction.CombatFinished(Outcome.VICTORY, 0))
        assertEquals(2, step.profile.run.level)
        assertEquals(1, step.profile.run.wins)
        assertEquals(Phase.CHOOSING, step.profile.run.phase)
        assertEquals(listOf<StoryEvent>(StoryEvent.LeveledUp(2)), step.events)
    }

    @Test fun combatFinishedRejectedWhenNotPending() {
        val profile = engine.newGame()
        val step = engine.reduce(profile, StoryAction.CombatFinished(Outcome.VICTORY, 0))
        assertEquals(profile, step.profile)
        assertEquals(listOf<StoryEvent>(StoryEvent.StoryRejected(StoryRejectReason.WRONG_PHASE)), step.events)
    }

    private val twoFightsEngine = StoryEngine(
        fakeContent(
            mapOf(
                "bree_2_0" to EncounterContent(listOf(rival())),
                "bree_3_0" to EncounterContent(listOf(rival())),
            ),
        ),
    )

    /** Won bree_2_0 (level 2, 1 win) and standing on the bree_3_0 fight. */
    private fun pendingAtBree30(): StoryProfile {
        var p = twoFightsEngine.newGame()
        p = twoFightsEngine.reduce(p, StoryAction.Choose(0)).profile
        p = twoFightsEngine.reduce(p, StoryAction.CombatFinished(Outcome.VICTORY, 0)).profile
        p = twoFightsEngine.reduce(p, StoryAction.Choose(0)).profile
        assertEquals(NodeId("bree", 3, 0), p.run.node)
        assertEquals(Phase.COMBAT_PENDING, p.run.phase)
        return p
    }

    @Test fun defeatReturnsToPreviousNode() {
        val previous = NodeId("bree", 2, 0)
        val lost = NodeId("bree", 3, 0)
        val pending = pendingAtBree30()
        val atPrevious = pending.copy(run = pending.run.copy(node = previous, path = pending.run.path.dropLast(1), phase = Phase.CHOOSING))
        val step = twoFightsEngine.reduce(pending, StoryAction.CombatFinished(Outcome.DEFEAT, 1234))
        val run = step.profile.run
        assertEquals(previous, run.node)
        assertEquals(listOf(NodeId.START, previous), run.path)
        assertEquals(Phase.CHOOSING, run.phase)
        assertEquals(twoFightsEngine.options(atPrevious), twoFightsEngine.options(step.profile))
        assertEquals(2, twoFightsEngine.options(step.profile).size)
        assertEquals(
            listOf(StoryEvent.ReturnedToPreviousNode(lost, previous), StoryEvent.NodeEntered(previous)),
            step.events,
        )
        assertTrue(twoFightsEngine.isValid(step.profile))
    }

    @Test fun defeatKeepsLevelWinsLapAndHistory() {
        val step = twoFightsEngine.reduce(pendingAtBree30(), StoryAction.CombatFinished(Outcome.DEFEAT, 1234))
        val run = step.profile.run
        assertEquals(2, run.level)
        assertEquals(1, run.wins)
        assertEquals(1, run.lap)
        assertEquals(1, run.lapStartLevel)
        assertEquals(emptyList(), step.profile.history)
        assertTrue(step.events.none { it is StoryEvent.RunEnded })
    }

    @Test fun deathsIncrementOnEveryDefeat() {
        val e = twoFightsEngine
        assertEquals(0, e.newGame().run.deaths)
        var p = e.reduce(pendingAtBree30(), StoryAction.CombatFinished(Outcome.DEFEAT, 1)).profile
        assertEquals(1, p.run.deaths)
        p = e.reduce(p, StoryAction.Choose(0)).profile
        assertEquals(Phase.COMBAT_PENDING, p.run.phase)
        p = e.reduce(p, StoryAction.CombatFinished(Outcome.DEFEAT, 2)).profile
        assertEquals(2, p.run.deaths)
        assertEquals(NodeId("bree", 2, 0), p.run.node)
        p = e.reduce(p, StoryAction.Choose(0)).profile
        p = e.reduce(p, StoryAction.CombatFinished(Outcome.VICTORY, 3)).profile
        assertEquals(2, p.run.deaths)
        assertEquals(3, p.run.level)
    }

    @Test fun defeatWithoutPreviousNodeFallsBackToStart() {
        val inLap2 = lap2().profile
        assertEquals(listOf(NodeId.START), inLap2.run.path)
        val step = fullLapEngine.reduce(inLap2, StoryAction.CombatFinished(Outcome.DEFEAT, 200))
        assertEquals(inLap2.run.copy(deaths = 1), step.profile.run)
        assertEquals(inLap2.history, step.profile.history)
        assertEquals(
            listOf(
                StoryEvent.ReturnedToPreviousNode(NodeId.START, NodeId.START),
                StoryEvent.NodeEntered(NodeId.START),
                StoryEvent.ZoneEntered("bree"),
            ),
            step.events,
        )
    }

    @Test fun startNextLapOnlyWhenComplete() {
        val profile = engine.newGame()
        val step = engine.reduce(profile, StoryAction.StartNextLap(0))
        assertEquals(profile, step.profile)
        assertEquals(listOf<StoryEvent>(StoryEvent.StoryRejected(StoryRejectReason.WRONG_PHASE)), step.events)
    }

    private val fullLapEngine = StoryEngine(
        fakeContent(
            Route.zones.filter { it.id != Route.FINAL_ZONE_ID }
                .associate { "${it.id}_1_0" to EncounterContent(listOf(rival())) },
        ),
    )

    private fun completeLap(): StoryProfile {
        var profile = fullLapEngine.newGame()
        var steps = 0
        while (profile.run.phase != Phase.LAP_COMPLETE) {
            check(++steps < MAX_STEPS) { "lap did not complete" }
            val action = when (profile.run.phase) {
                Phase.COMBAT_PENDING -> StoryAction.CombatFinished(Outcome.VICTORY, 0)
                Phase.CHOOSING ->
                    if (fullLapEngine.options(profile).size == 2) StoryAction.Choose(0) else StoryAction.Continue
                Phase.LAP_COMPLETE -> error("unreachable")
            }
            profile = fullLapEngine.reduce(profile, action).profile
        }
        return profile
    }

    private fun lap2(): StoryStep = fullLapEngine.reduce(completeLap(), StoryAction.StartNextLap(99))

    @Test fun fullLapThenNextLap() {
        val done = completeLap()
        assertEquals(24, done.run.level)
        assertEquals(23, done.run.wins)
        val step = lap2()
        val record = RunRecord(1, NodeId.FINAL, 24, 23, RunResult.COMPLETED, 99)
        assertEquals(listOf(record), step.profile.history)
        assertEquals(
            RunState(24, 2, 24, 0, NodeId.START, listOf(NodeId.START), Phase.COMBAT_PENDING),
            step.profile.run,
        )
        assertEquals(
            listOf(
                StoryEvent.RunEnded(record),
                StoryEvent.LapStarted(2),
                StoryEvent.NodeEntered(NodeId.START),
                StoryEvent.ZoneEntered("bree"),
            ),
            step.events,
        )
        assertEquals(24, fullLapEngine.combatSetupFor(step.profile)!!.rivals.single().level)
    }

    @Test fun startNextLapRecordsDeathsAndResetsThem() {
        val done = completeLap().let { it.copy(run = it.run.copy(deaths = 3)) }
        val step = fullLapEngine.reduce(done, StoryAction.StartNextLap(99))
        assertEquals(listOf(RunRecord(1, NodeId.FINAL, 24, 23, RunResult.COMPLETED, 99, deaths = 3)), step.profile.history)
        assertEquals(0, step.profile.run.deaths)
        assertEquals(2, step.profile.run.lap)
    }

    private companion object {
        const val MAX_STEPS = 500
    }
}
