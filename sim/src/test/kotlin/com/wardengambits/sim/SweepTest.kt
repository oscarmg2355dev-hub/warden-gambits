package com.wardengambits.sim

import com.wardengambits.engine.ai.BossModes
import com.wardengambits.engine.ai.PatternBrain
import com.wardengambits.engine.combo.ComboTuning
import com.wardengambits.engine.model.CombatSetup
import com.wardengambits.engine.model.FighterSpec
import com.wardengambits.engine.model.LevelStats
import com.wardengambits.engine.model.RivalScaling
import com.wardengambits.engine.model.RivalSpec
import com.wardengambits.engine.rules.CombatEngine
import com.wardengambits.engine.story.NodeId
import com.wardengambits.engine.story.TreeShape
import com.wardengambits.engine.story.content.ModesContent
import com.wardengambits.engine.story.content.ReactionContent
import com.wardengambits.engine.story.content.RivalContent
import com.wardengambits.engine.story.content.StoryContentLoader
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class SweepTest {
    @Test
    fun `grid parses overrides and keeps the current values for the rest`() {
        val grid = SweepGrid.parse(listOf("attack=50,60", "hp=70", "start=40,100"))
        assertEquals(4, grid.size)
        assertEquals(RivalScaling.DEFAULT.copy(hpPct = 70, attackPct = 50, startPowerPct = 40), grid.first().scaling)
        assertTrue(grid.all { it.scaling.hpPct == 70 && it.tuning == ComboTuning.DEFAULT })
        assertEquals(setOf(50, 60), grid.map { it.scaling.attackPct }.toSet())
    }

    @Test
    fun `grid parses max power percent`() {
        val grid = SweepGrid.parse(listOf("attack=80", "hp=150", "start=100", "power=100,200"))
        assertEquals(listOf(100, 200), grid.map { it.scaling.powerPct })
    }

    @Test
    fun `grid parses combo tuning keys`() {
        val grid = SweepGrid.parse(listOf("spear=18,22", "fist=10", "cure=30"))
        assertEquals(listOf(ComboTuning(18, 10, 30), ComboTuning(22, 10, 30)), grid.map { it.tuning })
        assertTrue(grid.all { it.scaling == RivalScaling.DEFAULT })
        val tactics = SweepGrid.parse(listOf("tactics=0,50"))
        assertEquals(listOf(0, 50), tactics.map { it.tuning.tacticsDazePct })
    }

    @Test
    fun `empty grid is the current point of the swept scaling`() {
        assertEquals(listOf(SweepCase(RivalScaling.DEFAULT)), SweepGrid.parse(emptyList()))
        assertEquals(listOf(SweepCase(RivalScaling.BOSS)), SweepGrid.parse(emptyList(), SweepTarget.BOSS.base))
    }

    @Test
    fun `weaker rivals lose more often with the same seeds`() {
        val setup = CombatSetup(FighterSpec("P", 1), listOf(RivalSpec("W", 1, PatternBrain(listOf("12", "121", "31")))))
        val strong = (0 until 50).count {
            FightRunner.fight(setup, Skill.MEDIO, it.toLong(), CombatEngine(RivalScaling(200, 200, 100, 100, 100, 100))).victory
        }
        val weak = (0 until 50).count {
            FightRunner.fight(setup, Skill.MEDIO, it.toLong(), CombatEngine(RivalScaling(50, 50, 100, 100, 50, 100))).victory
        }
        assertTrue(weak > strong, "weak=$weak strong=$strong")
    }

    @Test
    fun `sweep target selects which scaling the grid point replaces`() {
        val point = RivalScaling(150, 80, 100, 100, 60, 100)
        val setup = CombatSetup(
            FighterSpec("P", 10),
            listOf(RivalSpec("Boss", 10, PatternBrain(listOf("12")), isBoss = true), RivalSpec("C", 10, PatternBrain(listOf("12")))),
        )
        assertEquals(ComboTuning(30, 12, 25), SweepTarget.BOSS.engine(point, ComboTuning(30, 12, 25)).tuning)
        val boss = SweepTarget.BOSS.engine(point).start(setup, 1L).rivals.map { it.stats }
        assertEquals(listOf(LevelStats.forRival(10, point), LevelStats.forRival(10, RivalScaling.DEFAULT)), boss)
        val common = SweepTarget.COMMON.engine(point).start(setup, 1L).rivals.map { it.stats }
        assertEquals(listOf(LevelStats.forRival(10, RivalScaling.BOSS), LevelStats.forRival(10, point)), common)
        assertEquals(SweepTarget.BOSS to listOf("hp=1"), SweepTarget.split(listOf("boss", "hp=1")))
        assertEquals(SweepTarget.COMMON to listOf("hp=1"), SweepTarget.split(listOf("hp=1")))
    }

    @Test
    fun `withBoss overrides only the boss of the zone`() {
        val content = StoryContentLoader.loadFromResources()
        val changed = withBoss(content, "moria", offset = 8, pattern = listOf("12", "31"), everyNTicks = 3)
        val boss = changed.node(NodeId("moria", TreeShape.LEVELS, 0))!!.encounter!!.rivals.first()
        // The boss keeps its attack and defense modes (boss-modes spec §1).
        val modes = content.node(NodeId("moria", TreeShape.LEVELS, 0))!!.encounter!!.rivals.first().modes
        assertNotNull(modes)
        assertEquals(RivalContent(boss.name, boss.portrait, 8, listOf("12", "31"), 3, modes = modes), boss)
        assertEquals(content.node(NodeId("moria", 6, 0)), changed.node(NodeId("moria", 6, 0)))
        assertEquals(content.zone("durin"), changed.zone("durin"))
        val original = content.node(NodeId("moria", TreeShape.LEVELS, 0))!!.encounter!!.rivals
        assertEquals(original.drop(1), changed.node(NodeId("moria", TreeShape.LEVELS, 0))!!.encounter!!.rivals.drop(1))
    }

    @Test
    fun `withBoss maps the boss reaction`() {
        val content = StoryContentLoader.loadFromResources()
        val node = NodeId("chet", TreeShape.LEVELS, 0)
        // Bosses carry modes instead of a reaction (boss-modes spec §1): the mapper gets null and its result is kept.
        val reaction = ReactionContent(atHpPct = listOf(45), combos = listOf("21"))
        val changed = withBoss(content, "chet", reaction = { it ?: reaction })
        val before = content.node(node)!!.encounter!!.rivals.first()
        assertEquals(before.copy(reaction = reaction), changed.node(node)!!.encounter!!.rivals.first())
    }

    @Test
    fun `withBoss maps the boss modes`() {
        val content = StoryContentLoader.loadFromResources()
        val node = NodeId("chet", TreeShape.LEVELS, 0)
        val before = content.node(node)!!.encounter!!.rivals.first()
        val changed = withBoss(content, "chet", modes = { it?.copy(maxDefenses = 1) })
        assertEquals(before.copy(modes = before.modes!!.copy(maxDefenses = 1)), changed.node(node)!!.encounter!!.rivals.first())
    }

    @Test
    fun `modes options replace thresholds, defenses and the damage tail of the defense`() {
        val modes = ModesContent(50, 70, 3, listOf("2132", "21", "212", "2121", "21212", "12", "121"))
        assertEquals(modes, modesOverride(emptyMap())(modes))
        val all = mapOf("below" to "40", "above" to "60", "max" to "2", "tail" to "1212;12121")
        assertEquals(ModesContent(40, 60, 2, listOf("2132", "21", "212", "2121", "21212", "1212", "12121")), modesOverride(all)(modes))
        assertEquals(null, modesOverride(all)(null))
    }

    @Test
    fun `withBossModes maps only the bosses of the given zones`() {
        val content = StoryContentLoader.loadFromResources()
        val changed = withBossModes(content, listOf("chet", "moria"), modesOverride(mapOf("max" to "1")))
        listOf("chet", "moria").forEach { zone ->
            assertEquals(1, changed.node(NodeId(zone, TreeShape.LEVELS, 0))!!.encounter!!.rivals.first().modes!!.maxDefenses)
        }
        assertEquals(content.zone("bree"), changed.zone("bree"))
        assertEquals(content.node(NodeId("chet", 6, 0)), changed.node(NodeId("chet", 6, 0)))
    }

    @Test
    fun `fights report how many times the boss entered its defense`() {
        val modes = BossModes(50, 70, 3, listOf("2132", "21", "212", "2121", "21212", "12", "121"))
        val boss = RivalSpec("Boss", 5, PatternBrain(listOf("12", "121"), 1, modes = modes), isBoss = true)
        val result = FightRunner.fight(CombatSetup(FighterSpec("P", 5), listOf(boss)), Skill.MAESTRO, 1L)
        assertTrue(result.bossDefenses in 1..3, result.toString())
        val plain = FightRunner.fight(CombatSetup(FighterSpec("P", 5), listOf(RivalSpec("R", 5, PatternBrain(listOf("12"))))), Skill.MAESTRO, 1L)
        assertEquals(0, plain.bossDefenses)
    }

    @Test
    fun `withoutReactions removes every rival reaction and nothing else`() {
        val content = StoryContentLoader.loadFromResources()
        val stripped = withoutReactions(content)
        val rivals = content.zones.flatMap { z -> z.nodes.flatMap { it.encounter?.rivals.orEmpty() } }
        val strippedRivals = stripped.zones.flatMap { z -> z.nodes.flatMap { it.encounter?.rivals.orEmpty() } }
        assertTrue(rivals.any { it.reaction != null })
        assertEquals(rivals.map { it.copy(reaction = null) }, strippedRivals)
    }
}
