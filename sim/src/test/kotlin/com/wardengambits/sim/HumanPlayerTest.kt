package com.wardengambits.sim

import com.wardengambits.engine.ai.PatternBrain
import com.wardengambits.engine.combo.ComboChain
import com.wardengambits.engine.combo.ComboFamily
import com.wardengambits.engine.combo.Combos
import com.wardengambits.engine.model.ActiveEffect
import com.wardengambits.engine.model.CombatSetup
import com.wardengambits.engine.model.CombatState
import com.wardengambits.engine.model.EffectKind
import com.wardengambits.engine.model.Fighter
import com.wardengambits.engine.model.FighterId
import com.wardengambits.engine.model.FighterSpec
import com.wardengambits.engine.model.PLAYER_ID
import com.wardengambits.engine.model.RivalSpec
import com.wardengambits.engine.rules.CombatEngine
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class HumanPlayerTest {
    private fun state(playerLevel: Int = 5, vararg rivalLevels: Int = intArrayOf(5)): CombatState =
        CombatEngine().start(
            CombatSetup(
                FighterSpec("P", playerLevel),
                rivalLevels.map { RivalSpec("R", it, PatternBrain(listOf("12"))) },
            ),
            seed = 1L,
        )

    private fun bossState(playerLevel: Int = 5, bossLevel: Int = 5, escort: Boolean = false): CombatState =
        CombatEngine().start(
            CombatSetup(
                FighterSpec("P", playerLevel),
                listOf(RivalSpec("Boss", bossLevel, PatternBrain(listOf("12")), isBoss = true)) +
                    if (escort) listOf(RivalSpec("Escort", 1, PatternBrain(listOf("12")))) else emptyList(),
            ),
            seed = 1L,
        )

    private fun CombatState.with(id: FighterId, change: Fighter.() -> Fighter): CombatState =
        copy(fighters = fighters.map { if (it.id == id) it.change() else it })

    private fun CombatState.playerHpPct(pct: Int): CombatState = with(PLAYER_ID) { copy(hp = stats.maxHp * pct / 100) }

    private fun CombatState.playerPower(power: Int): CombatState = with(PLAYER_ID) { copy(power = power) }

    private fun CombatState.chain(family: ComboFamily?, tier: Int): CombatState =
        with(PLAYER_ID) { copy(chain = ComboChain(family, tier)) }

    private fun CombatState.playerEffect(kind: EffectKind, combo: String = "x", ticks: Int = 20): CombatState =
        with(PLAYER_ID) { copy(effects = effects + ActiveEffect(kind, PLAYER_ID, combo, 0, ticks)) }

    private val opened = PolicyMemory(dazeOpened = true)

    private fun choose(skill: Skill, state: CombatState, memory: PolicyMemory = PolicyMemory()) =
        PlayerPolicy.choose(skill, state, memory).combo?.sequence

    @Test
    fun `cadence per skill`() {
        val ticks = 1..8
        assertEquals(listOf(1, 3, 5, 7), ticks.filter { HumanPlayer(Skill.NOVATO).actsOnTick(it) })
        assertEquals(listOf(1, 2, 4, 5, 7, 8), ticks.filter { HumanPlayer(Skill.MEDIO).actsOnTick(it) })
        assertEquals(ticks.toList(), ticks.filter { HumanPlayer(Skill.EXPERTO).actsOnTick(it) })
    }

    @Test
    fun `novato heals with the cura chain up to tier 2 below 30 percent`() {
        assertEquals("21", choose(Skill.NOVATO, state().playerHpPct(29)))
        assertEquals("212", choose(Skill.NOVATO, state().playerHpPct(29).chain(ComboFamily.HEAL, 1)))
        assertEquals("21", choose(Skill.NOVATO, state().playerHpPct(29).chain(ComboFamily.HEAL, 2)))
        assertEquals(null, choose(Skill.NOVATO, state().playerHpPct(29).chain(ComboFamily.HEAL, 1).playerPower(17)))
        assertEquals("12", choose(Skill.NOVATO, state().playerHpPct(35)))
    }

    @Test
    fun `novato chains the spear up to tier 3 and waits for the next tier`() {
        assertEquals("12", choose(Skill.NOVATO, state()))
        assertEquals("121", choose(Skill.NOVATO, state().chain(ComboFamily.SPEAR_A, 1)))
        assertEquals("1212", choose(Skill.NOVATO, state().chain(ComboFamily.SPEAR_A, 2)))
        assertEquals("12", choose(Skill.NOVATO, state().chain(ComboFamily.SPEAR_A, 3)))
        assertEquals(null, choose(Skill.NOVATO, state().chain(ComboFamily.SPEAR_A, 2).playerPower(25)))
        assertEquals("12", choose(Skill.NOVATO, state().chain(ComboFamily.HEAL, 2).playerPower(25)))
        assertEquals(null, choose(Skill.NOVATO, state().playerPower(9)))
    }

    @Test
    fun `novato uses no boss tricks and no area`() {
        assertEquals("12", choose(Skill.NOVATO, bossState()))
        assertEquals("12", choose(Skill.NOVATO, state(5, 5, 5)))
    }

    @Test
    fun `medio advances the spear chain tier by tier and starts a new cycle after tier 4`() {
        assertEquals("12", choose(Skill.MEDIO, state()))
        assertEquals("121", choose(Skill.MEDIO, state().chain(ComboFamily.SPEAR_A, 1)))
        assertEquals("1212", choose(Skill.MEDIO, state().chain(ComboFamily.SPEAR_A, 2)))
        assertEquals("12121", choose(Skill.MEDIO, state().chain(ComboFamily.SPEAR_A, 3)))
        assertEquals("12", choose(Skill.MEDIO, state().chain(ComboFamily.SPEAR_A, 4)))
    }

    @Test
    fun `medio waits for the next spear tier and restarts at tier 1 only when the chain broke`() {
        assertEquals(null, choose(Skill.MEDIO, state().chain(ComboFamily.SPEAR_A, 2).playerPower(25)))
        assertEquals("12", choose(Skill.MEDIO, state().playerPower(25)))
        assertEquals("12", choose(Skill.MEDIO, state().chain(ComboFamily.HEAL, 2).playerPower(25)))
    }

    @Test
    fun `medio heals with the heal chain below 40 percent`() {
        assertEquals("21", choose(Skill.MEDIO, state().playerHpPct(39)))
        assertEquals("212", choose(Skill.MEDIO, state().playerHpPct(39).chain(ComboFamily.HEAL, 1)))
        assertEquals("21212", choose(Skill.MEDIO, state().playerHpPct(39).chain(ComboFamily.HEAL, 3)))
        assertEquals(null, choose(Skill.MEDIO, state().playerHpPct(39).chain(ComboFamily.HEAL, 3).playerPower(30)))
        assertEquals("12", choose(Skill.MEDIO, state().playerHpPct(41)))
    }

    @Test
    fun `area chain against two or more rivals`() {
        assertEquals("32", choose(Skill.MEDIO, state(5, 5, 5)))
        assertEquals("323", choose(Skill.MEDIO, state(5, 5, 5).chain(ComboFamily.AREA, 1)))
        assertEquals(null, choose(Skill.MEDIO, state(5, 5, 5).chain(ComboFamily.AREA, 2).playerPower(30)))
    }

    @Test
    fun `targets the lowest hp rival`() {
        val s = state(5, 5, 5).with(FighterId(2)) { copy(hp = 10) }
        assertEquals(FighterId(2), PlayerPolicy.choose(Skill.MEDIO, s).target)
    }

    @Test
    fun `against a boss opens with one aturdir chain up to tier 3 on the boss`() {
        val boss = bossState(escort = true).with(FighterId(2)) { copy(hp = 1) }
        val first = PlayerPolicy.choose(Skill.MEDIO, boss)
        assertEquals("31", first.combo?.sequence)
        assertEquals(FighterId(1), first.target)
        assertEquals("313", choose(Skill.MEDIO, boss.chain(ComboFamily.DAZE, 1)))
        assertEquals("3131", choose(Skill.EXPERTO, boss.chain(ComboFamily.DAZE, 2)))
        assertEquals(null, choose(Skill.MEDIO, boss.chain(ComboFamily.DAZE, 2).playerPower(25)))
        assertNotEquals("31", choose(Skill.MEDIO, bossState(), opened))
        assertEquals("12", choose(Skill.MEDIO, state()))
    }

    @Test
    fun `human player remembers that the aturdir opening is done`() {
        val player = HumanPlayer(Skill.MEDIO)
        assertEquals("3131", player.decide(bossState().chain(ComboFamily.DAZE, 2)).combo?.sequence)
        assertEquals("2132", player.decide(bossState().copy(tick = 5)).combo?.sequence)
    }

    @Test
    fun `the aturdir opening ends as soon as the boss is dazed`() {
        val dazed = bossState().chain(ComboFamily.DAZE, 2)
            .with(FighterId(1)) { copy(effects = listOf(ActiveEffect(EffectKind.DAZED, FighterId(1), "", 0, 5))) }
        assertEquals("2132", choose(Skill.MEDIO, dazed))
        val player = HumanPlayer(Skill.MEDIO)
        player.decide(dazed)
        assertEquals("2132", player.decide(bossState().copy(tick = 9)).combo?.sequence)
    }

    @Test
    fun `no aturdir opening while the boss is immune to daze`() {
        val immune = bossState().with(FighterId(1)) {
            copy(effects = listOf(ActiveEffect(EffectKind.SHIELD_TACTICS, FighterId(1), "2131", 0, 50)))
        }
        assertEquals("2132", choose(Skill.MEDIO, immune))
        assertEquals("2132", choose(Skill.MEDIO, immune.chain(ComboFamily.DAZE, 1)))
    }

    @Test
    fun `against a boss master shield with 40 power, otherwise guard`() {
        assertEquals("2132", choose(Skill.MEDIO, bossState(), opened))
        assertEquals("2132", choose(Skill.MEDIO, bossState().playerPower(40), opened))
        assertEquals("22", choose(Skill.MEDIO, bossState().playerPower(39), opened))
        assertEquals("12", choose(Skill.MEDIO, bossState().playerPower(39).playerEffect(EffectKind.GUARD), opened))
        assertEquals("12", choose(Skill.MEDIO, bossState().playerEffect(EffectKind.SHIELD_MASTER), opened))
        assertNotEquals("2132", choose(Skill.MEDIO, state(5, 9), opened))
        assertNotEquals("22", choose(Skill.MEDIO, state(5, 9).playerPower(30), opened))
    }

    @Test
    fun `shield at most once per eight ticks`() {
        val later = bossState().copy(tick = 10)
        assertNotEquals("2132", choose(Skill.MEDIO, later, PolicyMemory(lastShieldTick = 4, dazeOpened = true)))
        assertEquals("2132", choose(Skill.MEDIO, later, PolicyMemory(lastShieldTick = 3, dazeOpened = true)))
    }

    @Test
    fun `shield always leaves power for the cheapest spear`() {
        val shield = Combos.find("2132")!!
        val spear = Combos.find("12")!!
        for (power in 0..100) {
            if (choose(Skill.MEDIO, bossState().playerPower(power), opened) == "2132") {
                assertTrue(power - shield.cost >= spear.cost, "shield at power $power")
            }
        }
    }

    @Test
    fun `experto uses a direct hit only when it kills`() {
        val weak = state().with(FighterId(1)) { copy(hp = 5) }.playerEffect(EffectKind.FURY, ticks = 25)
        assertEquals("123", choose(Skill.EXPERTO, weak))
        assertNotEquals("123", choose(Skill.MEDIO, weak))
        assertNotEquals("123", choose(Skill.EXPERTO, state().playerEffect(EffectKind.FURY, ticks = 25)))
    }

    @Test
    fun `experto drains against groups after a full area chain`() {
        assertEquals("32", choose(Skill.EXPERTO, state(5, 5, 5)))
        assertEquals("3123", choose(Skill.EXPERTO, state(5, 5, 5).chain(ComboFamily.AREA, 4)))
        assertEquals("31232", choose(Skill.EXPERTO, state(5, 5, 5).chain(ComboFamily.DRAIN, 1)))
        assertEquals("32", choose(Skill.EXPERTO, state(5, 5, 5).chain(ComboFamily.AREA, 4).playerEffect(EffectKind.DRAIN, "31232")))
        assertEquals("32", choose(Skill.MEDIO, state(5, 5, 5).chain(ComboFamily.AREA, 4)))
    }

    @Test
    fun `experto casts fury before starting a spear chain, never inside one`() {
        assertEquals("11", choose(Skill.EXPERTO, state()))
        assertEquals("12", choose(Skill.EXPERTO, state().playerEffect(EffectKind.FURY, ticks = 25)))
        assertEquals("121", choose(Skill.EXPERTO, state().chain(ComboFamily.SPEAR_A, 1)))
        assertEquals("12", choose(Skill.EXPERTO, state().playerEffect(EffectKind.GUARD)))
        assertEquals("12", choose(Skill.MEDIO, state()))
    }

    @Test
    fun `experto keeps no power reserve`() {
        val furious = state().playerEffect(EffectKind.FURY, ticks = 25)
        assertEquals("12121", choose(Skill.EXPERTO, furious.chain(ComboFamily.SPEAR_A, 3).playerPower(34)))
        assertEquals("21", choose(Skill.EXPERTO, furious.playerHpPct(30).playerPower(10)))
    }

    @Test
    fun `does nothing while dazed`() {
        val dazed = state().with(PLAYER_ID) { copy(effects = listOf(ActiveEffect(EffectKind.DAZED, FighterId(1), "", 0, 2))) }
        Skill.entries.forEach { assertEquals(null, choose(it, dazed)) }
    }

    @Test
    fun `never selects an unaffordable combo`() {
        val starts = listOf(state(5, 5), state(5, 5, 5), state(5, 7, 3, 3), bossState(), bossState(escort = true))
        for (skill in Skill.entries) {
            for (base in starts) {
                for (family in listOf(null) + ComboFamily.entries) {
                    for (tier in 0..4) {
                        for (hpPct in listOf(10, 35, 50, 100)) {
                            for (power in 0..100 step 3) {
                                val s = base.chain(family, tier).playerHpPct(hpPct).playerPower(power)
                                for (memory in listOf(PolicyMemory(), opened)) {
                                    val combo = PlayerPolicy.choose(skill, s, memory).combo
                                    assertTrue(combo == null || combo.cost <= power, "$skill hp=$hpPct power=$power chose $combo")
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun healerState(vararg patterns: List<String>): CombatState =
        CombatEngine().start(
            CombatSetup(FighterSpec("P", 5), patterns.map { RivalSpec("R", 5, PatternBrain(it)) }),
            seed = 1L,
        )

    @Test
    fun `focuses the spear chain on the lowest hp healer instead of area against two healers`() {
        val healers = healerState(listOf("12", "21"), listOf("12", "21")).with(FighterId(2)) { copy(hp = hp - 5) }
        val decision = PlayerPolicy.choose(Skill.MEDIO, healers)
        assertEquals(FighterId(2), decision.target)
        assertEquals("12", decision.combo?.sequence)
        assertEquals("1212", choose(Skill.EXPERTO, healers.chain(ComboFamily.SPEAR_A, 2)))
    }

    @Test
    fun `with a single healer keeps lowest hp target and area`() {
        val mixed = healerState(listOf("12"), listOf("12", "21")).with(FighterId(1)) { copy(hp = 10) }
        val decision = PlayerPolicy.choose(Skill.MEDIO, mixed)
        assertEquals(FighterId(1), decision.target)
        assertEquals("32", decision.combo?.sequence)
    }

    @Test
    fun `medio and experto recover power when they cannot pay the next combo`() {
        for (skill in listOf(Skill.MEDIO, Skill.EXPERTO)) {
            assertEquals("321", choose(skill, state().playerPower(5)), "$skill spear")
            assertEquals("321", choose(skill, state().playerPower(0)), "$skill spear at 0")
            assertEquals("321", choose(skill, state(5, 5, 5).playerPower(5)), "$skill area")
            assertEquals("321", choose(skill, bossState().playerPower(5)), "$skill daze opening")
        }
    }

    @Test
    fun `novato never recovers power`() {
        assertEquals(null, choose(Skill.NOVATO, state().playerPower(5)))
        assertEquals(null, choose(Skill.NOVATO, state().chain(ComboFamily.SPEAR_A, 2).playerPower(0)))
    }

    @Test
    fun `no recover in immediate danger`() {
        assertEquals(null, choose(Skill.MEDIO, state().playerHpPct(39).playerPower(5)))
        assertEquals(null, choose(Skill.EXPERTO, state().playerHpPct(20).playerPower(0)))
    }

    @Test
    fun `recover breaks a running chain only when the wait is long`() {
        // 1212 costs 26: 20 power is a 2-tick wait (keep the chain), 0 power a 6-tick wait (recover).
        assertEquals(null, choose(Skill.MEDIO, state().chain(ComboFamily.SPEAR_A, 2).playerPower(20)))
        assertEquals("321", choose(Skill.MEDIO, state().chain(ComboFamily.SPEAR_A, 2).playerPower(0)))
        assertEquals(null, choose(Skill.EXPERTO, state(5, 5, 5).chain(ComboFamily.AREA, 2).playerPower(20)))
        assertEquals("321", choose(Skill.EXPERTO, state(5, 5, 5).chain(ComboFamily.AREA, 3).playerPower(10)))
    }
}
