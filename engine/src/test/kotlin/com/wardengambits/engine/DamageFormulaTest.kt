package com.wardengambits.engine

import com.wardengambits.engine.combo.ComboTuning
import com.wardengambits.engine.rules.DamageFormula
import kotlin.test.Test
import kotlin.test.assertEquals

class DamageFormulaTest {
    private val tuning = ComboTuning.DEFAULT

    @Test fun defaultTuning() = assertEquals(ComboTuning(spearBasePct = 16, fistBasePct = 21, cureBasePct = 35), tuning)

    /** Level 1 (attack 1000, heal 800) and level 10 (attack 2800, heal 2600) of the x100 stat scale. */
    @Test fun units() {
        assertEquals(160, DamageFormula.spearUnit(1_000, tuning))
        assertEquals(448, DamageFormula.spearUnit(2_800, tuning))
        assertEquals(210, DamageFormula.fistUnit(1_000, tuning))
        assertEquals(588, DamageFormula.fistUnit(2_800, tuning))
        assertEquals(280, DamageFormula.cureUnit(800, tuning))
        assertEquals(910, DamageFormula.cureUnit(2_600, tuning))
        assertEquals(1, DamageFormula.spearUnit(9, tuning), "integer floor of 1.44")
    }

    @Test fun spearDot() {
        assertEquals(10, DamageFormula.spearDot(tier = 2, spearUnit = 5, fury = false))
        assertEquals(12, DamageFormula.spearDot(tier = 2, spearUnit = 5, fury = true))
        assertEquals(2, DamageFormula.spearDot(tier = 1, spearUnit = 2, fury = true))
    }

    @Test fun areaDot() = assertEquals(12, DamageFormula.areaDot(tier = 4, fistUnit = 3))

    @Test fun reducedDot() {
        assertEquals(10, DamageFormula.received(10, guard = false, masterShield = false))
        assertEquals(8, DamageFormula.received(10, guard = true, masterShield = false))
        assertEquals(5, DamageFormula.received(10, guard = false, masterShield = true))
        assertEquals(4, DamageFormula.received(10, guard = true, masterShield = true))
        assertEquals(1, DamageFormula.received(1, guard = true, masterShield = true))
        assertEquals(1, DamageFormula.received(0, guard = false, masterShield = false))
    }

    @Test fun directHit() {
        assertEquals(18, DamageFormula.directHit(spearUnit = 2, fury = false, defense = 2, guard = false, masterShield = false))
        assertEquals(22, DamageFormula.directHit(spearUnit = 2, fury = true, defense = 2, guard = false, masterShield = false))
        assertEquals(14, DamageFormula.directHit(spearUnit = 2, fury = false, defense = 2, guard = true, masterShield = false))
        assertEquals(7, DamageFormula.directHit(spearUnit = 2, fury = false, defense = 2, guard = true, masterShield = true))
        assertEquals(1, DamageFormula.directHit(spearUnit = 2, fury = false, defense = 50, guard = true, masterShield = true))
    }
}
