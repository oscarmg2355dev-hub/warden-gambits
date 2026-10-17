package com.wardengambits.engine.ai

import com.wardengambits.engine.combo.ComboFamily
import com.wardengambits.engine.combo.Combos

/**
 * A boss's attack and defense modes (boss-modes spec §2): it defends once its HP is at or below [defenseBelowPct]
 * (percent of max HP), at most [maxDefenses] times per combat, casting [defense] in a cycle every tick, and returns
 * to attack once its HP is at or above [attackAbovePct].
 */
data class BossModes(
    val defenseBelowPct: Int,
    val attackAbovePct: Int,
    val maxDefenses: Int,
    val defense: List<String>,
) {
    init {
        require(defenseBelowPct > 0 && defenseBelowPct < attackAbovePct && attackAbovePct <= PERCENT) {
            "Need 0 < defenseBelowPct < attackAbovePct <= $PERCENT, got $defenseBelowPct / $attackAbovePct"
        }
        require(maxDefenses >= 1) { "maxDefenses must be >= 1, got $maxDefenses" }
        require(defense.isNotEmpty() && defense.all { Combos.find(it) != null }) { "Invalid defense $defense" }
    }

    /**
     * Index in [defense] of the first of its final damage combos (after the last shield or heal), where a Dazed boss
     * jumps; equal to the size when the defense does not end with damage.
     */
    val dazedRestart: Int = defense.indexOfLast { Combos.find(it)?.family in PROTECTIVE } + 1

    private companion object {
        const val PERCENT = 100
        val PROTECTIVE = setOf(ComboFamily.SHIELD, ComboFamily.HEAL)
    }
}
