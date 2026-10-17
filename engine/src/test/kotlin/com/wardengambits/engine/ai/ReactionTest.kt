package com.wardengambits.engine.ai

import com.wardengambits.engine.TEST_TUNING
import com.wardengambits.engine.action.CombatAction
import com.wardengambits.engine.event.CombatEvent
import com.wardengambits.engine.model.ActiveEffect
import com.wardengambits.engine.model.CombatSetup
import com.wardengambits.engine.model.CombatState
import com.wardengambits.engine.model.EffectKind
import com.wardengambits.engine.model.FighterId
import com.wardengambits.engine.model.FighterSpec
import com.wardengambits.engine.model.Outcome
import com.wardengambits.engine.model.PLAYER_ID
import com.wardengambits.engine.model.Reaction
import com.wardengambits.engine.model.RivalSpec
import com.wardengambits.engine.rules.CombatEngine
import com.wardengambits.engine.story.StoryEngine
import com.wardengambits.engine.story.content.EncounterContent
import com.wardengambits.engine.story.content.ReactionContent
import com.wardengambits.engine.story.content.RivalContent
import com.wardengambits.engine.story.fakeContent
import kotlinx.serialization.json.Json
import com.wardengambits.engine.withFighter
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** Rival reactions by HP (rival-healing spec §2): shield + heal chain at HP thresholds. */
class ReactionTest {
    private val rival = FighterId(1)
    private val bossReaction = Reaction(listOf(50, 25), "2131", listOf("21", "212", "2121", "21212"))
    private val engine = CombatEngine(tuning = TEST_TUNING)

    /** A resolved rival cast: the tick it resolved on, its sequence and whether it was chained. */
    private data class Cast(val tick: Int, val sequence: String, val chained: Boolean = false)

    private fun start(reaction: Reaction? = bossReaction, everyNTicks: Int = 2, seed: Long = 1L): CombatState {
        val brain = PatternBrain(listOf("12", "121"), everyNTicks, reaction)
        val setup = CombatSetup(FighterSpec("Player", 5), listOf(RivalSpec("Boss", 3, brain, isBoss = true)))
        return engine.start(setup, seed)
    }

    private fun CombatState.atHpPct(pct: Int): CombatState = withFighter(rival) { copy(hp = stats.maxHp * pct / 100) }

    private class Run(val state: CombatState, val events: List<Pair<Int, CombatEvent>>)

    private fun run(state: CombatState, ticks: Int, engine: CombatEngine = this.engine): Run {
        var current = state
        val events = mutableListOf<Pair<Int, CombatEvent>>()
        repeat(ticks) {
            if (current.outcome != null) return@repeat
            val step = engine.reduce(current, CombatAction.Tick)
            current = step.state
            events += step.events.map { current.tick to it }
        }
        return Run(current, events)
    }

    private fun Run.casts(): List<Cast> = events.mapNotNull { (tick, event) ->
        (event as? CombatEvent.ComboResolved)?.takeIf { it.fighter == rival }?.let { Cast(tick, it.combo.sequence, it.chained) }
    }

    @Test
    fun bossShieldsThenFullHealChain() {
        val state = start().withFighter(rival) { copy(castCount = 1) }.atHpPct(49)
        val casts = run(state, 8).casts()
        assertEquals(
            listOf(
                Cast(2, "2131"), Cast(3, "21"), Cast(4, "212", true), Cast(5, "2121", true), Cast(6, "21212", true),
                Cast(8, "121"),
            ),
            casts,
        )
    }

    @Test
    fun secondThresholdOnce() {
        val first = run(start().atHpPct(49), 7)
        assertEquals(listOf("2131", "21", "212", "2121", "21212"), first.casts().map { it.sequence })
        val second = run(first.state.atHpPct(20), 5)
        assertEquals(listOf("2131", "21", "212", "2121", "21212"), second.casts().map { it.sequence })
        val third = run(second.state.atHpPct(10), 8)
        assertEquals(listOf("12", "121", "12", "121"), third.casts().map { it.sequence })
    }

