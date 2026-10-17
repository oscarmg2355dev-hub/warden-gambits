package com.wardengambits.engine.model

/** Effect kinds of spec v2 §5. */
enum class EffectKind { DOT, COT, DAZE_CHANCE, DAZED, FURY, GUARD, VIGOR, SHIELD_MASTER, SHIELD_TACTICS, DRAIN }

/**
 * An effect on a fighter, identified by (fighter, [sourceId], [sourceCombo]). It acts from tick [activeFrom] while
 * [remainingTicks] > 0; [remainingTicks] decreases at the end of every tick in which it is active.
 * A [EffectKind.DRAIN] cast on tick c has `activeFrom = c + 1` and pulses on c+5, c+10, … (see [isDrainPulse]).
 */
data class ActiveEffect(
    val kind: EffectKind,
    val sourceId: FighterId,
    val sourceCombo: String,
    val magnitude: Int,
    val remainingTicks: Int,
    val activeFrom: Int = 0,
    /** Duration the effect was created or last refreshed with (for presentation; never changes while it ticks down). */
    val totalTicks: Int = remainingTicks,
) {
    fun isActive(tick: Int): Boolean = activeFrom <= tick

    fun isDrainPulse(tick: Int, period: Int): Boolean {
        val castTick = activeFrom - 1
        return kind == EffectKind.DRAIN && tick > castTick && (tick - castTick) % period == 0
    }
}
