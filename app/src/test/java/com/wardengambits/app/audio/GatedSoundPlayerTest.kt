package com.wardengambits.app.audio

import org.junit.Assert.assertEquals
import org.junit.Test

class GatedSoundPlayerTest {
    private val fake = FakeSoundPlayer()
    private val gated = GatedSoundPlayer(fake)

    @Test fun enabledByDefaultForwardsEverything() {
        gated.playSfx(Sfx.ACTION)
        gated.startAmbient()
        gated.pauseAmbient()
        gated.stopAmbient()
        assertEquals(listOf("sfx:ACTION", "startAmbient", "pauseAmbient", "stopAmbient"), fake.calls)
    }

    @Test fun disabledPlaysNothing() {
        gated.setEnabled(false)
        fake.calls.clear()
        gated.playSfx(Sfx.VICTORY)
        gated.playSfx(Sfx.KEY_SWORD)
        gated.playSfx(Sfx.KEY_SHIELD)
        gated.playSfx(Sfx.KEY_PUNCH)
        gated.startAmbient()
        assertEquals(emptyList<String>(), fake.calls)
    }

    @Test fun disablingStopsAmbientThatWasPlaying() {
        gated.startAmbient()
        gated.setEnabled(false)
        assertEquals(listOf("startAmbient", "stopAmbient"), fake.calls)
    }

    @Test fun reEnablingPlaysAgain() {
        gated.setEnabled(false)
        gated.setEnabled(true)
        fake.calls.clear()
        gated.playSfx(Sfx.DEFEAT)
        assertEquals(listOf("sfx:DEFEAT"), fake.calls)
    }
}
