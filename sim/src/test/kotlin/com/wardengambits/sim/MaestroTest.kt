package com.wardengambits.sim

import com.wardengambits.engine.action.CombatAction
import com.wardengambits.engine.ai.BossModes
import com.wardengambits.engine.ai.PatternBrain
import com.wardengambits.engine.ai.RivalBrain
import com.wardengambits.engine.combo.ComboChain
import com.wardengambits.engine.combo.ComboDefinition
import com.wardengambits.engine.combo.ComboFamily
import com.wardengambits.engine.combo.Input
import com.wardengambits.engine.event.CombatEvent
import com.wardengambits.engine.model.ActiveEffect
import com.wardengambits.engine.model.BossMode
import com.wardengambits.engine.model.CombatSetup
import com.wardengambits.engine.model.CombatState
import com.wardengambits.engine.model.EffectKind
import com.wardengambits.engine.model.Fighter
import com.wardengambits.engine.model.FighterId
import com.wardengambits.engine.model.FighterSpec
import com.wardengambits.engine.model.PLAYER_ID
import com.wardengambits.engine.model.Reaction
import com.wardengambits.engine.model.RivalSpec
import com.wardengambits.engine.random.GameRandom
import com.wardengambits.engine.rules.CombatEngine
import com.wardengambits.engine.story.Route
import java.io.File
import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/** The MAESTRO simulated player (rival-healing spec §4). */
class MaestroTest {
    private val engine = CombatEngine()
    private val heals = listOf("21", "212", "2121", "21212")
    private val boss = FighterId(1)

    private val defense = listOf("2132") + heals + listOf("12", "121")

    private fun bossSetup(pattern: List<String> = listOf("12", "121"), escort: Boolean = false) =
        CombatSetup(
            FighterSpec("P", 5),
            listOf(RivalSpec("Boss", 5, PatternBrain(pattern, 1, modes = BossModes(50, 70, 3, defense)), isBoss = true)) +
                if (escort) listOf(RivalSpec("Escort", 5, PatternBrain(listOf("12")))) else emptyList(),
        )

    private fun CombatState.with(id: FighterId, change: Fighter.() -> Fighter): CombatState =
        copy(fighters = fighters.map { if (it.id == id) it.change() else it })

    private fun choose(state: CombatState, memory: PolicyMemory = PolicyMemory(lastShieldTick = 1, dazeOpened = true)) =
        PlayerPolicy.choose(Skill.MAESTRO, state, memory)

    private companion object {
        const val OPENING_TICKS = 10
    }

    @Test
    fun maestroActsEveryTick() {
        assertEquals((1..8).toList(), (1..8).filter { HumanPlayer(Skill.MAESTRO).actsOnTick(it) })
    }

    /** Boss in its defense, [casts] combos into it (0..4 shield and heals, 5..6 its final damage combos). */
    private fun CombatState.defending(casts: Int = 1): CombatState = with(boss) { copy(bossMode = BossMode.DEFENSE, defenses = 1, modeCasts = casts) }

    @Test
    fun maestroDazesBossWhenDefenseStarts() {
        val inputs = Input.entries.associateBy { it.code }
        // The boss drops below half (and loses its effects) only after the opening, so only the defense explains a 31.
        var state = engine.start(bossSetup(), seed = 3L)
        val player = HumanPlayer(Skill.MAESTRO, engine.tuning)
        var bossShieldTick: Int? = null
        val decisions = mutableMapOf<Int, Decision>()
        while (state.outcome == null && state.tick < 20) {
            if (state.tick == OPENING_TICKS) state = state.with(boss) { copy(hp = stats.maxHp * 49 / 100, effects = emptyList()) }
            val decision = player.decide(state)
            decisions[state.tick + 1] = decision
            if (decision.target != null && decision.target != state.targetId) {
                state = engine.reduce(state, CombatAction.SelectTarget(decision.target)).state
            }
            decision.combo?.sequence?.forEach { state = engine.reduce(state, CombatAction.Press(PLAYER_ID, inputs.getValue(it))).state }
            if (decision.combo != null) state = engine.reduce(state, CombatAction.Execute(PLAYER_ID)).state
            val step = engine.reduce(state, CombatAction.Tick)
            state = step.state
            if (bossShieldTick == null && step.events.any { it is CombatEvent.ComboResolved && it.fighter == boss && it.combo.sequence == "2132" }) {
                bossShieldTick = state.tick
            }
        }
        val shieldTick = assertNotNull(bossShieldTick, "the boss never entered its defense")
        assertTrue(shieldTick > OPENING_TICKS)
        assertTrue((OPENING_TICKS - 2..shieldTick).none { decisions[it]?.combo?.family == ComboFamily.DAZE }, decisions.toString())
        val next = decisions.getValue(shieldTick + 1)
        assertEquals(boss, next.target)
        assertEquals("31", next.combo?.sequence)
    }

