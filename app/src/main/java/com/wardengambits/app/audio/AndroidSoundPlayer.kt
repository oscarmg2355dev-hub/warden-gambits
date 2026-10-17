package com.wardengambits.app.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.SoundPool
import android.util.Log
import com.wardengambits.app.R

/**
 * [SoundPlayer] on Android: a preloaded [SoundPool] for the short effects and a looping [MediaPlayer] for
 * the combat ambience. Audio failures are logged and ignored: sound never breaks the game.
 */
class AndroidSoundPlayer(
    context: Context,
    private val warn: (String) -> Unit = { Log.w(TAG, it) },
) : SoundPlayer {
    private val appContext = context.applicationContext
    private val attributes = AudioAttributes.Builder()
        .setUsage(AudioAttributes.USAGE_GAME)
        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
        .build()
    private val pool: SoundPool = SoundPool.Builder().setMaxStreams(MAX_STREAMS).setAudioAttributes(attributes).build()
    private val soundIds: Map<Sfx, Int> = Sfx.entries.associateWith { sfx ->
        safely("load $sfx", 0) { pool.load(appContext, rawFor(sfx), 1) }
    }
    private var ambient: MediaPlayer? = null
    private val audioManager = appContext.getSystemService(AudioManager::class.java)
    private val focusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
        .setAudioAttributes(attributes)
        .setOnAudioFocusChangeListener { change ->
            if (change == AudioManager.AUDIOFOCUS_LOSS || change == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT) pauseAmbient()
        }
        .build()
    private var enabled = true

    override fun playSfx(sfx: Sfx) {
        if (!enabled) return
        val id = soundIds[sfx] ?: 0
        if (id != 0) safely("play $sfx", Unit) { pool.play(id, 1f, 1f, 1, 0, 1f) }
    }

    override fun startAmbient() {
        if (!enabled) return
        val player = ambient ?: createAmbient()?.also { ambient = it } ?: return
        safely("start ambient", Unit) {
            audioManager.requestAudioFocus(focusRequest)
            if (!player.isPlaying) player.start()
        }
    }

    override fun pauseAmbient() {
        val player = ambient ?: return
        safely("pause ambient", Unit) { if (player.isPlaying) player.pause() }
    }

    override fun stopAmbient() {
        val player = ambient ?: return
        ambient = null
        safely("stop ambient", Unit) {
            player.release()
            audioManager.abandonAudioFocusRequest(focusRequest)
        }
    }

    override fun setEnabled(enabled: Boolean) {
        this.enabled = enabled
        if (!enabled) stopAmbient()
    }

    override fun release() {
        stopAmbient()
        safely("release sound pool", Unit) { pool.release() }
    }

    private fun createAmbient(): MediaPlayer? = safely("create ambient", null) {
        val session = appContext.getSystemService(AudioManager::class.java).generateAudioSessionId()
        MediaPlayer.create(appContext, R.raw.amb_combat, attributes, session)?.apply {
            isLooping = true
            setVolume(AMBIENT_VOLUME, AMBIENT_VOLUME)
        }
    }

    private inline fun <T> safely(what: String, fallback: T, block: () -> T): T = try {
        block()
    } catch (e: RuntimeException) {
        warn("Could not $what: ${e.message}")
        fallback
    }

    private companion object {
        const val TAG = "AndroidSoundPlayer"
        const val MAX_STREAMS = 6
        const val AMBIENT_VOLUME = 0.5f

        fun rawFor(sfx: Sfx): Int = when (sfx) {
            Sfx.ACTION -> R.raw.sfx_action
            Sfx.DEFEAT_ENEMY -> R.raw.sfx_defeat_enemy
            Sfx.VICTORY -> R.raw.sfx_victory
            Sfx.DEFEAT -> R.raw.sfx_defeat
            Sfx.KEY_SWORD -> R.raw.sfx_key_sword
            Sfx.KEY_SHIELD -> R.raw.sfx_key_shield
            Sfx.KEY_PUNCH -> R.raw.sfx_key_punch
        }
    }
}
