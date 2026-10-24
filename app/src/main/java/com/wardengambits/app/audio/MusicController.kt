package com.wardengambits.app.audio

import com.wardengambits.app.navigation.Routes

/** Volume fraction while the narration speaks. */
const val MUSIC_DUCK = 0.25f

/** Route patterns (as in the back stack) where the music is silent: combat has its own ambience. */
val COMBAT_ROUTES = setOf(Routes.COMBAT, Routes.PRACTICE_COMBAT, Routes.TUTORIAL_COMBAT)

/** Decides when the music plays, pauses or ducks; sends only the changes to the [MusicPlayer]. */
class MusicController(private val player: MusicPlayer) {
    private var playing: Boolean? = null
    private var volume: Float? = null

    fun update(route: String?, musicEnabled: Boolean, narrating: Boolean, foreground: Boolean) {
        val shouldPlay = musicEnabled && foreground && route !in COMBAT_ROUTES
        if (shouldPlay != playing) {
            playing = shouldPlay
            if (shouldPlay) player.play() else player.pause()
        }
        val target = if (narrating) MUSIC_DUCK else 1f
        if (target != volume) {
            volume = target
            player.setVolume(target, DUCK_FADE_MS)
        }
    }

    fun release() = player.release()

    private companion object {
        const val DUCK_FADE_MS = 300L
    }
}
