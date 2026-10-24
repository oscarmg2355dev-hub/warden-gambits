package com.wardengambits.app.audio

import com.wardengambits.app.navigation.Routes
import org.junit.Assert.assertEquals
import org.junit.Test

private class FakeMusicPlayer : MusicPlayer {
    val calls = mutableListOf<String>()
    override fun play() { calls += "play" }
    override fun pause() { calls += "pause" }
    override fun setVolume(fraction: Float, fadeMillis: Long) { calls += "volume $fraction" }
    override fun release() { calls += "release" }
}

class MusicControllerTest {
    private val player = FakeMusicPlayer()
    private val controller = MusicController(player)

    private fun update(route: String?, music: Boolean = true, narrating: Boolean = false, fg: Boolean = true) =
        controller.update(route, music, narrating, fg)

    private fun lastPlayState() = player.calls.last { it == "play" || it == "pause" }

    @Test
    fun musicPlaysOutsideCombat() {
        for (route in listOf("menu", "map", "node", "practice")) {
            val p = FakeMusicPlayer()
            MusicController(p).update(route, true, false, true)
            assertEquals(route, true, "play" in p.calls)
        }
    }

    @Test
    fun musicPausesInCombatRoutes() {
        for (route in listOf(Routes.COMBAT, "practice_combat/{zone}/{level}/{index}")) {
            val p = FakeMusicPlayer()
            val c = MusicController(p)
            c.update("map", true, false, true)
            c.update(route, true, false, true)
            assertEquals(route, "pause", p.calls.last { it == "play" || it == "pause" })
        }
    }

    @Test
    fun musicOffPauses() {
        update("map")
        update("map", music = false)
        assertEquals("pause", lastPlayState())
    }

    @Test
    fun backgroundPauses() {
        update("map")
        update("map", fg = false)
        assertEquals("pause", lastPlayState())
    }

    @Test
    fun narrationDucksAndRestores() {
        update("node")
        update("node", narrating = true)
        update("node", narrating = false)
        assertEquals(listOf("volume 1.0", "volume 0.25", "volume 1.0"), player.calls.filter { it.startsWith("volume") })
    }

    @Test
    fun noRepeatedCalls() {
        update("map")
        update("map")
        assertEquals(1, player.calls.count { it == "play" })
    }
}
