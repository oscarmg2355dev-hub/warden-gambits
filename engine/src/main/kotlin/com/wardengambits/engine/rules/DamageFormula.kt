package com.wardengambits.engine.rules

import com.wardengambits.engine.combo.ComboTuning
import kotlin.math.max

/** Integer formulas of spec v2 §2 and §7. */
object DamageFormula {
    private const val FULL_PCT = 100

    /** Spear damage with Furia, percent of normal. */
    const val FURY_PCT = 120

    /** Damage taken with Guardia, percent of normal. */
    const val GUARD_PCT = 80

    /** Damage taken with Escudo maestro, percent of normal. */
    const val MASTER_SHIELD_PCT = 50

    /** Power regeneration with Brío, percent of normal. */
    const val VIGOR_REGEN_PCT = 120

    /** Golpe directo hits for this many SPDMG at once. */
    const val DIRECT_HIT_MULTIPLIER = 10

    /** SPDMG. */
    fun spearUnit(attack: Int, tuning: ComboTuning): Int = attack * tuning.spearBasePct / FULL_PCT

    /** FIDMG. */
    fun fistUnit(attack: Int, tuning: ComboTuning): Int = attack * tuning.fistBasePct / FULL_PCT

    /** CUR. */
    fun cureUnit(heal: Int, tuning: ComboTuning): Int = heal * tuning.cureBasePct / FULL_PCT

    fun spearDot(tier: Int, spearUnit: Int, fury: Boolean): Int =
        tier * spearUnit * (if (fury) FURY_PCT else FULL_PCT) / FULL_PCT

    fun areaDot(tier: Int, fistUnit: Int): Int = tier * fistUnit

    /** Damage actually taken from [amount] after Guardia and Escudo maestro (multiplied); at least 1. */
    fun received(amount: Int, guard: Boolean, masterShield: Boolean): Int =
        max(1, amount * (if (guard) GUARD_PCT else FULL_PCT) / FULL_PCT * (if (masterShield) MASTER_SHIELD_PCT else FULL_PCT) / FULL_PCT)

    fun directHit(spearUnit: Int, fury: Boolean, defense: Int, guard: Boolean, masterShield: Boolean): Int =
        received(max(1, spearDot(DIRECT_HIT_MULTIPLIER, spearUnit, fury) - defense), guard, masterShield)
}
