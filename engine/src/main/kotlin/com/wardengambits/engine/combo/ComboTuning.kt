package com.wardengambits.engine.combo

/**
 * Base percents of spec v2 §2: SPDMG = attack × [spearBasePct] / 100, FIDMG = attack × [fistBasePct] / 100,
 * CUR = heal × [cureBasePct] / 100. Every pulse that moves HP (DOT, COT, drain, direct hit) is scaled by a uniform
 * roll in [rollMinPct]..[rollMaxPct] percent (spec 2026-10-15 combat cards §1). While Escudo táctico is active, a
 * fighter's Daze chance is scaled to [tacticsDazePct] percent. 0 (the default, user decision 2026-10-17 after
 * descripcion.md «impide entrar en modo daze») is full immunity: no roll, and Aturdir stores no chance on the fighter.
 */
data class ComboTuning(
    val spearBasePct: Int = 16,
    val fistBasePct: Int = 21,
    val cureBasePct: Int = 35,
    val rollMinPct: Int = 50,
    val rollMaxPct: Int = 150,
    val tacticsDazePct: Int = 0,
) {
    companion object {
        val DEFAULT = ComboTuning()

        /** Default base percents without variance: every pulse rolls 100 % and the random is not consumed. */
        val FIXED = ComboTuning(rollMinPct = 100, rollMaxPct = 100)
    }
}
