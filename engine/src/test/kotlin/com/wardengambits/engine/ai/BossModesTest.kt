package com.wardengambits.engine.ai

import com.wardengambits.engine.TEST_TUNING
import com.wardengambits.engine.action.CombatAction
import com.wardengambits.engine.event.CombatEvent
import com.wardengambits.engine.model.ActiveEffect
import com.wardengambits.engine.model.BossMode
import com.wardengambits.engine.model.CombatSetup
import com.wardengambits.engine.model.CombatState
import com.wardengambits.engine.model.EffectKind
import com.wardengambits.engine.model.FighterId
import com.wardengambits.engine.model.FighterSpec
import com.wardengambits.engine.model.PLAYER_ID
import com.wardengambits.engine.model.Reaction
import com.wardengambits.engine.model.RivalSpec
import com.wardengambits.engine.rules.CombatEngine
import com.wardengambits.engine.story.StoryEngine
import com.wardengambits.engine.story.content.EncounterContent
import com.wardengambits.engine.story.content.ModesContent
import com.wardengambits.engine.story.content.RivalContent
import com.wardengambits.engine.story.fakeContent
import com.wardengambits.engine.withFighter
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

/** Boss attack and defense modes (boss-modes spec §2). */
class BossModesTest {
    private val rival = FighterId(1)
    private val defense = listOf("2132", "21", "212", "2121", "21212", "12", "121")
    private val bossModes = BossModes(defenseBelowPct = 50, attackAbovePct = 70, maxDefenses = 3, defense = defense)
    private val engine = CombatEngine(tuning = TEST_TUNING)

    /** A resolved rival cast: the tick it resolved on, its sequence and whether it was chained. */
    private data class Cast(val tick: Int, val sequence: String, val chained: Boolean = false)

