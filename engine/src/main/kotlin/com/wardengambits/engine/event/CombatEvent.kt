package com.wardengambits.engine.event

import com.wardengambits.engine.combo.ComboDefinition
import com.wardengambits.engine.model.EffectKind
import com.wardengambits.engine.model.FighterId
import com.wardengambits.engine.model.Outcome

enum class RejectReason {
    BUFFER_FULL,
    UNKNOWN_SEQUENCE,
    DAZED,
    NOT_ENOUGH_POWER,
    FIGHTER_DEFEATED,
    COMBAT_OVER,
    INVALID_TARGET,
}

sealed interface CombatEvent {
    data class ComboQueued(val fighter: FighterId, val combo: ComboDefinition) : CombatEvent
    data class ComboRejected(val fighter: FighterId, val reason: RejectReason) : CombatEvent
    data class ComboResolved(val fighter: FighterId, val combo: ComboDefinition, val chained: Boolean) : CombatEvent
    data class EffectApplied(val target: FighterId, val kind: EffectKind, val ticks: Int) : CombatEvent
    /**
     * One HP change caused by one effect (combat text strip spec §1): negative [amount] is damage, positive is
     * healing (it can be +0 for a capped heal). [kind] is null only for a direct hit (`123`).
     */
    data class EffectResolved(
        val target: FighterId,
        val source: FighterId,
        val sourceCombo: String,
        val kind: EffectKind?,
        val amount: Int,
        /** Pulse roll (percent) that scaled [amount]; 100 for effects that do not roll. */
        val rollPct: Int = 100,
    ) : CombatEvent
    /** A rival that could not pay its next pattern combo drank a potion: [amount] power gained (clamped to its max). */
    data class PotionUsed(val fighter: FighterId, val amount: Int) : CombatEvent
    data class EffectExpired(val target: FighterId, val kind: EffectKind) : CombatEvent
    data class FighterDefeated(val id: FighterId) : CombatEvent
    data class CombatEnded(val outcome: Outcome) : CombatEvent
}
