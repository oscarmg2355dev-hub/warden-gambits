package com.wardengambits.engine.story

import com.wardengambits.engine.ai.BossModes
import com.wardengambits.engine.ai.PatternBrain
import com.wardengambits.engine.model.CombatSetup
import com.wardengambits.engine.model.FighterSpec
import com.wardengambits.engine.model.Outcome
import com.wardengambits.engine.model.Reaction
import com.wardengambits.engine.model.RivalSpec
import com.wardengambits.engine.story.content.NodeContent
import com.wardengambits.engine.story.content.StoryContent

class StoryEngine(private val content: StoryContent) {
    private val expectedLevels by lazy { ExpectedLevels(content) }

    fun newGame(): StoryProfile = StoryProfile(startRun(level = 1, lap = 1, lapStartLevel = 1).first, emptyList())

    fun reduce(profile: StoryProfile, action: StoryAction): StoryStep = when (action) {
        is StoryAction.Choose -> navigate(profile, action.option)
        StoryAction.Continue -> navigate(profile, 0, requiredDestinations = 1)
        is StoryAction.CombatFinished -> finishCombat(profile, action)
        is StoryAction.StartNextLap -> startNextLap(profile, action.nowMillis)
    }

    fun options(profile: StoryProfile): List<Choice> {
        val run = profile.run
        if (run.phase != Phase.CHOOSING) return emptyList()
        val destinations = TreeShape.destinations(run.node)
        return destinations.map { destination ->
            Choice(destination, if (destinations.size == 2) node(destination).choiceLabel else null)
        }
    }

    /** True when [profile] is coherent with the tree shape and content; never throws. */
    fun isValid(profile: StoryProfile): Boolean {
        val run = profile.run
        val node = run.node
        val inShape = when {
            Route.zones.none { it.id == node.zoneId } -> false
            node.zoneId == Route.FINAL_ZONE_ID -> node == NodeId.FINAL
            else -> node.level in 1..TreeShape.LEVELS && node.index in 0 until TreeShape.size(node.level)
        }
        if (!inShape) return false
        val nodeContent = content.node(node) ?: return false
        val phaseOk = when (run.phase) {
            Phase.COMBAT_PENDING -> nodeContent.encounter != null
            Phase.LAP_COMPLETE -> node == NodeId.FINAL
            Phase.CHOOSING -> true
        }
        val historyOk = profile.history.all { r ->
            Route.zones.any { it.id == r.node.zoneId } && r.lap >= 1 && r.level >= 1 && r.wins >= 0 && r.deaths >= 0
        }
        val finalOk = node != NodeId.FINAL || run.phase == Phase.LAP_COMPLETE
        return phaseOk && historyOk && finalOk &&
            run.level >= 1 && run.lap >= 1 && run.lapStartLevel >= 1 && run.wins >= 0 && run.deaths >= 0 &&
            run.path.lastOrNull() == node
    }

    fun node(id: NodeId): NodeContent = requireNotNull(content.node(id)) { "No content for ${id.key}" }

    /** The route zones with their names from the loaded content (so in its language), in route order. */
    fun zones(): List<ZoneInfo> = Route.zones.map { zone(it.id) }

    /** [zoneId]'s route entry with the content's name (the route's name when the content lacks the zone). */
    fun zone(zoneId: String): ZoneInfo {
        val info = Route.zone(zoneId)
        return content.zone(zoneId)?.let { info.copy(name = it.name) } ?: info
    }

    fun zoneProgress(profile: StoryProfile): ZoneProgress {
        val node = profile.run.node
        if (node == NodeId.FINAL) return ZoneProgress(zone(Route.FINAL_ZONE_ID), 1, 1)
        return ZoneProgress(zone(node.zoneId), node.level, TreeShape.LEVELS)
    }

    fun combatSetupFor(profile: StoryProfile): CombatSetup? {
        val run = profile.run
        if (run.phase != Phase.COMBAT_PENDING) return null
        if (node(run.node).encounter == null) return null
        return CombatSetup(FighterSpec("Warden", run.level), rivalSpecs(run.node, run.lapStartLevel))
    }

    /** Practice mode: every encounter of the story with its unlock state (never touches the profile). */
    fun practiceEntries(profile: StoryProfile): List<PracticeEntry> = PracticeCatalog.entries(content, profile)

    /** Practice fight at [node]: the Warden at the expected level against the first-lap rivals; null without encounter. */
    fun practiceSetup(node: NodeId): CombatSetup? {
        if (content.node(node)?.encounter == null) return null
        return CombatSetup(FighterSpec("Warden", expectedLevels.expectedLevel(node)), rivalSpecs(node, lapStartLevel = 1))
    }

