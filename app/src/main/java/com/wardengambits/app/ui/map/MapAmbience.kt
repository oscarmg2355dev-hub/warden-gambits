package com.wardengambits.app.ui.map

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.animation.core.withInfiniteAnimationFrameMillis
import com.wardengambits.app.ui.map.world.Tile
import com.wardengambits.app.ui.map.world.WorldMap
import com.wardengambits.app.ui.map.world.WorldNoise
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sin

/*
 * Ambient animation over the world map, ported from the approved prototype's frame()
 * (docs/superpowers/specs/assets/2026-10-15-map-proto-v2.html): water shimmer, cloud shadows,
 * chimney smoke, Moria torches and mallorn sparkles, at about 10 fps and only over the visible tiles.
 * The road to Mordor adds pulsing lava, swaying marsh wisps and dense smoke from the Orodruin.
 */

private const val FRAME_MS = 100L
private const val TORCH_GLOW = 14
private const val SMOKE_LIFE = 24
private const val SMOKE_MARGIN = 8
private const val SPARKLE_MARGIN = 4
private const val CLOUD_STEP = 3
private const val VOLCANO_LIFE = 40
private const val VOLCANO_MARGIN = 40
private const val WISP_GLOW = 3
private const val VENT_GLOW = 6

/** Lets a frame through only when [periodMs] have passed since the last one (~10 fps). */
internal class FrameGate(private val periodMs: Long = FRAME_MS) {
    private var last = Long.MIN_VALUE

    fun accept(frameMs: Long): Boolean {
        if (last != Long.MIN_VALUE && frameMs - last < periodMs) return false
        last = frameMs
        return true
    }
}

/**
 * The ambience loop: while [running], asks [frames] for every display frame and calls [onStep] on
 * the ones [FrameGate] lets through. Returns at once when not running; otherwise runs until cancelled.
 */
internal suspend fun runAmbience(running: Boolean, frames: suspend ((Long) -> Unit) -> Unit, onStep: () -> Unit) {
    if (!running) return
    val gate = FrameGate()
    while (true) {
        frames { ms -> if (gate.accept(ms)) onStep() }
    }
}

/** Tile columns covered by the viewport, clamped to the map. */
internal fun visibleColumns(scrollPx: Int, viewportWidthPx: Int, tilePx: Float, width: Int): IntRange {
    val first = floor(scrollPx / tilePx).toInt().coerceIn(0, width - 1)
    val last = ceil((scrollPx + viewportWidthPx) / tilePx).toInt().coerceIn(0, width - 1)
    return first..last
}

/** Colour of the lava tile (x, y) at frame [t]: noise drifting across the flow plus a slow pulse, orange to red. */
internal fun lavaColor(x: Int, y: Int, t: Int): Color {
    val n = WorldNoise.valueNoise(x / 5f - t * 0.12f, y / 3f + t * 0.05f, 811)
    val pulse = 0.5f + 0.5f * sin(t * 0.35f + x * 0.07f)
    val heat = 0.6f * n + 0.4f * pulse
    return Color(0.82f + 0.18f * n, 0.12f + 0.55f * heat, 0.04f + 0.12f * n * pulse, 0.8f)
}

/** Gentle sway of a marsh wisp (in tiles) at frame [t]; each wisp has its own phase. */
internal fun wispOffset(wisp: Tile, t: Int): Offset {
    val phase = WorldNoise.h01(wisp.x, wisp.y, 9) * 6.28f
    return Offset(sin(t * 0.09f + phase) * 1.2f, sin(t * 0.13f + phase * 1.7f) * 1.6f)
}

/** A puff of smoke: light and sparse from chimneys, dark and dense ([volcanic]) from the Orodruin. */
internal class SmokePuff(val x: Int, val y: Int, val volcanic: Boolean = false, val drift: Float = 0f) {
    var age = 0
}

/** Mutable animation state: the frame counter `t` and the smoke puffs, stepped ~10 times a second. */
internal class AmbienceState(private val world: WorldMap) {
    var t = 0
        private set
    val smoke = ArrayList<SmokePuff>()

    /** Water tile indices bucketed by column, so a frame only visits the visible columns. */
    private val waterStart = IntArray(world.width + 1)
    private val waterByColumn = byColumn(world.water, waterStart)

    /** Lava tile indices bucketed by column, like the water. */
    private val lavaStart = IntArray(world.width + 1)
    private val lavaByColumn = byColumn(world.lava, lavaStart)

    private fun byColumn(tiles: IntArray, start: IntArray): IntArray {
        val out = IntArray(tiles.size)
        for (i in tiles) start[i % world.width + 1]++
        for (x in 0 until world.width) start[x + 1] += start[x]
        val fill = start.copyOf()
        for (i in tiles) out[fill[i % world.width]++] = i
        return out
    }

