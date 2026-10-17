package com.wardengambits.app.audio

import com.wardengambits.engine.event.CombatEvent
import com.wardengambits.engine.event.RejectReason
import com.wardengambits.engine.model.EffectKind
import com.wardengambits.engine.model.FighterId
import com.wardengambits.engine.model.Outcome
import com.wardengambits.engine.model.PLAYER_ID
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class SoundForTest {
    private val rival = FighterId(1)

    @Test fun rivalDefeatedPlaysDefeatEnemy() {
        assertEquals(Sfx.DEFEAT_ENEMY, soundFor(CombatEvent.FighterDefeated(rival), PLAYER_ID))
    }

    @Test fun playerDefeatedPlaysNothing() {
        assertNull(soundFor(CombatEvent.FighterDefeated(PLAYER_ID), PLAYER_ID))
    }

    @Test fun combatEndedPlaysVictoryOrDefeat() {
        assertEquals(Sfx.VICTORY, soundFor(CombatEvent.CombatEnded(Outcome.VICTORY), PLAYER_ID))
        assertEquals(Sfx.DEFEAT, soundFor(CombatEvent.CombatEnded(Outcome.DEFEAT), PLAYER_ID))
    }

    @Test fun otherEventsPlayNothing() {
        assertNull(soundFor(CombatEvent.EffectResolved(rival, PLAYER_ID, "123", null, -10), PLAYER_ID))
        assertNull(soundFor(CombatEvent.ComboRejected(PLAYER_ID, RejectReason.DAZED), PLAYER_ID))
        assertNull(soundFor(CombatEvent.EffectResolved(PLAYER_ID, PLAYER_ID, "21", EffectKind.COT, 5), PLAYER_ID))
        assertNull(soundFor(CombatEvent.PotionUsed(rival, 150), PLAYER_ID))
    }
}
