package com.wardengambits.engine

import com.wardengambits.engine.action.CombatAction
import com.wardengambits.engine.ai.PatternBrain
import com.wardengambits.engine.model.CombatSetup
import com.wardengambits.engine.model.Difficulty
import com.wardengambits.engine.model.FighterId
import com.wardengambits.engine.model.FighterSpec
import com.wardengambits.engine.model.Outcome
import com.wardengambits.engine.model.PLAYER_ID
import com.wardengambits.engine.model.RivalScaling
import com.wardengambits.engine.model.RivalSpec
import com.wardengambits.engine.rules.CombatEngine
import com.wardengambits.engine.story.NodeId
import com.wardengambits.engine.story.Phase
import com.wardengambits.engine.story.RunState
import com.wardengambits.engine.story.SaveCodec
import com.wardengambits.engine.story.StoryAction
import com.wardengambits.engine.story.StoryEngine
import com.wardengambits.engine.story.StoryProfile
import com.wardengambits.engine.story.fakeContent
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class DifficultyTest {
    private val engine = CombatEngine(tuning = TEST_TUNING)

    private fun setupWith(pct: Int?) = if (pct == null) {
        setup(3, 4, 5)
    } else {
        setup(3, 4, 5).copy(rivalAttackPct = pct)
    }

    @Test
    fun orderAndValues() {
        assertEquals(listOf(50, 60, 75, 100), Difficulty.entries.map { it.rivalAttackPct })
        assertEquals(listOf("SHIRE", "RANGER", "MARK", "EYE"), Difficulty.entries.map { it.name })
    }

    @Test
    fun attackScaledByRivalAttackPct() {
        val full = engine.start(setupWith(100), 1L)
        val half = engine.start(setupWith(50), 1L)
        full.rivals.zip(half.rivals).forEach { (a, b) ->
            assertEquals(maxOf(1, a.stats.attack * 50 / 100), b.stats.attack)
        }
        val bossSetup = CombatSetup(
            FighterSpec("Player", 3),
            listOf(RivalSpec("Boss", 7, NoopBrain(), isBoss = true)),
        )
        val boss100 = engine.start(bossSetup, 1L).rivals.single()
        val boss50 = engine.start(bossSetup.copy(rivalAttackPct = 50), 1L).rivals.single()
        assertEquals(maxOf(1, boss100.stats.attack * 50 / 100), boss50.stats.attack)
    }

    @Test
    fun playerAttackUnchanged() {
        assertEquals(
            engine.start(setupWith(100), 1L).player.stats,
            engine.start(setupWith(50), 1L).player.stats,
        )
    }

    @Test
    fun attackNeverBelowOne() {
        val tiny = CombatEngine(rivalScaling = RivalScaling.IDENTITY.copy(attackPct = 0))
        assertEquals(1, tiny.start(setup(1, 1), 1L).rivals.single().stats.attack)
        val half = tiny.start(setup(1, 1).copy(rivalAttackPct = 50), 1L)
        assertEquals(1, half.rivals.single().stats.attack)
    }

    private fun hpLostAfterRivalCast(pct: Int): Int {
        val rival = FighterId(1)
        var state = engine.start(setup(3, 3).copy(rivalAttackPct = pct), 1L)
        state = engine.press(state, rival, "12")
        state = engine.reduce(state, CombatAction.Execute(rival)).state
        state = engine.reduce(state, CombatAction.Tick).state
        return state.player.stats.maxHp - state.player.hp
    }

    @Test
    fun rivalDamageScalesWithAttack() {
        val full = hpLostAfterRivalCast(100)
        val half = hpLostAfterRivalCast(50)
        assertTrue(full > 0, "the rival combo must hurt")
        assertTrue(abs(full / 2 - half) <= 1, "full=$full half=$half")
    }

    @Test
    fun defaultIsUnchanged() {
        fun run(setup: CombatSetup): Any {
            var state = CombatEngine().start(setup, 77L)
            repeat(30) { state = CombatEngine().reduce(state, CombatAction.Tick).state }
            return state
        }
        val brainSetup = { extra: Boolean ->
            val rivals = listOf(RivalSpec("Orc", 3, PatternBrain(listOf("12", "121", "13"))))
            if (extra) CombatSetup(FighterSpec("P", 3), rivals, rivalAttackPct = 100) else CombatSetup(FighterSpec("P", 3), rivals)
        }
        assertEquals(run(brainSetup(false)), run(brainSetup(true)))
    }

    private val story = StoryEngine(fakeContent())

    private fun pending() = StoryProfile(
        RunState(1, 1, 1, 0, NodeId.START, emptyList(), Phase.COMBAT_PENDING),
        emptyList(),
    )

    private fun finish(profile: StoryProfile, difficulty: Difficulty?): StoryProfile {
        val ready = profile.copy(run = profile.run.copy(phase = Phase.COMBAT_PENDING))
        return story.reduce(ready, StoryAction.CombatFinished(Outcome.VICTORY, 1L, difficulty)).profile
    }

    @Test
    fun easiestDifficultyCountsDefeatsToo() {
        var p = pending()
        val ready = p.copy(run = p.run.copy(phase = Phase.COMBAT_PENDING))
        p = story.reduce(ready, StoryAction.CombatFinished(Outcome.DEFEAT, 1L, Difficulty.SHIRE)).profile
        assertEquals(Difficulty.SHIRE, p.run.easiestDifficulty)
        p = finish(p, Difficulty.EYE)
        assertEquals(Difficulty.SHIRE, p.run.easiestDifficulty)
    }

    @Test
    fun easiestDifficultyKeepsTheEasiest() {
        var p = pending()
        assertNull(p.run.easiestDifficulty)
        p = finish(p, Difficulty.MARK)
        assertEquals(Difficulty.MARK, p.run.easiestDifficulty)
        p = finish(p, Difficulty.SHIRE)
        assertEquals(Difficulty.SHIRE, p.run.easiestDifficulty)
        p = finish(p, Difficulty.EYE)
        assertEquals(Difficulty.SHIRE, p.run.easiestDifficulty)
        p = finish(p, null)
        assertEquals(Difficulty.SHIRE, p.run.easiestDifficulty)
    }

    @Test
    fun lapRecordCarriesDifficulty() {
        val done = pending().run.copy(phase = Phase.LAP_COMPLETE, node = NodeId.FINAL, easiestDifficulty = Difficulty.RANGER)
        val step = story.reduce(StoryProfile(done, emptyList()), StoryAction.StartNextLap(9L))
        assertEquals(Difficulty.RANGER, step.profile.history.single().difficulty)
        assertNull(step.profile.run.easiestDifficulty)
    }

    @Test
    fun oldSaveLoadsWithoutDifficulty() {
        val encoded = SaveCodec.encode(
            pending().copy(history = listOf(com.wardengambits.engine.story.RunRecord(1, NodeId.FINAL, 15, 14, com.wardengambits.engine.story.RunResult.COMPLETED, 5L))),
        )
        val old = encoded.replace(Regex(",\"easiestDifficulty\":null"), "").replace(Regex(",\"difficulty\":null"), "")
        assertTrue(!old.contains("ifficulty"), old)
        val decoded = assertNotNull(SaveCodec.decode(old))
        assertNull(decoded.profile.run.easiestDifficulty)
        assertNull(decoded.profile.history.single().difficulty)
    }
}
