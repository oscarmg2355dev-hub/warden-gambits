package com.wardengambits.engine.model

data class Stats(
    val maxHp: Int,
    val maxPower: Int,
    val powerRegen: Int,
    val attack: Int,
    val heal: Int,
    val defense: Int,
)

/**
 * Rival stat curve as integer percents of [LevelStats.forLevel] (the player curve).
 * [powerPct] scales max power; rivals start the fight with [startPower] (a percent of that max) instead of full power.
 */
data class RivalScaling(
    val hpPct: Int,
    val attackPct: Int,
    val healPct: Int,
    val defensePct: Int,
    val startPowerPct: Int,
    val regenPct: Int,
    val powerPct: Int = 100,
) {
    fun startPower(maxPower: Int): Int = maxPower * startPowerPct / 100

    companion object {
        val IDENTITY = RivalScaling(100, 100, 100, 100, 100, 100)

        /** Tuned with `./gradlew :sim:run --args="sweep"` (see docs/balance/tuning.md). */
        val DEFAULT = RivalScaling(hpPct = 115, attackPct = 64, healPct = 100, defensePct = 100, startPowerPct = 92, regenPct = 100)

        /** Scaling of zone bosses (first rival of a level-7 encounter). Tuned with `sweep boss ...`. */
        val BOSS = RivalScaling(hpPct = 180, attackPct = 77, healPct = 100, defensePct = 100, startPowerPct = 100, regenPct = 100)
    }
}

object LevelStats {
    /** Multiplier of HP, attack, heal and defense (so every damage and heal number scales with it). */
    const val STAT_SCALE = 100

    /** Max power of every fighter (rivals: times [RivalScaling.powerPct]). Regen and combo costs are not scaled. */
    const val MAX_POWER = 300

    fun forLevel(level: Int): Stats {
        val l = maxOf(1, level)
        return Stats(
            maxHp = (100 + 20 * (l - 1)) * STAT_SCALE,
            maxPower = MAX_POWER,
            powerRegen = 5,
            attack = (10 + 2 * (l - 1)) * STAT_SCALE,
            heal = (8 + 2 * (l - 1)) * STAT_SCALE,
            defense = (2 + (l - 1)) * STAT_SCALE,
        )
    }

    fun forRival(level: Int, scaling: RivalScaling = RivalScaling.DEFAULT): Stats {
        val base = forLevel(level)
        return Stats(
            maxHp = maxOf(1, base.maxHp * scaling.hpPct / 100),
            maxPower = maxOf(1, base.maxPower * scaling.powerPct / 100),
            powerRegen = maxOf(1, base.powerRegen * scaling.regenPct / 100),
            attack = maxOf(1, base.attack * scaling.attackPct / 100),
            heal = maxOf(1, base.heal * scaling.healPct / 100),
            defense = maxOf(0, base.defense * scaling.defensePct / 100),
        )
    }
}