    @Test
    fun dazeCancelsReaction() {
        val shielded = run(start().atHpPct(49), 2)
        assertEquals(listOf("2131"), shielded.casts().map { it.sequence })
        val dazed = ActiveEffect(EffectKind.DAZED, PLAYER_ID, "", 0, remainingTicks = 3, activeFrom = 3)
        val state = shielded.state.withFighter(rival) { copy(effects = effects + dazed) }
        val after = run(state, 6)
        assertEquals(listOf(6 to "12", 8 to "121"), after.casts().map { it.tick to it.sequence })
        assertEquals(-1, after.state.fighter(rival).reactionStep)
    }

    @Test
    fun doubleThresholdJumpFiresOnce() {
        val above = run(start().atHpPct(60), 2)
        assertEquals(listOf("12"), above.casts().map { it.sequence })
        val after = run(above.state.atHpPct(20), 30)
        assertEquals(1, after.casts().count { it.sequence == "2131" })
        assertEquals(0b11, after.state.fighter(rival).reactionUsed)
    }

    @Test
    fun potionCoversReaction() {
        val state = start().withFighter(rival) { copy(power = 0) }.atHpPct(49)
        val result = run(state, 2)
        val events = result.events.map { it.second }
        val potion = events.indexOfFirst { it is CombatEvent.PotionUsed && it.fighter == rival }
        val shield = events.indexOfFirst { it is CombatEvent.ComboResolved && it.fighter == rival && it.combo.sequence == "2131" }
        assertTrue(potion >= 0 && shield > potion, "potion then shield, got $events")
    }

    @Test
    fun diesOnThresholdTick() {
        val lethal = ActiveEffect(EffectKind.DOT, PLAYER_ID, "12", 1_000_000, remainingTicks = 10, activeFrom = 2)
        val state = start().atHpPct(49).withFighter(rival) { copy(effects = listOf(lethal)) }
        val result = run(state, 10)
        assertEquals(Outcome.VICTORY, result.state.outcome)
        val defeated = result.events.indexOfFirst { (_, event) -> event == CombatEvent.FighterDefeated(rival) }
        assertTrue(defeated >= 0)
        val castsAfter = result.events.drop(defeated + 1).count { (_, event) -> event is CombatEvent.ComboResolved }
        assertEquals(0, castsAfter)
    }

    @Test
    fun noReactionNoChange() {
        assertEquals(PatternBrain(listOf("12", "121"), 2), PatternBrain(listOf("12", "121"), 2, null))
        val result = run(start(reaction = null).atHpPct(20), 8)
        assertEquals(listOf("12", "121", "12", "121"), result.casts().map { it.sequence })
        val fighter = result.state.fighter(rival)
        assertEquals(0, fighter.reactionUsed)
        assertEquals(-1, fighter.reactionStep)
    }

    @Test
    fun sameSeedSameEvents() {
        val default = CombatEngine()
        fun fight() = run(start(seed = 42L).atHpPct(49), 40, default).let { it.state to it.events }
        val (state, events) = fight()
        assertEquals(state to events, fight())
        assertTrue(events.any { (_, event) -> event is CombatEvent.ComboResolved && event.combo.sequence == "2131" })
    }

    @Test
    fun contentReactionReachesTheBrain() {
        val json = """{"name":"Jefe","portrait":"boss_bree","pattern":["12"],
            "reaction":{"atHpPct":[50,25],"shield":"2131","combos":["21","212","2121","21212"]}}"""
        val content = Json.decodeFromString<RivalContent>(json)
        assertEquals(ReactionContent(listOf(50, 25), "2131", listOf("21", "212", "2121", "21212")), content.reaction)
        val story = StoryEngine(fakeContent(mapOf("bree_1_0" to EncounterContent(listOf(content)))))
        val spec = story.combatSetupFor(story.newGame())!!.rivals.single()
        assertEquals(bossReaction, spec.reaction)
        assertEquals(PatternBrain(listOf("12"), 1, bossReaction), spec.brain)
    }
}
