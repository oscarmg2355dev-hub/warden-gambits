package com.wardengambits.engine.rules

import com.wardengambits.engine.combo.ComboTuning
import com.wardengambits.engine.random.GameRandom
import kotlin.math.max

/** Per-pulse variance of damage and heal (spec 2026-10-15 combat cards §1). */
internal object PulseRoll {
    private const val FULL_PCT = 100
    private const val HALF_PCT = 50

    /** Uniform percent in the tuning range; does not consume [random] when the range is a single value. */
    fun roll(random: GameRandom, tuning: ComboTuning): Int =
        if (tuning.rollMinPct == tuning.rollMaxPct) {
            tuning.rollMinPct
        } else {
            tuning.rollMinPct + random.nextInt(tuning.rollMaxPct - tuning.rollMinPct + 1)
        }

    /** [magnitude] × [rollPct] / 100, rounded half up, at least 1. */
    fun scale(magnitude: Int, rollPct: Int): Int = max(1, (magnitude * rollPct + HALF_PCT) / FULL_PCT)
}