    fun step(firstColumn: Int, lastColumn: Int) {
        t++
        if (t % 2 == 0) {
            for (c in world.chimneys) {
                if (c.x in firstColumn - SMOKE_MARGIN..lastColumn + SMOKE_MARGIN &&
                    WorldNoise.h01(c.x, c.y, t shr 2) < 0.4f
                ) {
                    smoke += SmokePuff(c.x, c.y)
                }
            }
        }
        // The Orodruin smokes all the time: one or two dark puffs per step.
        for (v in world.vents) {
            if (v.x !in firstColumn - VOLCANO_MARGIN..lastColumn + VOLCANO_MARGIN) continue
            val puffs = if (WorldNoise.h01(v.x, v.y, t) < 0.5f) 2 else 1
            repeat(puffs) { k -> smoke += SmokePuff(v.x, v.y, volcanic = true, drift = WorldNoise.h01(v.x + k, v.y, t) * 2 - 1) }
        }
        val it = smoke.iterator()
        while (it.hasNext()) {
            val s = it.next()
            s.age++
            if (s.age > (if (s.volcanic) VOLCANO_LIFE else SMOKE_LIFE)) it.remove()
        }
    }

    fun draw(scope: DrawScope, columns: IntRange, tile: Float) = with(scope) {
        fun cell(x: Float, y: Float, w: Float, h: Float, color: Color) =
            drawRect(color, Offset(x * tile, y * tile), Size(w * tile, h * tile))

        val w = world.width
        val h = world.height
        // Water shimmer and shade.
        for (x in columns) {
            for (k in waterStart[x] until waterStart[x + 1]) {
                val y = waterByColumn[k] / w
                val v = WorldNoise.valueNoise(x / 4f + t * 0.18f, y / 2.5f, 5)
                if (v > 0.74f) cell(x.toFloat(), y.toFloat(), 1f, 1f, WATER_LIGHT)
                else if (v < 0.22f) cell(x.toFloat(), y.toFloat(), 1f, 1f, WATER_SHADE)
            }
        }
        // Pulsing lava.
        for (x in columns) {
            for (k in lavaStart[x] until lavaStart[x + 1]) {
                cell(x.toFloat(), (lavaByColumn[k] / w).toFloat(), 1f, 1f, lavaColor(x, lavaByColumn[k] / w, t))
            }
        }
        // Cloud shadows drifting east.
        var x = columns.first / CLOUD_STEP * CLOUD_STEP
        while (x <= columns.last) {
            var y = 0
            while (y < h) {
                val v = WorldNoise.valueNoise((x - t * 0.45f) / 44f, y / 28f, 333)
                if (v > 0.6f) {
                    cell(x.toFloat(), y.toFloat(), 3f, 3f, Color(0f, 0f, 25 / 255f, min(0.2f, (v - 0.6f) * 1.1f)))
                }
                y += CLOUD_STEP
            }
            x += CLOUD_STEP
        }
        // Glow of the crater under its smoke.
        for (v in world.vents) {
            if (!v.near(columns, VENT_GLOW)) continue
            val f = WorldNoise.h01(v.x, v.y, t shr 1)
            val center = Offset((v.x + 0.5f) * tile, (v.y + 0.5f) * tile)
            drawRect(
                Brush.radialGradient(
                    listOf(Color(1f, 110 / 255f, 30 / 255f, 0.45f + 0.2f * f), Color(1f, 60 / 255f, 10 / 255f, 0f)),
                    center = center,
                    radius = tile * VENT_GLOW,
                ),
                Offset((v.x - VENT_GLOW) * tile, (v.y - VENT_GLOW) * tile),
                Size(2 * VENT_GLOW * tile, 2 * VENT_GLOW * tile),
            )
        }
        // Chimney smoke and the Orodruin's dark plume.
        for (s in smoke) {
            if (s.volcanic) {
                val sx = s.x + s.drift * 2 + sin(s.age / 5f) * 1.5f + s.age * 0.35f
                val sy = s.y - 1 - s.age * 0.8f
                val r = 2 + s.age / 8
                val shade = 50 + s.age
                val color = Color(shade / 255f, (shade - 6) / 255f, (shade - 10) / 255f, (0.7f - s.age * 0.016f).coerceIn(0f, 1f))
                cell(sx.roundToInt().toFloat() - r / 2, sy.roundToInt().toFloat() - r / 2, r.toFloat(), r.toFloat(), color)
                continue
            }
            val sx = s.x + sin(s.age / 4f) * 1.2f + s.age * 0.25f
            val sy = s.y - s.age * 0.7f
            val r = 1 + s.age / 9
            val color = Color(225 / 255f, 225 / 255f, 230 / 255f, (0.55f - s.age * 0.022f).coerceIn(0f, 1f))
            cell(sx.roundToInt().toFloat(), sy.roundToInt().toFloat(), r.toFloat(), r.toFloat(), color)
        }
        // Moria torches: flickering glow, stick, flame and core.
        for (torch in world.torches) {
            if (!torch.near(columns, TORCH_GLOW)) continue
            val tx = torch.x.toFloat()
            val ty = torch.y.toFloat()
            val f = WorldNoise.h01(torch.x, torch.y, t shr 1)
            val center = Offset((tx + 0.5f) * tile, (ty + 0.5f) * tile)
            val glow = Brush.radialGradient(
                listOf(Color(1f, 170 / 255f, 60 / 255f, 0.35f + 0.15f * f), Color(1f, 120 / 255f, 30 / 255f, 0f)),
                center = center,
                radius = tile * TORCH_GLOW,
            )
            drawRect(
                glow,
                Offset((tx - TORCH_GLOW) * tile, (ty - TORCH_GLOW) * tile),
                Size(2 * TORCH_GLOW * tile, 2 * TORCH_GLOW * tile),
            )
            cell(tx, ty + 1, 1f, 3f, TORCH_STICK)
            cell(tx, ty - 1, 1f, 2f, if (f < 0.5f) FLAME_YELLOW else FLAME_ORANGE)
            cell(tx, ty, 1f, 1f, FLAME_CORE)
        }
        // Mallorn sparkles.
        for (m in world.mallorns) {
            if (!m.near(columns, SPARKLE_MARGIN)) continue
            val v = WorldNoise.h01(m.x, m.y, t shr 2)
            if (v < 0.3f) {
                val dx = floor(v * 37).toInt() % 9 - 4
                val dy = floor(v * 53).toInt() % 7 - 3
                cell((m.x + dx).toFloat(), (m.y + dy).toFloat(), 1f, 1f, SPARKLE)
            }
        }
        drawWisps(scope, columns, tile)
    }

