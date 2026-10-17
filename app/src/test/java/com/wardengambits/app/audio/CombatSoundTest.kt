package com.wardengambits.app.audio

import com.wardengambits.engine.combo.Input
import com.wardengambits.engine.event.CombatEvent
import com.wardengambits.engine.model.FighterId
import com.wardengambits.engine.model.Outcome
import com.wardengambits.engine.model.PLAYER_ID
import org.junit.Assert.assertEquals
import org.junit.Test

class CombatSoundTest {
    private val fake = FakeSoundPlayer()
    private val sound = CombatSound(fake, PLAYER_ID)

    @Test fun actionPressPlaysActionSfx() {
        sound.onAction()
        assertEquals(listOf("sfx:ACTION"), fake.calls)
    }

    @Test fun keyPressesPlaySwordShieldAndShout() {
        sound.onKey(Input.SPEAR)
        sound.onKey(Input.SHIELD)
        sound.onKey(Input.FIST)
        assertEquals(listOf("sfx:KEY_SWORD", "sfx:KEY_SHIELD", "sfx:KEY_PUNCH"), fake.calls)
    }

    @Test fun ambientStartsOnlyWhenActiveAndRunning() {
        sound.update(active = false, paused = false, outcome = null)
        sound.update(active = true, paused = true, outcome = null)
        assertEquals(emptyList<String>(), fake.calls)
        sound.update(active = true, paused = false, outcome = null)
        sound.update(active = true, paused = false, outcome = null)
        assertEquals(listOf("startAmbient"), fake.calls)
    }

    @Test fun ambientPausesOnPauseOrBackgroundAndResumes() {
        sound.update(active = true, paused = false, outcome = null)
        sound.update(active = true, paused = true, outcome = null)
        sound.update(active = true, paused = false, outcome = null)
        sound.update(active = false, paused = true, outcome = null)
        assertEquals(listOf("startAmbient", "pauseAmbient", "startAmbient", "pauseAmbient"), fake.calls)
    }

    @Test fun outcomeStopsAmbientAndEndEventPlaysFanfare() {
        sound.update(active = true, paused = false, outcome = null)
        sound.onEvent(CombatEvent.FighterDefeated(FighterId(1)))
        sound.onEvent(CombatEvent.CombatEnded(Outcome.VICTORY))
        sound.update(active = true, paused = false, outcome = Outcome.VICTORY)
        sound.update(active = false, paused = false, outcome = Outcome.VICTORY)
        assertEquals(listOf("startAmbient", "sfx:DEFEAT_ENEMY", "sfx:VICTORY", "stopAmbient"), fake.calls)
    }

    @Test fun leavingTheScreenStopsAmbient() {
        sound.update(active = true, paused = false, outcome = null)
        sound.dispose()
        sound.dispose()
        assertEquals(listOf("startAmbient", "stopAmbient"), fake.calls)
    }

    @Test fun leavingWhilePausedStopsAmbient() {
        sound.update(active = true, paused = false, outcome = null)
        sound.update(active = false, paused = true, outcome = null)
        sound.dispose()
        assertEquals(listOf("startAmbient", "pauseAmbient", "stopAmbient"), fake.calls)
    }
}
