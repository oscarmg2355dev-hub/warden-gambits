package com.wardengambits.app.audio

/** Records every call, in order, as a short string ("sfx:ACTION", "startAmbient", ...). */
class FakeSoundPlayer : SoundPlayer {
    val calls = mutableListOf<String>()

    override fun playSfx(sfx: Sfx) { calls += "sfx:$sfx" }
    override fun startAmbient() { calls += "startAmbient" }
    override fun pauseAmbient() { calls += "pauseAmbient" }
    override fun stopAmbient() { calls += "stopAmbient" }
    override fun setEnabled(enabled: Boolean) { calls += "enabled:$enabled" }
    override fun release() { calls += "release" }
}
