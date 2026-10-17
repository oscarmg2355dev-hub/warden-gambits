package com.wardengambits.engine

import com.wardengambits.engine.combo.Combos
import com.wardengambits.engine.model.ActiveEffect
import com.wardengambits.engine.model.EffectKind
import com.wardengambits.engine.model.Fighter
import com.wardengambits.engine.model.PLAYER_ID
import com.wardengambits.engine.model.Side
import com.wardengambits.engine.model.Stats
import kotlin.test.Test
import kotlin.test.assertEquals

class DazeChanceTest {
    private fun fighter(vararg effects: ActiveEffect) =
        Fighter(PLAYER_ID, "x", 1, Side.RIVAL, Stats(100, 100, 1, 1, 1, 1), 100, 0, effects = effects.toList())

    private fun at(magnitude: Int, from: Int = 0, kind: EffectKind = EffectKind.DAZE_CHANCE) =
        ActiveEffect(kind, PLAYER_ID, "31", magnitude, 5, from)

    @Test fun sumsActiveDazeChanceOnly() {
        val f = fighter(at(5), at(10), at(7, from = 9), at(20, kind = EffectKind.DOT))
        assertEquals(15, f.dazeChancePct(tick = 3))
    }

    @Test fun cappedAtDazeCap() {
        assertEquals(Combos.DAZE_CAP_PCT, fighter(at(40), at(40)).dazeChancePct(0))
    }

    @Test fun zeroWithoutEffects() {
        assertEquals(0, fighter().dazeChancePct(0))
    }
}
