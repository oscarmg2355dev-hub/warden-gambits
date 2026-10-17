package com.wardengambits.app.ui.help

import android.content.res.Resources
import androidx.annotation.StringRes
import com.wardengambits.app.R
import com.wardengambits.engine.combo.ComboDefinition
import com.wardengambits.engine.combo.ComboEffect
import com.wardengambits.engine.combo.ComboFamily
import com.wardengambits.engine.combo.ComboTuning
import com.wardengambits.engine.combo.Combos
import com.wardengambits.engine.model.EffectKind
import com.wardengambits.engine.rules.DamageFormula

/**
 * Combo names and help texts, written from the engine's data (family, tier, magnitudes, ticks) with the
 * translatable templates of the string resources, so they follow the app's language.
 */
object ComboDescriber {
    private const val FULL_PCT = 100

    private val NAMES: Map<String, Int> = mapOf(
        "12" to R.string.combo_name_12,
        "121" to R.string.combo_name_121,
        "1212" to R.string.combo_name_1212,
        "12121" to R.string.combo_name_12121,
        "13" to R.string.combo_name_13,
        "131" to R.string.combo_name_131,
        "1313" to R.string.combo_name_1313,
        "13131" to R.string.combo_name_13131,
        "31" to R.string.combo_name_31,
        "313" to R.string.combo_name_313,
        "3131" to R.string.combo_name_3131,
        "31313" to R.string.combo_name_31313,
        "32" to R.string.combo_name_32,
        "323" to R.string.combo_name_323,
        "3232" to R.string.combo_name_3232,
        "32323" to R.string.combo_name_32323,
        "21" to R.string.combo_name_21,
        "212" to R.string.combo_name_212,
        "2121" to R.string.combo_name_2121,
        "21212" to R.string.combo_name_21212,
        "3123" to R.string.combo_name_3123,
        "31232" to R.string.combo_name_31232,
        "11" to R.string.combo_name_11,
        "22" to R.string.combo_name_22,
        "33" to R.string.combo_name_33,
        "2132" to R.string.combo_name_2132,
        "2131" to R.string.combo_name_2131,
        "123" to R.string.combo_name_123,
        "321" to R.string.combo_name_321,
    )

    /** Name of the combo cast with [sequence] (every catalog combo has one). */
    @StringRes
    fun nameRes(sequence: String): Int = NAMES.getValue(sequence)

    @StringRes
    fun titleRes(family: ComboFamily): Int = when (family) {
        ComboFamily.SPEAR_A -> R.string.combo_family_spear_a
        ComboFamily.SPEAR_B -> R.string.combo_family_spear_b
        ComboFamily.DAZE -> R.string.combo_family_daze
        ComboFamily.AREA -> R.string.combo_family_area
        ComboFamily.HEAL -> R.string.combo_family_heal
        ComboFamily.DRAIN -> R.string.combo_family_drain
        ComboFamily.MULTIPLIER -> R.string.combo_family_multiplier
        ComboFamily.SHIELD -> R.string.combo_family_shield
        ComboFamily.DIRECT -> R.string.combo_family_direct
        ComboFamily.RECOVER -> R.string.combo_family_recover
    }

    fun name(res: Resources, def: ComboDefinition): String = res.getString(nameRes(def.sequence))

    fun title(res: Resources, family: ComboFamily): String = res.getString(titleRes(family))

    fun describe(res: Resources, def: ComboDefinition): String =
        res.getString(R.string.combo_describe, name(res, def), effectText(res, def))

    /** Combos grouped by family in catalog order (spec v2 §3). */
    fun grouped(): Map<ComboFamily, List<ComboDefinition>> {
        val byFamily = Combos.all.groupBy { it.family }
        return ComboFamily.entries.associateWith { byFamily.getValue(it) }
    }

    /** Effect description without the combo name. */
    fun effectText(res: Resources, def: ComboDefinition): String {
        val ticks = def.baseTicks
        return when (val effect = def.effect) {
            is ComboEffect.SpearDot -> withChainTicks(res, def, res.getString(R.string.combo_effect_spear, effect.multiplier, ticks))
            is ComboEffect.AreaDot -> withChainTicks(res, def, res.getString(R.string.combo_effect_area, effect.multiplier, ticks))
            is ComboEffect.HealOverTime -> withChainTicks(res, def, res.getString(R.string.combo_effect_heal, effect.multiplier, ticks))
            is ComboEffect.DazeChance -> {
                val base = res.getString(R.string.combo_effect_daze, effect.basePct, ticks)
                if (effect.chainBonusPct > 0) res.getString(R.string.combo_effect_daze_chain, base, effect.chainBonusPct) else base
            }
            is ComboEffect.Drain -> {
                val base = res.getString(R.string.combo_effect_drain, Combos.DRAIN_PERIOD, ticks)
                if (effect.chainMultiplier > 1) res.getString(R.string.combo_effect_drain_chain, base, effect.chainMultiplier) else base
            }
            is ComboEffect.Buff -> buffText(res, effect.kind, effect.ticks)
            ComboEffect.DirectHit -> res.getString(R.string.combo_effect_direct, DamageFormula.DIRECT_HIT_MULTIPLIER)
            is ComboEffect.PowerRestore -> res.getString(R.string.combo_effect_recover, effect.pct)
        }
    }

    private fun withChainTicks(res: Resources, def: ComboDefinition, base: String): String =
        if (def.chainBonusTicks > 0) res.getString(R.string.combo_effect_chain_ticks, base, def.chainBonusTicks) else base

    private fun buffText(res: Resources, kind: EffectKind, ticks: Int): String = when (kind) {
        EffectKind.FURY -> res.getString(R.string.combo_effect_fury, DamageFormula.FURY_PCT - FULL_PCT, ticks)
        EffectKind.GUARD -> res.getString(R.string.combo_effect_damage_taken, FULL_PCT - DamageFormula.GUARD_PCT, ticks)
        EffectKind.VIGOR -> res.getString(R.string.combo_effect_vigor, DamageFormula.VIGOR_REGEN_PCT - FULL_PCT, ticks)
        EffectKind.SHIELD_MASTER ->
            res.getString(R.string.combo_effect_damage_taken, FULL_PCT - DamageFormula.MASTER_SHIELD_PCT, ticks)
        EffectKind.SHIELD_TACTICS -> when (val pct = ComboTuning.DEFAULT.tacticsDazePct) {
            0 -> res.getString(R.string.combo_effect_daze_immune, ticks)
            else -> res.getString(R.string.combo_effect_daze_reduced, FULL_PCT - pct, ticks)
        }
        EffectKind.DOT, EffectKind.COT, EffectKind.DAZE_CHANCE, EffectKind.DAZED, EffectKind.DRAIN -> kind.name
    }
}