    @Test
    fun maestroChainsDazeDuringTheDefenseHealsUpToTierThree() {
        val reacting = engine.start(bossSetup(), seed = 1L).defending(casts = 2)
        assertEquals("313", choose(reacting.with(PLAYER_ID) { copy(chain = ComboChain(ComboFamily.DAZE, 1)) }).combo?.sequence)
        assertEquals("3131", choose(reacting.with(PLAYER_ID) { copy(chain = ComboChain(ComboFamily.DAZE, 2)) }).combo?.sequence)
        assertEquals("31", choose(reacting.with(PLAYER_ID) { copy(chain = ComboChain(ComboFamily.DAZE, 3)) }).combo?.sequence)
        assertTrue(choose(engine.start(bossSetup(), seed = 1L)).combo?.family != ComboFamily.DAZE)
    }

    @Test
    fun maestroChainsCureToTierFour() {
        val hurt = engine.start(bossSetup(), seed = 1L).with(PLAYER_ID) { copy(hp = stats.maxHp * 45 / 100, power = 100) }
        val chosen = (0..3).map { tier -> choose(hurt.with(PLAYER_ID) { copy(chain = ComboChain(ComboFamily.HEAL, tier)) }).combo?.sequence }
        assertEquals(heals, chosen)
        val broke = hurt.with(PLAYER_ID) { copy(power = 5) }
        assertEquals("321", choose(broke).combo?.sequence)
        assertTrue(PlayerPolicy.choose(Skill.EXPERTO, hurt, PolicyMemory(1, true)).combo?.family != ComboFamily.HEAL)
    }

    @Test
    fun maestroShieldsAgainstBoss() {
        assertEquals("2132", PlayerPolicy.choose(Skill.MAESTRO, engine.start(bossSetup(), 1L)).combo?.sequence)
        val dazer = engine.start(bossSetup(pattern = listOf("12", "31")), 1L)
        assertEquals("2131", PlayerPolicy.choose(Skill.MAESTRO, dazer).combo?.sequence)
        val player = HumanPlayer(Skill.MAESTRO)
        assertEquals("2131", player.decide(dazer).combo?.sequence)
        // Later it shields again as soon as its shield expired, and never while one is active.
        val expired = engine.start(bossSetup(pattern = listOf("12")), 1L).copy(tick = 70)
        assertEquals("2132", choose(expired).combo?.sequence)
        val shielded = expired.with(PLAYER_ID) { copy(effects = listOf(ActiveEffect(EffectKind.SHIELD_MASTER, PLAYER_ID, "2132", 0, 30))) }
        assertTrue(choose(shielded).combo?.family != ComboFamily.SHIELD)
    }

    /** Once the boss is past its shield and heals (its final damage combos) or back in attack, there is nothing to cut. */
    @Test
    fun maestroStopsDazingOnceTheDefenseHealsAreDone() {
        val start = engine.start(bossSetup(), seed = 1L)
        assertEquals("31", choose(start.defending(casts = 4)).combo?.sequence)
        assertTrue(choose(start.defending(casts = 5)).combo?.family != ComboFamily.DAZE)
        assertTrue(choose(start.defending(casts = 6)).combo?.family != ComboFamily.DAZE)
        // The defense loops: back at its shield it heals again.
        assertEquals("31", choose(start.defending(casts = 7)).combo?.sequence)
        assertTrue(choose(start.defending(casts = 2).with(boss) { copy(bossMode = BossMode.ATTACK) }).combo?.family != ComboFamily.DAZE)
        val dazed = ActiveEffect(EffectKind.DAZED, PLAYER_ID, "", 0, 2, activeFrom = 1)
        assertTrue(choose(start.defending(casts = 2).with(boss) { copy(effects = listOf(dazed)) }).combo?.family != ComboFamily.DAZE)
    }

    /** Escudo táctico gives full Daze immunity, so MAESTRO does not waste Aturdir on a shielded defending boss. */
    @Test
    fun maestroDoesNotDazeADefendingBossUnderEscudoTactico() {
        val tactics = ActiveEffect(EffectKind.SHIELD_TACTICS, boss, "2131", 0, 30, activeFrom = 1)
        val reacting = engine.start(bossSetup(), 1L).defending().with(boss) { copy(effects = listOf(tactics)) }
        assertTrue(choose(reacting).combo?.family != ComboFamily.DAZE)
    }

