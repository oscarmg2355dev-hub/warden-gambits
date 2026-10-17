package com.wardengambits.app.ui.map

import com.wardengambits.app.ui.map.world.Tile
import com.wardengambits.app.ui.map.world.WorldGen
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.abs

@OptIn(ExperimentalCoroutinesApi::class)
class MapAmbienceTest {
    /** A fake display: one frame every 16 ms of virtual time. */
    private fun TestScope.frames(): suspend ((Long) -> Unit) -> Unit = { onFrame ->
        delay(16)
        onFrame(testScheduler.currentTime)
    }

    @Test
    fun ambienceStepsAboutTenTimesASecondWhenRunning() = runTest {
        var steps = 0
        val job = launch { runAmbience(running = true, frames = frames()) { steps++ } }
        advanceTimeBy(1_000)
        job.cancel()
        assertTrue("steps = $steps", steps in 9..10)
    }

    @Test
    fun ambiencePausedWhenNotRunning() = runTest {
        var steps = 0
        val job = launch { runAmbience(running = false, frames = frames()) { steps++ } }
        advanceTimeBy(1_000)
        assertTrue("the loop returns at once", job.isCompleted)
        assertEquals(0, steps)
    }

    @Test
    fun frameGateLetsThroughAboutTenFramesPerSecond() {
        val gate = FrameGate()
        val accepted = (0L until 1_000L step 10).count { gate.accept(it) }
        assertEquals(10, accepted)
    }

    @Test
    fun smokeRisesFromVisibleChimneysAndFades() {
        val world = WorldGen.generate(listOf("bree", "chet"), 200)
        assertTrue("bree has chimneys", world.chimneys.isNotEmpty())
        val state = AmbienceState(world)
        repeat(8) { state.step(0, world.width - 1) }
        assertTrue(state.smoke.isNotEmpty())
        repeat(40) { state.step(-100, -50) }
        assertFalse("no chimney is visible, so the old puffs fade out", state.smoke.isNotEmpty())
    }

    @Test
    fun lavaPulsesBetweenOrangeAndRed() {
        val colors = (0 until 40).map { lavaColor(10, 20, it) }
        for (c in colors) {
            assertTrue("red $c", c.red >= 0.7f)
            assertTrue("blue $c", c.blue <= 0.25f)
            assertTrue("green $c", c.green in 0.05f..0.75f)
        }
        assertTrue("the glow changes over time", colors.maxOf { it.green } - colors.minOf { it.green } >= 0.2f)
        assertTrue("the noise drifts across tiles", lavaColor(10, 20, 3) != lavaColor(30, 20, 3))
    }

    @Test
    fun wispsSwayGently() {
        val offsets = (0 until 60).map { wispOffset(Tile(5, 7), it) }
        for (o in offsets) assertTrue("offset $o", abs(o.x) <= 1.5f && abs(o.y) <= 2.5f)
        assertTrue("the wisp moves", offsets.distinct().size > 5)
    }

    @Test
    fun orodruinBelchesDenseSmoke() {
        val world = WorldGen.generate(listOf("gorgoroth", "orodruin"), 200)
        assertTrue("Orodruin has a vent", world.vents.isNotEmpty())
        val state = AmbienceState(world)
        repeat(10) { state.step(0, world.width - 1) }
        assertTrue("dense smoke: ${state.smoke.count { it.volcanic }}", state.smoke.count { it.volcanic } >= 8)
        repeat(80) { state.step(-200, -100) }
        assertTrue("the smoke fades when the vent is off screen", state.smoke.isEmpty())
    }

    @Test
    fun visibleColumnsFollowTheScroll() {
        assertEquals(10..35, visibleColumns(scrollPx = 100, viewportWidthPx = 250, tilePx = 10f, width = 100))
        assertEquals(90..99, visibleColumns(scrollPx = 900, viewportWidthPx = 250, tilePx = 10f, width = 100))
    }
}
