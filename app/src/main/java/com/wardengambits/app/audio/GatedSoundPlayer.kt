package com.wardengambits.app.audio

/** Forwards to [delegate] only while sound is enabled (the menu's "Sonido" setting); disabling stops the ambience. */
class GatedSoundPlayer(private val delegate: SoundPlayer) : SoundPlayer {
    private var enabled = true

    override fun playSfx(sfx: Sfx) {
        if (enabled) delegate.playSfx(sfx)
    }

    override fun startAmbient() {
        if (enabled) delegate.startAmbient()
    }

    override fun pauseAmbient() {
        if (enabled) delegate.pauseAmbient()
    }

    override fun stopAmbient() {
        if (enabled) delegate.stopAmbient()
    }

    override fun setEnabled(enabled: Boolean) {
        if (this.enabled && !enabled) delegate.stopAmbient()
        this.enabled = enabled
    }

    override fun release() = delegate.release()
}