    private fun rivalSpecs(node: NodeId, lapStartLevel: Int): List<RivalSpec> {
        val encounter = requireNotNull(node(node).encounter) { "No encounter at ${node.key}" }
        val baseLevel = requireNotNull(content.zone(node.zoneId)) { "No zone ${node.zoneId}" }.baseLevel
        return encounter.rivals.mapIndexed { index, rival ->
            val reaction = rival.reaction?.let { Reaction(it.atHpPct, it.shield, it.combos) }
            val modes = rival.modes?.let { BossModes(it.defenseBelowPct, it.attackAbovePct, it.maxDefenses, it.defense) }
            RivalSpec(
                rival.name,
                maxOf(1, baseLevel + rival.levelOffset + (lapStartLevel - 1)),
                PatternBrain(rival.pattern, rival.everyNTicks, reaction, modes),
                // ContentRules enforces the boss as the first rival of a level-7 encounter.
                isBoss = node.level == TreeShape.LEVELS && index == 0,
                portrait = rival.portrait,
            )
        }
    }

    private fun finishCombat(profile: StoryProfile, action: StoryAction.CombatFinished): StoryStep {
        val run = profile.run
        if (run.phase != Phase.COMBAT_PENDING) return reject(profile, StoryRejectReason.WRONG_PHASE)
        return when (action.outcome) {
            Outcome.VICTORY -> {
                val next = run.copy(level = run.level + 1, wins = run.wins + 1, phase = Phase.CHOOSING)
                StoryStep(profile.copy(run = next), listOf(StoryEvent.LeveledUp(next.level)))
            }
            Outcome.DEFEAT -> returnToPreviousNode(profile)
        }
    }

    /**
     * A lost fight sends the Warden back to the previous node of the path, already completed (phase CHOOSING),
     * keeping level, wins and lap and counting one death. Level-1 nodes never have encounters, so a previous
     * node always exists in real content; without one the Warden re-enters [NodeId.START].
     */
    private fun returnToPreviousNode(profile: StoryProfile): StoryStep {
        val run = profile.run
        val counted = run.copy(deaths = run.deaths + 1)
        val previous = run.path.getOrNull(run.path.size - 2)
        if (previous == null) {
            val (entered, events) = enterNode(counted.copy(path = emptyList()), NodeId.START)
            return StoryStep(
                profile.copy(run = entered),
                listOf<StoryEvent>(StoryEvent.ReturnedToPreviousNode(run.node, NodeId.START)) + events,
            )
        }
        val back = counted.copy(node = previous, path = run.path.dropLast(1), phase = Phase.CHOOSING)
        return StoryStep(
            profile.copy(run = back),
            listOf(StoryEvent.ReturnedToPreviousNode(run.node, previous), StoryEvent.NodeEntered(previous)),
        )
    }

    private fun startNextLap(profile: StoryProfile, nowMillis: Long): StoryStep {
        val run = profile.run
        if (run.phase != Phase.LAP_COMPLETE) return reject(profile, StoryRejectReason.WRONG_PHASE)
        val record = RunRecord(run.lap, NodeId.FINAL, run.level, run.wins, RunResult.COMPLETED, nowMillis, run.deaths)
        val (fresh, entry) = startRun(level = run.level, lap = run.lap + 1, lapStartLevel = run.level)
        return StoryStep(
            StoryProfile(fresh, profile.history + record),
            listOf(StoryEvent.RunEnded(record), StoryEvent.LapStarted(run.lap + 1)) + entry,
        )
    }

    /** A run standing on [NodeId.START] with an empty history path, entered through [enterNode]. */
    private fun startRun(level: Int, lap: Int, lapStartLevel: Int): Pair<RunState, List<StoryEvent>> {
        val run = RunState(level, lap, lapStartLevel, 0, NodeId.START, emptyList(), Phase.CHOOSING)
        return enterNode(run, NodeId.START)
    }

    private fun navigate(profile: StoryProfile, option: Int, requiredDestinations: Int = 2): StoryStep {
        val run = profile.run
        if (run.phase != Phase.CHOOSING) return reject(profile, StoryRejectReason.WRONG_PHASE)
        val destinations = TreeShape.destinations(run.node)
        if (destinations.size != requiredDestinations || option !in destinations.indices) {
            return reject(profile, StoryRejectReason.INVALID_OPTION)
        }
        val (entered, events) = enterNode(run, destinations[option])
        return StoryStep(profile.copy(run = entered), events)
    }

    /** Moves the run into [node]: appends it to the path, sets the phase and lists the entry events. */
    private fun enterNode(run: RunState, node: NodeId): Pair<RunState, List<StoryEvent>> {
        val phase = when {
            node == NodeId.FINAL -> Phase.LAP_COMPLETE
            node(node).encounter != null -> Phase.COMBAT_PENDING
            else -> Phase.CHOOSING
        }
        val events = buildList {
            add(StoryEvent.NodeEntered(node))
            if (node.level == 1 || node == NodeId.FINAL) add(StoryEvent.ZoneEntered(node.zoneId))
        }
        return run.copy(node = node, path = run.path + node, phase = phase) to events
    }

    private fun reject(profile: StoryProfile, reason: StoryRejectReason) =
        StoryStep(profile, listOf(StoryEvent.StoryRejected(reason)))
}
