package com.wardengambits.engine

import com.wardengambits.engine.model.ActiveEffect
import com.wardengambits.engine.model.EffectKind
import com.wardengambits.engine.model.Fighter
import com.wardengambits.engine.model.LevelStats
import com.wardengambits.engine.model.PLAYER_ID
import com.wardengambits.engine.model.Side
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class FighterDazedTest {
    private val stats = LevelStats.forLevel(1)
    private val fighter = Fighter(PLAYER_ID, "Warden", 1, Side.PLAYER, stats, hp = stats.maxHp, power = stats.maxPower)

    @Test fun notDazedWithoutTheEffect() {
        assertFalse(fighter.isDazed())
        val chance = ActiveEffect(EffectKind.DAZE_CHANCE, PLAYER_ID, "31", 30, 10, activeFrom = 1)
        assertFalse(fighter.copy(effects = listOf(chance)).isDazed())
    }

    @Test fun dazedAsSoonAsTheEffectIsPresentEvenIfPending() {
        val dazed = ActiveEffect(EffectKind.DAZED, PLAYER_ID, "31", 0, 10, activeFrom = 5)
        assertTrue(fighter.copy(effects = listOf(dazed)).isDazed())
    }
}