    private fun start(modes: BossModes? = bossModes, everyNTicks: Int = 2, seed: Long = 1L): CombatState {
        val brain = PatternBrain(listOf("12", "121"), everyNTicks, modes = modes)
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

    private fun Run.boss() = state.fighter(rival)

    /** Daze on the boss active on ticks [from]..[from] + [ticks] − 1. */
    private fun CombatState.dazed(from: Int, ticks: Int): CombatState {
        val dazed = ActiveEffect(EffectKind.DAZED, PLAYER_ID, "", 0, remainingTicks = ticks, activeFrom = from)
        return withFighter(rival) { copy(effects = effects + dazed) }
    }

    @Test
    fun attacksAboveHalf() {
        val result = run(start().atHpPct(51), 8)
        assertEquals(listOf(Cast(2, "12"), Cast(4, "121", true), Cast(6, "12"), Cast(8, "121", true)), result.casts())
        assertEquals(BossMode.ATTACK, result.boss().bossMode)
        assertEquals(0, result.boss().defenses)
    }

    @Test
    fun entersDefenseBelowHalfAndCastsEveryTick() {
        val result = run(start().atHpPct(20), 9)
        assertEquals(
            listOf(
                Cast(2, "2132"), Cast(3, "21"), Cast(4, "212", true), Cast(5, "2121", true), Cast(6, "21212", true),
                Cast(7, "12"), Cast(8, "121", true), Cast(9, "2132"),
            ),
            result.casts(),
        )
        assertEquals(BossMode.DEFENSE, result.boss().bossMode)
        assertEquals(1, result.boss().defenses)
    }

    @Test
    fun returnsToAttackAt70() {
        val defending = run(start().atHpPct(20), 3)
        assertEquals(BossMode.DEFENSE, defending.boss().bossMode)
        val result = run(defending.state.atHpPct(70), 5)
        assertEquals(listOf(Cast(4, "12"), Cast(6, "121", true), Cast(8, "12")), result.casts())
        assertEquals(BossMode.ATTACK, result.boss().bossMode)
        assertEquals(1, result.boss().defenses)
    }

    @Test
    fun atMostMaxDefenses() {
        var state = start()
        repeat(3) { round ->
            val defending = run(state.atHpPct(40), 2)
            assertEquals(BossMode.DEFENSE, defending.boss().bossMode)
            assertEquals(round + 1, defending.boss().defenses)
            state = run(defending.state.atHpPct(90), 1).state
            assertEquals(BossMode.ATTACK, state.fighter(rival).bossMode)
        }
        val last = run(state.atHpPct(10), 10)
        assertEquals(BossMode.ATTACK, last.boss().bossMode)
        assertEquals(3, last.boss().defenses)
        assertTrue(last.casts().all { it.sequence == "12" || it.sequence == "121" }, "only attack casts: ${last.casts()}")
        assertEquals(5, last.casts().size)
    }

    @Test
    fun dazedInDefenseSkipsRemainingHeals() {
        val shielded = run(start().atHpPct(20), 2)
        assertEquals(listOf("2132"), shielded.casts().map { it.sequence })
        val result = run(shielded.state.dazed(from = 3, ticks = 3), 6)
        assertEquals(listOf(Cast(6, "12"), Cast(7, "121", true), Cast(8, "2132")), result.casts())
        assertEquals(BossMode.DEFENSE, result.boss().bossMode)
    }

    /** The last allowed defense runs its pattern once (defense.size casts) and then the boss attacks whatever its HP. */
    @Test
    fun lastDefenseRunsOneCycleThenAttacks() {
        val result = run(start(modes = bossModes.copy(maxDefenses = 1)).atHpPct(20), 12)
        assertEquals(
            listOf(
                2 to "2132", 3 to "21", 4 to "212", 5 to "2121", 6 to "21212", 7 to "12", 8 to "121",
                10 to "12", 12 to "121",
            ),
            result.casts().map { it.tick to it.sequence },
        )
        assertEquals(BossMode.ATTACK, result.boss().bossMode)
        assertEquals(1, result.boss().defenses)
        // A Daze skip counts as reaching the damage tail: the cycle ends after the tail.
        val dazed = run(start(modes = bossModes.copy(maxDefenses = 1)).atHpPct(20).dazed(from = 1, ticks = 2), 8)
        assertEquals(listOf(3 to "12", 4 to "121", 6 to "12", 8 to "121"), dazed.casts().map { it.tick to it.sequence })
        assertEquals(BossMode.ATTACK, dazed.boss().bossMode)
    }

    /** Before the last defense the boss keeps looping its defense while it is below attackAbovePct. */
    @Test
    fun earlierDefensesStillLoopUntilAttackAbove() {
        val result = run(start().atHpPct(20), 9)
        assertEquals(
            listOf("2132", "21", "212", "2121", "21212", "12", "121", "2132"),
            result.casts().map { it.sequence },
        )
        assertEquals(BossMode.DEFENSE, result.boss().bossMode)
        assertEquals(1, result.boss().defenses)
    }

    /** Bree's tutorial boss (40/60/1, `21, 212, 12`) cannot loop its defense against a player that barely hurts it. */
    @Test
    fun breeDefenseCannotLoop() {
        val bree = BossModes(defenseBelowPct = 40, attackAbovePct = 60, maxDefenses = 1, defense = listOf("21", "212", "12"))
        val brain = PatternBrain(listOf("12", "121", "11", "31"), 4, modes = bree)
        val setup = CombatSetup(FighterSpec("Player", 5), listOf(RivalSpec("Cabecilla", 3, brain, isBoss = true)))
        val result = run(engine.start(setup, 7L).atHpPct(30), 16)
        assertEquals(
            listOf(4 to "21", 5 to "212", 6 to "12", 8 to "12", 12 to "121", 16 to "11"),
            result.casts().map { it.tick to it.sequence },
        )
        assertEquals(BossMode.ATTACK, result.boss().bossMode)
        assertEquals(1, result.boss().defenses)
    }

    /** A Dazed defender neither queues nor gets rejected every tick (no event noise); the casts are the same. */
    @Test
    fun dazedDefenderStaysQuiet() {
        val shielded = run(start().atHpPct(20), 2)
        val result = run(shielded.state.dazed(from = 3, ticks = 3), 6)
        val noisy = result.events.filter { (tick, event) ->
            tick in 3..5 && (event as? CombatEvent.ComboQueued)?.fighter == rival ||
                tick in 3..5 && (event as? CombatEvent.ComboRejected)?.fighter == rival
        }
        assertEquals(emptyList(), noisy)
        assertEquals(listOf(Cast(6, "12"), Cast(7, "121", true), Cast(8, "2132")), result.casts())
    }

    @Test
    fun dazedOnEntryStartsWithDamage() {
        val result = run(start().atHpPct(20).dazed(from = 1, ticks = 2), 5)
        assertEquals(listOf(Cast(3, "12"), Cast(4, "121", true), Cast(5, "2132")), result.casts())
        assertEquals(BossMode.DEFENSE, result.boss().bossMode)
        assertEquals(1, result.boss().defenses)
    }

    @Test
    fun noFlipWithinOneTick() {
        // Between both thresholds the boss keeps its mode: an attacking boss at 60 % does not defend...
        val attacking = run(start().atHpPct(60), 4)
        assertEquals(BossMode.ATTACK, attacking.boss().bossMode)
        // ...and a defending one at 60 % keeps casting its defense every tick.
        val defending = run(start().atHpPct(20), 2)
        val band = run(defending.state.atHpPct(60), 3)
        assertEquals(listOf(Cast(3, "21"), Cast(4, "212", true), Cast(5, "2121", true)), band.casts())
        assertEquals(BossMode.DEFENSE, band.boss().bossMode)
        // The tick it returns to attack (a due tick) it makes one transition and casts its attack pattern from the start.
        val back = run(band.state.atHpPct(80), 1)
        assertEquals(listOf(Cast(6, "12")), back.casts())
        assertEquals(BossMode.ATTACK, back.boss().bossMode)
        assertEquals(1, back.boss().defenses)
    }

    @Test
    fun potionCoversDefenseCasts() {
        fun Run.potionThenCast(sequence: String) {
            val events = events.map { it.second }
            val potion = events.indexOfFirst { it is CombatEvent.PotionUsed && it.fighter == rival }
            val cast = events.indexOfFirst { it is CombatEvent.ComboResolved && it.fighter == rival && it.combo.sequence == sequence }
            assertTrue(potion >= 0 && cast > potion, "potion then $sequence, got $events")
        }
        val shielded = run(start().withFighter(rival) { copy(power = 0) }.atHpPct(20), 2)
        shielded.potionThenCast("2132")
        // Odd tick, outside the attack rhythm: the defense still drinks to pay its heal.
        run(shielded.state.withFighter(rival) { copy(power = 0) }, 1).potionThenCast("21")
    }

    @Test
    fun rivalsWithoutModesUnchanged() {
        assertEquals(PatternBrain(listOf("12", "121"), 2), PatternBrain(listOf("12", "121"), 2, null, null))
        val result = run(start(modes = null).atHpPct(20), 8)
        assertEquals(listOf("12", "121", "12", "121"), result.casts().map { it.sequence })
        val fighter = result.boss()
        assertEquals(BossMode.ATTACK, fighter.bossMode)
        assertEquals(0, fighter.defenses)
        assertEquals(0, fighter.modeCasts)
    }

    @Test
    fun sameSeedSameEvents() {
        val default = CombatEngine()
        fun fight() = run(start(seed = 42L).atHpPct(49), 40, default).let { it.state to it.events }
        val (state, events) = fight()
        assertEquals(state to events, fight())
        assertTrue(events.any { (_, event) -> event is CombatEvent.ComboResolved && event.combo.sequence == "2132" })
    }

    @Test
    fun invalidModesAreRejected() {
        assertFailsWith<IllegalArgumentException> { BossModes(0, 70, 3, defense) }
        assertFailsWith<IllegalArgumentException> { BossModes(70, 70, 3, defense) }
        assertFailsWith<IllegalArgumentException> { BossModes(50, 101, 3, defense) }
        assertFailsWith<IllegalArgumentException> { BossModes(50, 70, 0, defense) }
        assertFailsWith<IllegalArgumentException> { BossModes(50, 70, 3, emptyList()) }
        assertFailsWith<IllegalArgumentException> { BossModes(50, 70, 3, listOf("21", "99")) }
        assertFailsWith<IllegalArgumentException> {
            PatternBrain(listOf("12"), 1, Reaction(listOf(50), null, listOf("21")), bossModes)
        }
    }

    @Test
    fun contentModesReachTheBrain() {
        val json = """{"name":"Jefe","portrait":"boss_bree","pattern":["12"],
            "modes":{"defenseBelowPct":50,"attackAbovePct":70,"maxDefenses":3,
            "defense":["2132","21","212","2121","21212","12","121"]}}"""
        val content = Json.decodeFromString<RivalContent>(json)
        assertEquals(ModesContent(50, 70, 3, defense), content.modes)
        val story = StoryEngine(fakeContent(mapOf("bree_1_0" to EncounterContent(listOf(content)))))
        val spec = story.combatSetupFor(story.newGame())!!.rivals.single()
        assertEquals(bossModes, spec.modes)
        assertEquals(PatternBrain(listOf("12"), 1, modes = bossModes), spec.brain)
    }
}
