package com.wardengambits.engine.model

import kotlinx.serialization.Serializable

/**
 * How hard the rivals hit: [rivalAttackPct] is a percent applied to their (already scaled) attack. Ordered from
 * easiest to hardest; [EYE] is the game as balanced (docs/balance/tuning.md).
 */
@Serializable
enum class Difficulty(val rivalAttackPct: Int) {
    SHIRE(50),
    RANGER(60),
    MARK(75),
    EYE(100),
}