    private fun drawWisps(scope: DrawScope, columns: IntRange, tile: Float) = with(scope) {
        for (wisp in world.wisps) {
            if (!wisp.near(columns, WISP_GLOW + 2)) continue
            val o = wispOffset(wisp, t)
            val cx = wisp.x + 0.5f + o.x
            val cy = wisp.y + 0.5f + o.y
            val f = WorldNoise.h01(wisp.x, wisp.y, t shr 1)
            drawRect(
                Brush.radialGradient(
                    listOf(Color(170 / 255f, 1f, 220 / 255f, 0.3f + 0.15f * f), Color(120 / 255f, 220 / 255f, 200 / 255f, 0f)),
                    center = Offset(cx * tile, cy * tile),
                    radius = tile * WISP_GLOW,
                ),
                Offset((cx - WISP_GLOW) * tile, (cy - WISP_GLOW) * tile),
                Size(2 * WISP_GLOW * tile, 2 * WISP_GLOW * tile),
            )
            drawRect(WISP_CORE, Offset((cx - 0.5f) * tile, (cy - 0.5f) * tile), Size(tile, tile))
        }
    }

    private fun Tile.near(columns: IntRange, margin: Int): Boolean =
        x in columns.first - margin..columns.last + margin
}

private val WATER_LIGHT = Color(220 / 255f, 240 / 255f, 1f, 0.5f)
private val WATER_SHADE = Color(0f, 20 / 255f, 60 / 255f, 0.22f)
private val TORCH_STICK = Color(0xFF4A3020)
private val FLAME_YELLOW = Color(0xFFFFD060)
private val FLAME_ORANGE = Color(0xFFFF8A20)
private val FLAME_CORE = Color(0xFFFFF2B0)
private val SPARKLE = Color(1f, 250 / 255f, 220 / 255f, 0.9f)
private val WISP_CORE = Color(225 / 255f, 1f, 240 / 255f, 0.85f)

/**
 * Animated layer drawn over the world image, in world coordinates (it fills the scrolled content).
 * Only the columns inside [viewportWidthPx] from [scrollPx] are visited; the frame loop stops while
 * [running] is false (screen below STARTED). Uses the infinite-animation policy, so it is also off
 * in UI tests.
 */
@Composable
fun MapAmbience(
    world: WorldMap,
    scrollPx: () -> Int,
    viewportWidthPx: Int,
    tilePx: Float,
    running: Boolean,
    modifier: Modifier = Modifier,
) {
    val state = remember(world) { AmbienceState(world) }
    var frame by remember(world) { mutableIntStateOf(0) }
    val currentScroll by rememberUpdatedState(scrollPx)
    LaunchedEffect(state, running, viewportWidthPx, tilePx) {
        runAmbience(running, { onFrame -> withInfiniteAnimationFrameMillis(onFrame) }) {
            val columns = visibleColumns(currentScroll(), viewportWidthPx, tilePx, world.width)
            state.step(columns.first, columns.last)
            frame++
        }
    }
    Canvas(modifier.fillMaxSize()) {
        // Reading the frame state in the draw phase redraws this layer on every step.
        if (frame >= 0) state.draw(this, visibleColumns(scrollPx(), viewportWidthPx, tilePx, world.width), tilePx)
    }
}
