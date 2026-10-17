package com.wardengambits.engine

import com.wardengambits.engine.action.CombatAction
import com.wardengambits.engine.ai.PatternBrain
import com.wardengambits.engine.combo.Combos
import com.wardengambits.engine.model.CombatSetup
import com.wardengambits.engine.model.CombatState
import com.wardengambits.engine.model.FighterSpec
import com.wardengambits.engine.model.PLAYER_ID
import com.wardengambits.engine.model.RivalSpec
import com.wardengambits.engine.rules.CombatEngine
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Locks the HP outcome of seeded fights that mix every over-time effect (DOT, COT, drain, direct hit, guard and
 * shields): changing how ticks are reported must not change any HP. The fingerprints use the default engine, so they
 * are re-recorded (deliberately, from the engine itself) whenever the stats or the balance change. Last recorded:
 * 2026-10-15, «Daño variable 50–150 %»: variable damage and heal per pulse (rolls of 50..150 % on DOT, COT, drain
 * and direct hit) with its re-balance (`RivalScaling.DEFAULT` PV 115 / attack 64, `RivalScaling.BOSS` PV 180 /
 * attack 77); the defeat case raises its rivals to level 10 so that it is still a defeat.
 */
class HpRegressionTest {
    private val playerScript = listOf("12", "21", "3123", "123", "32", "2132", "121", "22", "212", "31232", "13")

    private fun fingerprint(seed: Long, playerLevel: Int, rivalPatterns: List<Pair<Int, List<String>>>): String {
        val engine = CombatEngine()
        val setup = CombatSetup(
            player = FighterSpec("Player", playerLevel),
            rivals = rivalPatterns.mapIndexed { index, (level, pattern) ->
                RivalSpec("Rival ${index + 1}", level, PatternBrain(pattern))
            },
        )
        var state: CombatState = engine.start(setup, seed)
        var checksum = 0L
        var next = 0
        repeat(MAX_TICKS) {
            if (state.outcome != null) return@repeat
            val combo = Combos.find(playerScript[next % playerScript.size])!!
            if (state.player.power >= combo.cost && state.player.queued == null) {
                state = engine.press(state, PLAYER_ID, combo.sequence)
                state = engine.reduce(state, CombatAction.Execute(PLAYER_ID)).state
                next++
            }
            state = engine.reduce(state, CombatAction.Tick).state
            checksum += state.tick.toLong() * state.fighters.withIndex().sumOf { (i, f) -> (i + 1L) * f.hp }
        }
        return "t=${state.tick} o=${state.outcome} hp=${state.fighters.map { it.hp }} c=$checksum"
    }

    private val rivals2 = listOf(3 to listOf("12", "21", "31"), 3 to listOf("3123", "32", "22"))
    private val rivals4 = listOf(
        2 to listOf("21", "13"), 2 to listOf("2132", "12"), 2 to listOf("3123", "121"), 2 to listOf("32", "212"),
    )

    @Test fun twoRivalsSeed1() = assertEquals(EXPECTED[0], fingerprint(1, 3, rivals2))

    @Test fun twoRivalsSeed42() = assertEquals(EXPECTED[1], fingerprint(42, 5, rivals2))

    @Test fun fourRivalsSeed7() = assertEquals(EXPECTED[2], fingerprint(7, 6, rivals4))

    @Test fun fourRivalsSeed99() = assertEquals(EXPECTED[3], fingerprint(99, 2, rivals4))

    @Test fun duelSeed5() = assertEquals(EXPECTED[4], fingerprint(5, 4, listOf(5 to listOf("21212", "123", "3123"))))

    /** Covers a DEFEAT with lethal over-time damage. */
    @Test fun defeatSeed3() = assertEquals(EXPECTED[5], fingerprint(3, 1, rivals4Strong))

    private val rivals4Strong = listOf(
        10 to listOf("12", "121", "31"), 10 to listOf("3123", "32", "22"), 10 to listOf("2132", "12"), 10 to listOf("32", "212"),
    )

    private companion object {
        const val MAX_TICKS = 400
        val EXPECTED = listOf(
            "t=46 o=VICTORY hp=[14000, 0, 0] c=40652946",
            "t=41 o=VICTORY hp=[18000, 0, 0] c=45532177",
            "t=400 o=null hp=[19668, 0, 0, 0, 9985] c=5571523808",
            "t=400 o=null hp=[11769, 0, 0, 0, 12386] c=6433878710",
            "t=400 o=null hp=[15015, 20700] c=4548358568",
            "t=7 o=DEFEAT hp=[0, 29122, 31683, 31828, 32200] c=12550095",
        )
    }
}
