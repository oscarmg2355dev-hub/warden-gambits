package com.wardengambits.app.ui.combat

import com.wardengambits.engine.event.CombatEvent
import com.wardengambits.engine.model.EffectKind
import com.wardengambits.engine.model.FighterId
import com.wardengambits.engine.model.Outcome
import com.wardengambits.engine.model.PLAYER_ID
import com.wardengambits.app.ui.theme.WardenColors
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class CombatTextTest {
    private val r1 = FighterId(1)
    private val r2 = FighterId(2)

    private fun resolved(target: FighterId, combo: String, kind: EffectKind?, amount: Int) =
        CombatEvent.EffectResolved(target, PLAYER_ID, combo, kind, amount)

    @Test fun eventsMapToTextColourAndFighter() {
        assertEquals(CombatTextLine(r1, "−4 SP2", CombatTextTone.DAMAGE, -4), combatTextFor(resolved(r1, "121", EffectKind.DOT, -4)))
        assertEquals(CombatTextLine(PLAYER_ID, "+3 CU1", CombatTextTone.HEAL, 3), combatTextFor(resolved(PLAYER_ID, "21", EffectKind.COT, 3)))
        assertEquals(CombatTextLine(r2, "−18 GD", CombatTextTone.DAMAGE, -18), combatTextFor(resolved(r2, "123", null, -18)))
        assertEquals(CombatTextLine(r1, "−2 PA1", CombatTextTone.DAMAGE, -2), combatTextFor(resolved(r1, "32", EffectKind.DOT, -2)))
        assertEquals(CombatTextLine(PLAYER_ID, "+4 RV1", CombatTextTone.HEAL, 4), combatTextFor(resolved(PLAYER_ID, "3123", EffectKind.DRAIN, 4)))
        assertEquals(CombatTextLine(r1, "−5", CombatTextTone.DAMAGE, -5), combatTextFor(resolved(r1, "999", null, -5)))
    }

    /** Rival potion (2026-10-13): "+150 PO" in the drinker's strip, in the power colour. */
    @Test fun potionShowsPowerGained() {
        assertEquals(CombatTextLine(r1, "+150 PO", CombatTextTone.POWER, 150), combatTextFor(CombatEvent.PotionUsed(r1, 150)))
    }

    @Test fun linesCarryTheRollPct() {
        val event = CombatEvent.EffectResolved(r1, PLAYER_ID, "121", EffectKind.DOT, -4, 135)
        assertEquals(CombatTextLine(r1, "−4 SP2", CombatTextTone.DAMAGE, -4, 135), combatTextFor(event))
    }

    @Test fun tonesHaveTheirColours() {
        assertEquals(DamageRed, combatTextColor(CombatTextTone.DAMAGE))
        assertEquals(HealGreen, combatTextColor(CombatTextTone.HEAL))
        assertEquals(WardenColors.Power, combatTextColor(CombatTextTone.POWER))
    }

    @Test fun otherEventsGiveNothing() {
        assertNull(combatTextFor(CombatEvent.CombatEnded(Outcome.VICTORY)))
        assertNull(combatTextFor(CombatEvent.FighterDefeated(r1)))
    }

    /** An effect that changed nothing (a heal at full HP, a drain or a tick that did no harm) shows no number. */
    @Test fun effectsAtZeroShowNothing() {
        assertNull(combatTextFor(resolved(PLAYER_ID, "3123", EffectKind.DRAIN, 0)))
        assertNull(combatTextFor(resolved(PLAYER_ID, "21", EffectKind.COT, 0)))
        assertNull(combatTextFor(resolved(PLAYER_ID, "212", EffectKind.COT, 0)))
        assertNull(combatTextFor(resolved(r1, "121", EffectKind.DOT, 0)))
        assertNull(combatTextFor(resolved(r1, "123", null, 0)))
    }
}
