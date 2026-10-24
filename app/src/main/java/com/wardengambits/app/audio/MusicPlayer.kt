package com.wardengambits.app.audio

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.content.Context
import android.media.MediaPlayer
import android.util.Log
import com.wardengambits.app.R

/** Background music: a playlist that loops. [play] fades in, [pause] fades out, [setVolume] ducks or restores. */
interface MusicPlayer {
    fun play()
    fun pause()
    fun setVolume(fraction: Float, fadeMillis: Long)
    fun release()
}

/** Plays res/raw/music_*.ogg one after another, forever. MediaPlayers are created on the first [play]. */
class AndroidMusicPlayer(context: Context) : MusicPlayer {
    private val appContext = context.applicationContext
    private val tracks = intArrayOf(R.raw.music_1, R.raw.music_2, R.raw.music_3)
    private var index = 0
    private var player: MediaPlayer? = null
    private var wanted = false
    private var gain = 1f
    private var fade = 0f
    private var gainAnim: ValueAnimator? = null
    private var fadeAnim: ValueAnimator? = null
    private var released = false

    override fun play() {
        if (released) return
        wanted = true
        if (player == null) player = createPlayer()
        val p = player ?: return
        try {
            if (!p.isPlaying) p.start()
        } catch (e: IllegalStateException) {
            Log.w(TAG, "Could not start the music", e)
            return
        }
        animateFade(1f, FADE_IN_MS)
    }

    override fun pause() {
        if (released) return
        wanted = false
        animateFade(0f, FADE_OUT_MS) {
            if (!wanted) runCatching { player?.takeIf { it.isPlaying }?.pause() }
        }
    }

    override fun setVolume(fraction: Float, fadeMillis: Long) {
        if (released) return
        gainAnim?.cancel()
        if (fadeMillis <= 0) {
            gain = fraction
            applyVolume()
            return
        }
        gainAnim = ValueAnimator.ofFloat(gain, fraction).apply {
            duration = fadeMillis
            addUpdateListener { gain = it.animatedValue as Float; applyVolume() }
            start()
        }
    }

    override fun release() {
        released = true
        gainAnim?.cancel()
        fadeAnim?.cancel()
        runCatching { player?.release() }
        player = null
    }

    private fun animateFade(target: Float, millis: Long, onEnd: (() -> Unit)? = null) {
        fadeAnim?.cancel()
        fadeAnim = ValueAnimator.ofFloat(fade, target).apply {
            duration = millis
            addUpdateListener { fade = it.animatedValue as Float; applyVolume() }
            addListener(object : AnimatorListenerAdapter() {
                private var cancelled = false
                override fun onAnimationCancel(animation: Animator) { cancelled = true }
                override fun onAnimationEnd(animation: Animator) { if (!cancelled) onEnd?.invoke() }
            })
            start()
        }
    }

    private fun applyVolume() {
        val v = (gain * fade).coerceIn(0f, 1f)
        runCatching { player?.setVolume(v, v) }
    }

    private fun createPlayer(): MediaPlayer? = try {
        val afd = appContext.resources.openRawResourceFd(tracks[index])
        MediaPlayer().apply {
            setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
            afd.close()
            prepare()
            setOnCompletionListener { onTrackEnded() }
            val v = (gain * fade).coerceIn(0f, 1f)
            setVolume(v, v)
        }
    } catch (e: Exception) {
        Log.w(TAG, "Could not prepare the music", e)
        null
    }

    private fun onTrackEnded() {
        runCatching { player?.release() }
        index = (index + 1) % tracks.size
        player = createPlayer()
        if (wanted) runCatching { player?.start() }
    }

    private companion object {
        const val TAG = "AndroidMusicPlayer"
        const val FADE_IN_MS = 1500L
        const val FADE_OUT_MS = 800L
    }
}
