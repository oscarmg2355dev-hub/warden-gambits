package com.wardengambits.engine

import com.wardengambits.engine.action.CombatAction
import com.wardengambits.engine.ai.RivalBrain
import com.wardengambits.engine.combo.ComboDefinition
import com.wardengambits.engine.combo.ComboTuning
import com.wardengambits.engine.combo.Input
import com.wardengambits.engine.model.CombatSetup
import com.wardengambits.engine.model.CombatState
import com.wardengambits.engine.model.Fighter
import com.wardengambits.engine.model.FighterId
import com.wardengambits.engine.model.FighterSpec
import com.wardengambits.engine.model.RivalSpec
import com.wardengambits.engine.random.GameRandom
import com.wardengambits.engine.rules.CombatEngine

/**
 * Fixed combo tuning for the mechanics tests (SPDMG = attack × 20 %, FIDMG = attack × 18 %, CUR = heal × 25 %), so
 * re-balancing [ComboTuning.DEFAULT] does not change what they check, and without pulse variance (every roll is 100 %,
 * the random is not consumed). [DamageFormulaTest] pins the default.
 */
val TEST_TUNING = ComboTuning(spearBasePct = 20, fistBasePct = 18, cureBasePct = 25, rollMinPct = 100, rollMaxPct = 100)

class FixedRandom(private vararg val rolls: Int) : GameRandom {
    private var index = 0

    override val state: Long = 0L

    override fun nextInt(bound: Int): Int {
        val roll = rolls[index % rolls.size]
        index++
        return roll
    }
}

class NoopBrain : RivalBrain {
    override fun decide(state: CombatState, self: FighterId, random: GameRandom): ComboDefinition? = null
}

fun setup(playerLevel: Int = 1, vararg rivalLevels: Int = intArrayOf(1)): CombatSetup = CombatSetup(
    player = FighterSpec("Player", playerLevel),
    rivals = rivalLevels.mapIndexed { index, level -> RivalSpec("Rival ${index + 1}", level, NoopBrain()) },
)

fun CombatEngine.press(state: CombatState, id: FighterId, seq: String): CombatState =
    seq.fold(state) { current, char ->
        val input = Input.entries.first { it.code == char }
        reduce(current, CombatAction.Press(id, input)).state
    }

fun CombatState.withFighter(id: FighterId, change: Fighter.() -> Fighter): CombatState =
    copy(fighters = fighters.map { if (it.id == id) it.change() else it })
