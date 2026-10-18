package com.wardengambits.engine

import com.wardengambits.engine.ai.PatternBrain
import com.wardengambits.engine.model.CombatSetup
import com.wardengambits.engine.model.FighterSpec
import com.wardengambits.engine.model.LevelStats
import com.wardengambits.engine.model.RivalScaling
import com.wardengambits.engine.model.RivalSpec
import com.wardengambits.engine.model.Stats
import com.wardengambits.engine.rules.CombatEngine
import kotlin.test.Test
import kotlin.test.assertEquals

class RivalScalingTest {
    private val scaling = RivalScaling(hpPct = 80, attackPct = 75, healPct = 50, defensePct = 50, startPowerPct = 40, regenPct = 60)

    @Test
    fun identityScalingMatchesLevelStats() {
        val identity = RivalScaling(100, 100, 100, 100, 100, 100)
        (1..20).forEach { assertEquals(LevelStats.forLevel(it), LevelStats.forRival(it, identity)) }
    }

    @Test
    fun level1() = assertEquals(
        // hp 10000*80%, attack 1000*75%, heal 800*50%, defense 200*50%, regen 5*60% = 3 (3.0); power is not scaled here
        Stats(maxHp = 8_000, maxPower = 300, powerRegen = 3, attack = 750, heal = 400, defense = 100),
        LevelStats.forRival(1, scaling),
    )

    @Test
    fun level10() = assertEquals(
        // hp 28000*80%, attack 2800*75%, heal 2600*50%, defense 1100*50%
        Stats(maxHp = 22_400, maxPower = 300, powerRegen = 3, attack = 2_100, heal = 1_300, defense = 550),
        LevelStats.forRival(10, scaling),
    )

    @Test
    fun valuesNeverDropBelowOne() = assertEquals(
        Stats(maxHp = 1, maxPower = 300, powerRegen = 1, attack = 1, heal = 1, defense = 0),
        LevelStats.forRival(1, RivalScaling(0, 0, 0, 0, 0, 0)),
    )

    @Test
    fun startPowerIsAPercentOfMaxPower() {
        assertEquals(40, scaling.startPower(100))
        assertEquals(120, scaling.startPower(LevelStats.MAX_POWER))
        assertEquals(300, RivalScaling(100, 100, 100, 100, 100, 100).startPower(LevelStats.MAX_POWER))
    }

    @Test
    fun startScalesRivalsOnly() {
        val setup = CombatSetup(
            FighterSpec("Player", 10),
            listOf(RivalSpec("A", 10, PatternBrain(listOf("12"))), RivalSpec("B", 3, PatternBrain(listOf("12")))),
        )
        val state = CombatEngine(rivalScaling = scaling).start(setup, 1L)
        assertEquals(LevelStats.forLevel(10), state.player.stats)
        assertEquals(LevelStats.forLevel(10).maxHp, state.player.hp)
        assertEquals(300, state.player.power)
        assertEquals(listOf(LevelStats.forRival(10, scaling), LevelStats.forRival(3, scaling)), state.rivals.map { it.stats })
        assertEquals(listOf(22_400, LevelStats.forRival(3, scaling).maxHp), state.rivals.map { it.hp })
        assertEquals(listOf(120, 120), state.rivals.map { it.power })
    }

    @Test
    fun defaultEngineUsesDefaultScaling() {
        val state = CombatEngine().start(setup(5, 5), 1L)
        assertEquals(LevelStats.forRival(5), state.rivals.single().stats)
        assertEquals(RivalScaling.DEFAULT.startPower(LevelStats.MAX_POWER), state.rivals.single().power)
        assertEquals(LevelStats.forLevel(5), state.player.stats)
    }

    @Test
    fun startUsesBossScalingForBossRivals() {
        val boss = RivalScaling(hpPct = 150, attackPct = 90, healPct = 100, defensePct = 100, startPowerPct = 60, regenPct = 100)
        val setup = CombatSetup(
            FighterSpec("Player", 10),
            listOf(RivalSpec("Boss", 10, PatternBrain(listOf("12")), isBoss = true), RivalSpec("Escort", 10, PatternBrain(listOf("12")))),
        )
        val state = CombatEngine(rivalScaling = scaling, bossScaling = boss).start(setup, 1L)
        assertEquals(listOf(LevelStats.forRival(10, boss), LevelStats.forRival(10, scaling)), state.rivals.map { it.stats })
        assertEquals(listOf(LevelStats.forRival(10, boss).maxHp, 22_400), state.rivals.map { it.hp })
        assertEquals(listOf(180, 120), state.rivals.map { it.power })
        assertEquals(LevelStats.forLevel(10), state.player.stats)
    }

    @Test
    fun defaultEngineUsesBossScalingForBosses() {
        val setup = CombatSetup(FighterSpec("Player", 5), listOf(RivalSpec("Boss", 5, PatternBrain(listOf("12")), isBoss = true)))
        val state = CombatEngine().start(setup, 1L)
        assertEquals(LevelStats.forRival(5, RivalScaling.BOSS), state.rivals.single().stats)
        assertEquals(RivalScaling.BOSS.startPower(LevelStats.forRival(5, RivalScaling.BOSS).maxPower), state.rivals.single().power)
    }

    /** Decision of the user (2026-10-18): bosses fight alone with double HP and double power (180 % → 360 %, 300 → 600). */
    @Test
    fun bossesHaveDoubleHpAndPower() {
        assertEquals(360, RivalScaling.BOSS.hpPct)
        assertEquals(200, RivalScaling.BOSS.powerPct)
        val setup = CombatSetup(FighterSpec("Player", 10), listOf(RivalSpec("Boss", 10, PatternBrain(listOf("12")), isBoss = true)))
        val boss = CombatEngine().start(setup, 1L).rivals.single()
        assertEquals(28_000 * 360 / 100, boss.stats.maxHp)
        assertEquals(boss.stats.maxHp, boss.hp)
        assertEquals(2 * LevelStats.MAX_POWER, boss.stats.maxPower)
        assertEquals(2 * LevelStats.MAX_POWER, boss.power)
    }

    @Test
    fun rivalSpecIsNotBossByDefault() = assertEquals(false, RivalSpec("A", 1, PatternBrain(listOf("12"))).isBoss)

    @Test
    fun startMarksBossFightersSoThePlayerCanSeeThem() {
        val setup = CombatSetup(
            FighterSpec("Player", 5),
            listOf(RivalSpec("Boss", 5, PatternBrain(listOf("12")), isBoss = true), RivalSpec("Escort", 5, PatternBrain(listOf("12")))),
        )
        val state = CombatEngine().start(setup, 1L)
        assertEquals(listOf(true, false), state.rivals.map { it.isBoss })
        assertEquals(false, state.player.isBoss)
    }

    @Test
    fun powerPctScalesMaxPowerAndStartPower() {
        val big = RivalScaling(hpPct = 100, attackPct = 100, healPct = 100, defensePct = 100, startPowerPct = 50, regenPct = 100, powerPct = 200)
        assertEquals(600, LevelStats.forRival(5, big).maxPower)
        assertEquals(100, RivalScaling.IDENTITY.powerPct)
        val setup = CombatSetup(FighterSpec("Player", 5), listOf(RivalSpec("Boss", 5, PatternBrain(listOf("12")), isBoss = true)))
        val boss = CombatEngine(bossScaling = big).start(setup, 1L).rivals.single()
        assertEquals(600, boss.stats.maxPower)
        assertEquals(300, boss.power)
    }
}
