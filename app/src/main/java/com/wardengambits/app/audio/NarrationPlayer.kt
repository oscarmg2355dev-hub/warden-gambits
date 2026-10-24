package com.wardengambits.app.audio

import android.content.Context
import android.media.MediaPlayer
import android.util.Log
import com.wardengambits.app.i18n.AppLanguage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class NarrationState { IDLE, PLAYING }

/** Plays the recorded narration of a node (`assets/narration/<lang>/<key>.m4a`). */
interface NarrationPlayer {
    val state: StateFlow<NarrationState>
    fun hasAudio(key: String, language: AppLanguage): Boolean
    fun play(key: String, language: AppLanguage)
    fun stop()
    fun release()
}

/** Decides when the narration starts and stops; the player does the sound. */
class NarrationController(private val player: NarrationPlayer) {
    val state: StateFlow<NarrationState> get() = player.state

    /** The last node read on its own, so coming back to it (after its fight) does not read it again. */
    private var lastAutoPlayed: Pair<String, AppLanguage>? = null

    fun hasAudio(key: String, language: AppLanguage) = player.hasAudio(key, language)

    /**
     * A node (or the ending) came on screen: stop the previous narration and, if enabled and recorded, start this one,
     * unless it is the node that was just read on its own (back from its fight); the button still reads it.
     */
    fun onNodeShown(key: String, language: AppLanguage, enabled: Boolean) {
        player.stop()
        if (!enabled || !player.hasAudio(key, language) || lastAutoPlayed == key to language) return
        lastAutoPlayed = key to language
        start(key, language)
    }

    fun onLeave() = player.stop()

    /** The "Parar"/"Leer" button. */
    fun toggle(key: String, language: AppLanguage) {
        if (player.state.value == NarrationState.PLAYING) {
            player.stop()
        } else if (player.hasAudio(key, language)) {
            start(key, language)
        }
    }

    fun release() = player.release()

    private fun start(key: String, language: AppLanguage) {
        try {
            player.play(key, language)
        } catch (_: Exception) {
            // A broken recording must never crash a screen; the player reports its own failures.
        }
    }
}

class AndroidNarrationPlayer(context: Context) : NarrationPlayer {
    private val appContext = context.applicationContext
    private val available = mutableMapOf<AppLanguage, Set<String>>()
    private var player: MediaPlayer? = null
    private val _state = MutableStateFlow(NarrationState.IDLE)
    override val state: StateFlow<NarrationState> = _state.asStateFlow()

    override fun hasAudio(key: String, language: AppLanguage): Boolean {
        val names = available.getOrPut(language) {
            runCatching { appContext.assets.list("narration/${language.code}")?.toSet() }.getOrNull().orEmpty()
        }
        return "$key.m4a" in names
    }

    override fun play(key: String, language: AppLanguage) {
        stop()
        val p = MediaPlayer()
        try {
            appContext.assets.openFd("narration/${language.code}/$key.m4a").use {
                p.setDataSource(it.fileDescriptor, it.startOffset, it.length)
            }
            p.setOnCompletionListener { finish(it) }
            p.setOnErrorListener { mp, _, _ -> finish(mp); true }
            p.prepare()
            player = p
            p.start()
            _state.value = NarrationState.PLAYING
        } catch (e: Exception) {
            Log.w(TAG, "Could not play $key", e)
            runCatching { p.release() }
            player = null
            _state.value = NarrationState.IDLE
        }
    }

    private fun finish(mp: MediaPlayer) {
        runCatching { mp.release() }
        if (player === mp) {
            player = null
            _state.value = NarrationState.IDLE
        }
    }

    override fun stop() {
        val p = player
        player = null
        if (p != null) {
            runCatching { p.stop() }
            runCatching { p.release() }
        }
        _state.value = NarrationState.IDLE
    }

    override fun release() = stop()

    private companion object {
        const val TAG = "NarrationPlayer"
    }
}
