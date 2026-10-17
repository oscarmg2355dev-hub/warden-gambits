package com.wardengambits.sim

import com.wardengambits.engine.action.CombatAction
import com.wardengambits.engine.combo.Input
import com.wardengambits.engine.model.CombatSetup
import com.wardengambits.engine.model.Outcome
import com.wardengambits.engine.model.PLAYER_ID
import com.wardengambits.engine.rules.CombatEngine

/**
 * Result of one simulated fight; [capped] fights hit [FightRunner.MAX_TICKS] and count as defeats. [bossDefenses] is
 * how many times the bosses entered their defense mode (boss-modes spec §2).
 */
data class FightResult(
    val victory: Boolean,
    val ticks: Int,
    val hpLeft: Int,
    val maxHp: Int,
    val capped: Boolean,
    val bossDefenses: Int = 0,
)

/** Plays one fight with the real [CombatEngine] and a [HumanPlayer]. */
object FightRunner {
    const val MAX_TICKS = 600

    private val defaultEngine = CombatEngine()
    private val inputs = Input.entries.associateBy { it.code }

    fun fight(setup: CombatSetup, skill: Skill, seed: Long, engine: CombatEngine = defaultEngine): FightResult {
        var state = engine.start(setup, seed)
        val player = HumanPlayer(skill, engine.tuning)
        while (state.outcome == null && state.tick < MAX_TICKS) {
            if (player.actsOnTick(state.tick + 1)) {
                val decision = player.decide(state)
                val target = decision.target
                if (target != null && target != state.targetId) {
                    state = engine.reduce(state, CombatAction.SelectTarget(target)).state
                }
                val combo = decision.combo
                if (combo != null) {
                    combo.sequence.forEach { state = engine.reduce(state, CombatAction.Press(PLAYER_ID, inputs.getValue(it))).state }
                    state = engine.reduce(state, CombatAction.Execute(PLAYER_ID)).state
                }
            }
            state = engine.reduce(state, CombatAction.Tick).state
        }
        val p = state.player
        val defenses = state.rivals.filter { it.isBoss }.sumOf { it.defenses }
        return FightResult(state.outcome == Outcome.VICTORY, state.tick, p.hp, p.stats.maxHp, state.outcome == null, defenses)
    }
}