    @Test
    fun maestroHealsBeforeDazingADefendingBoss() {
        val reacting = engine.start(bossSetup(), 1L).defending()
            .with(PLAYER_ID) { copy(hp = stats.maxHp * 35 / 100) }
        assertEquals("21", choose(reacting).combo?.sequence)
        assertEquals("31", choose(reacting.with(PLAYER_ID) { copy(hp = stats.maxHp * 60 / 100) }).combo?.sequence)
    }

    /** A boss brain the policy must never query for its next combo: only the engine may. */
    private class OpaqueBrain : RivalBrain {
        override fun decide(state: CombatState, self: FighterId, random: GameRandom): ComboDefinition? = null

        override fun nextCast(state: CombatState, self: FighterId): ComboDefinition? = error("the player peeked at nextCast")
    }

    @Test
    fun maestroNeverPeeksAtTheRivalsNextCombo() {
        val setup = CombatSetup(
            FighterSpec("P", 5),
            listOf(RivalSpec("Boss", 5, OpaqueBrain(), isBoss = true), RivalSpec("Escort", 5, OpaqueBrain())),
        )
        val start = engine.start(setup, 1L)
        val states = listOf(
            start,
            start.copy(tick = 70),
            start.with(boss) { copy(bossMode = BossMode.DEFENSE, modeCasts = 1, hp = hp / 3) },
            start.with(PLAYER_ID) { copy(hp = stats.maxHp / 3) },
            start.with(FighterId(2)) { copy(hp = 0) },
        )
        for (state in states) {
            for (memory in listOf(PolicyMemory(), PolicyMemory(lastShieldTick = 1, dazeOpened = true))) {
                PlayerPolicy.choose(Skill.MAESTRO, state, memory)
            }
        }
        // Nor at the position in the pattern: the same visible state gives the same decision whatever the cast count.
        val patterned = engine.start(bossSetup(pattern = listOf("12", "121", "1212"), escort = true), 1L)
        for (memory in listOf(PolicyMemory(), PolicyMemory(lastShieldTick = 1, dazeOpened = true))) {
            val decisions = (0..5).map { casts -> PlayerPolicy.choose(Skill.MAESTRO, patterned.with(boss) { copy(castCount = casts) }, memory) }
            assertEquals(1, decisions.toSet().size, decisions.toString())
        }
    }

    @Test
    fun maestroFocusesHealer() {
        val healer = RivalSpec("Healer", 5, PatternBrain(listOf("12"), 1, Reaction(listOf(40), null, listOf("21212"))))
        val setup = CombatSetup(FighterSpec("P", 5), listOf(RivalSpec("A", 5, PatternBrain(listOf("12"))), healer))
        val start = engine.start(setup, 1L).with(FighterId(1)) { copy(hp = hp / 2) }
        val focused = PlayerPolicy.choose(Skill.MAESTRO, start)
        assertEquals(FighterId(2), focused.target)
        assertEquals("32", focused.combo?.sequence)
        assertEquals(FighterId(1), PlayerPolicy.choose(Skill.EXPERTO, start).target)
        val spent = start.with(FighterId(2)) { copy(reactionUsed = 1) }
        assertEquals(FighterId(1), PlayerPolicy.choose(Skill.MAESTRO, spent).target)
    }

    @Test
    fun reportIncludesMaestro() {
        val zone = Route.treeZones.first().id
        val rows = Skill.entries.flatMap { skill ->
            listOf(true, false).map { boss ->
                EncounterRow(zone, "${zone}_${if (boss) 7 else 1}_0", "R L1", boss, 1, skill, 90.0, 10.0, 50.0, 0)
            }
        }
        val adventures = Skill.entries.associateWith { skill ->
            aggregate(skill, listOf(AdventureRunSummary(listOf(zone), mapOf(zone to 1), true, false, emptyMap(), 0)))
        }
        val summary = summaryReport(rows, adventures)
        assertTrue(summary.lines().any { it.startsWith("MAESTRO") && "sin muertes" in it }, summary)
        assertTrue(summary.lines().any { it.startsWith("zona") && "MAESTRO" in it }, summary)
        val dir = Files.createTempDirectory("maestro").toFile()
        writeCsvs(dir, rows, adventures)
        assertTrue(File(dir, "encounters.csv").readLines().any { ",MAESTRO," in it })
        assertTrue(File(dir, "adventures.csv").readLines().any { it.startsWith("MAESTRO,") })
        dir.deleteRecursively()
    }
}
