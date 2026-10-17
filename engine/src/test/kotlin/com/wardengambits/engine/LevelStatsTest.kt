package com.wardengambits.engine

import com.wardengambits.engine.model.LevelStats
import com.wardengambits.engine.model.Stats
import kotlin.test.Test
import kotlin.test.assertEquals

class LevelStatsTest {
    @Test fun statScaleIs100() = assertEquals(100, LevelStats.STAT_SCALE)
    @Test fun maxPowerIs300() = assertEquals(300, LevelStats.MAX_POWER)

    @Test fun level1() = assertEquals(
        Stats(maxHp = 10_000, maxPower = 300, powerRegen = 5, attack = 1_000, heal = 800, defense = 200),
        LevelStats.forLevel(1),
    )
    @Test fun level5() = assertEquals(Stats(18_000, 300, 5, 1_800, 1_600, 600), LevelStats.forLevel(5))
    @Test fun level10() = assertEquals(Stats(28_000, 300, 5, 2_800, 2_600, 1_100), LevelStats.forLevel(10))
    @Test fun levelBelowOneIsClampedToOne() = assertEquals(LevelStats.forLevel(1), LevelStats.forLevel(0))
}
