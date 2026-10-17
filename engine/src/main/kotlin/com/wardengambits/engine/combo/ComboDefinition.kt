package com.wardengambits.engine.combo

import com.wardengambits.engine.model.EffectKind

enum class TargetKind { SELECTED, ALL_OPPONENTS, SELF }

/** Combo families of spec v2 §3. Tiered families chain 1→4 (DRAIN 1→2); the rest have tier 0. */
enum class ComboFamily { SPEAR_A, SPEAR_B, DAZE, AREA, HEAL, DRAIN, MULTIPLIER, SHIELD, DIRECT, RECOVER }

/** What a resolved combo does (spec v2 §3, formulas in §7). */
sealed interface ComboEffect {
    /** DoT on the target: `multiplier × SPDMG` per tick (+Furia). */
    data class SpearDot(val multiplier: Int) : ComboEffect

    /** DoT on every alive opponent: `multiplier × FIDMG` per tick. */
    data class AreaDot(val multiplier: Int) : ComboEffect

    /** Daze chance on the target: [basePct], plus [chainBonusPct] when chained. */
    data class DazeChance(val basePct: Int, val chainBonusPct: Int) : ComboEffect

    /** CoT on the caster: `multiplier × CUR` per tick. */
    data class HealOverTime(val multiplier: Int) : ComboEffect

    /** Life drain on the caster: CUR per pulse, × [chainMultiplier] when chained. */
    data class Drain(val chainMultiplier: Int) : ComboEffect

    /** Multiplier or shield on the caster. */
    data class Buff(val kind: EffectKind, val ticks: Int) : ComboEffect

    /** Immediate hit: `max(1, 10 × SPDMG − Defensa)` with the target's reductions. */
    data object DirectHit : ComboEffect

    /** Instant power refill on the caster: [pct] % of its max power, clamped to the max. No lasting effect. */
    data class PowerRestore(val pct: Int = 10) : ComboEffect
}

/**
 * [baseTicks] is the duration of the effect created; [chainBonusTicks] is added when the combo resolves chained.
 * [tier] is 1–4 for tiered families and 0 for combos without tiers. Pure data: names and help texts are built
 * by the app from [sequence], [family] and the effect.
 */
data class ComboDefinition(
    val sequence: String,
    val family: ComboFamily,
    val tier: Int,
    val cost: Int,
    val target: TargetKind,
    val effect: ComboEffect,
    val baseTicks: Int,
    val chainBonusTicks: Int = 0,
)
