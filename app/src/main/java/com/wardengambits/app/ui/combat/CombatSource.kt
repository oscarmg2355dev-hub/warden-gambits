package com.wardengambits.app.ui.combat

import com.wardengambits.app.GameSession
import com.wardengambits.engine.model.CombatSetup
import com.wardengambits.engine.model.Outcome
import com.wardengambits.engine.story.NodeId
import com.wardengambits.engine.story.StoryAction
import com.wardengambits.engine.story.StoryEngine
import com.wardengambits.engine.story.StoryProfile
import com.wardengambits.engine.story.TreeShape
import com.wardengambits.engine.story.ZoneInfo
import com.wardengambits.engine.story.ZoneProgress

/** Where a combat comes from: the story's pending fight, or a practice fight that never touches the profile. */
sealed interface CombatSource {
    /** The fight to run; null when there is nothing to fight. */
    val setup: CombatSetup?

    /** Warden level shown before the fight. */
    val levelBefore: Int

    /** Victories of the profile when the fight started (for the defeat summary). */
    val winsBefore: Int

    fun rivalNames(engine: StoryEngine): List<String>

    /** Zone and level label of the fight in [engine]'s language. */
    fun progress(engine: StoryEngine): ZoneProgress
}

/** The story's pending combat: reports its outcome to the [GameSession]. */
class StoryCombatSource(private val session: GameSession, storyEngine: StoryEngine) : CombatSource {
    private val profileAtStart: StoryProfile = session.profile.value
    override val setup: CombatSetup? = storyEngine.combatSetupFor(profileAtStart)
    override val levelBefore: Int get() = setup?.player?.level ?: profileAtStart.run.level
    override val winsBefore: Int get() = profileAtStart.run.wins

    override fun rivalNames(engine: StoryEngine): List<String> =
        engine.node(profileAtStart.run.node).encounter?.rivals?.map { it.name }.orEmpty()

    override fun progress(engine: StoryEngine): ZoneProgress = engine.zoneProgress(profileAtStart)

    fun report(outcome: Outcome, nowMillis: Long) {
        session.dispatch(StoryAction.CombatFinished(outcome, nowMillis))
    }
}

/** A practice fight at [node]: reports nothing, the profile stays untouched. */
class PracticeCombatSource(private val node: NodeId, storyEngine: StoryEngine) : CombatSource {
    override val setup: CombatSetup? = storyEngine.practiceSetup(node)
    override val levelBefore: Int get() = setup?.player?.level ?: 1
    override val winsBefore: Int get() = 0

    override fun rivalNames(engine: StoryEngine): List<String> =
        runCatching { engine.node(node).encounter?.rivals?.map { it.name } }.getOrNull().orEmpty()

    override fun progress(engine: StoryEngine): ZoneProgress =
        ZoneProgress(
            runCatching { engine.zone(node.zoneId) }.getOrDefault(ZoneInfo(node.zoneId, node.zoneId)),
            node.level,
            TreeShape.LEVELS,
        )
}
