package com.wardengambits.app.ui.theme

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.ImageShader
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.drawscope.CanvasDrawScope
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

/**
 * App-wide background: [base] colour plus a static, WhatsApp-wallpaper-like doodle tile of the game's motifs (the One
 * Ring, the Eye, Mount Doom, the Warden's spear and shield, the combo keys…) drawn as thin gold line-art, faint but
 * visible at normal phone brightness. The tile is rendered once into an [ImageBitmap] and repeated with an [ImageShader].
 */
fun Modifier.rpgBackground(base: Color = WardenColors.Background): Modifier = drawWithCache {
    val tilePx = RpgPattern.TILE.toPx().roundToInt().coerceAtLeast(1)
    val tile = ImageBitmap(tilePx, tilePx)
    CanvasDrawScope().draw(this, layoutDirection, Canvas(tile), Size(tilePx.toFloat(), tilePx.toFloat())) {
        RpgPattern.drawTile(this)
    }
    val brush = ShaderBrush(ImageShader(tile, TileMode.Repeated, TileMode.Repeated))
    onDrawBehind {
        drawRect(base)
        drawRect(brush)
    }
}

/** The motifs and their placement inside the repeating tile. */
internal object RpgPattern {
    val TILE = 300.dp
    private val MOTIF = 40.dp
    private val STROKE = 1.5.dp
    private val LineColor = WardenColors.Primary.copy(alpha = 0.14f)

    enum class Motif {
        ONE_RING, EYE, MOUNT_DOOM, WARDEN_SPEAR, WARDEN_SHIELD, LORIEN_LEAF, WHITE_TREE, HOBBIT_DOOR,
        DURIN_STAR, HORN, POTION, COMBO_KEYS, SWORD, STAR, NAZGUL_CROWN, SCROLL,
    }

    /** Centre (fraction of the tile), rotation in degrees and scale of each motif. */
    private data class Placement(val motif: Motif, val x: Float, val y: Float, val degrees: Float, val scale: Float)

    /** Motifs of the 6 × 4 grid, row by row, ordered so that neighbours differ. */
    private val grid = listOf(
        Motif.ONE_RING, Motif.WARDEN_SPEAR, Motif.EYE, Motif.WHITE_TREE,
        Motif.COMBO_KEYS, Motif.MOUNT_DOOM, Motif.LORIEN_LEAF, Motif.WARDEN_SHIELD,
        Motif.HOBBIT_DOOR, Motif.STAR, Motif.HORN, Motif.POTION,
        Motif.NAZGUL_CROWN, Motif.SWORD, Motif.DURIN_STAR, Motif.SCROLL,
        Motif.LORIEN_LEAF, Motif.ONE_RING, Motif.COMBO_KEYS, Motif.MOUNT_DOOM,
        Motif.HORN, Motif.HOBBIT_DOOR, Motif.POTION, Motif.WARDEN_SHIELD,
    )

    /** A jittered grid (odd rows shifted half a column) with deterministic pseudo-random rotation and scale. */
    private val placements = grid.mapIndexed { i, motif ->
        val row = i / COLS
        val col = i % COLS
        val jitterX = ((i * 29) % 17 - 8) / 300f
        val jitterY = ((i * 41) % 13 - 6) / 300f
        Placement(
            motif = motif,
            x = ((col + 0.5f + if (row % 2 == 1) 0.5f else 0f) / COLS + jitterX).mod(1f),
            y = ((row + 0.5f) / ROWS + jitterY).mod(1f),
            degrees = ((i * 37) % 50 - 25).toFloat(),
            scale = 0.8f + ((i * 53) % 40) / 100f,
        )
    }

    private const val COLS = 4
    private const val ROWS = 6

    val motifCount: Int get() = placements.map { it.motif }.distinct().size
    val placementCount: Int get() = placements.size

    fun drawTile(scope: DrawScope) = with(scope) {
        val stroke = Stroke(width = STROKE.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
        for (p in placements) {
            val s = MOTIF.toPx() * p.scale
            val path = motifPath(p.motif, s)
            // Draw the wrapped copies too, so a motif crossing an edge continues on the opposite side of the next tile.
            for (dx in -1..1) for (dy in -1..1) {
                val cx = (p.x + dx) * size.width
                val cy = (p.y + dy) * size.height
                if (cx + s < 0f || cx - s > size.width || cy + s < 0f || cy - s > size.height) continue
                rotate(p.degrees, pivot = Offset(cx, cy)) {
                    translate(cx - s / 2f, cy - s / 2f) { drawPath(path, LineColor, style = stroke) }
                }
            }
        }
    }

    /** Line-art outline of [motif] inside an [s] × [s] box; coordinates below are fractions of the box. */
    private fun motifPath(motif: Motif, s: Float): Path = Path().apply {
        fun m(x: Float, y: Float) = moveTo(x * s, y * s)
        fun l(x: Float, y: Float) = lineTo(x * s, y * s)
        fun q(x1: Float, y1: Float, x2: Float, y2: Float) = quadraticTo(x1 * s, y1 * s, x2 * s, y2 * s)
        fun circle(x: Float, y: Float, r: Float) = addOval(Rect(Offset(x * s, y * s), r * s))
        fun oval(l: Float, t: Float, r: Float, b: Float) = addOval(Rect(l * s, t * s, r * s, b * s))
        fun star(cx: Float, cy: Float, outer: Float, inner: Float, points: Int) {
            for (i in 0 until points * 2) {
                val r = if (i % 2 == 0) outer else inner
                val a = -PI / 2 + i * PI / points
                val x = cx + r * cos(a).toFloat()
                val y = cy + r * sin(a).toFloat()
                if (i == 0) m(x, y) else l(x, y)
            }
            close()
        }
        when (motif) {
            Motif.ONE_RING -> {
                oval(0.12f, 0.3f, 0.88f, 0.72f)
                oval(0.24f, 0.39f, 0.76f, 0.63f)
                star(0.82f, 0.2f, 0.1f, 0.03f, 4)
            }
            Motif.EYE -> {
                m(0.06f, 0.52f); q(0.5f, 0.14f, 0.94f, 0.52f); q(0.5f, 0.9f, 0.06f, 0.52f); close()
                m(0.5f, 0.32f); q(0.57f, 0.52f, 0.5f, 0.72f); q(0.43f, 0.52f, 0.5f, 0.32f); close()
                m(0.28f, 0.28f); l(0.2f, 0.12f)
                m(0.5f, 0.22f); l(0.5f, 0.04f)
                m(0.72f, 0.28f); l(0.8f, 0.12f)
            }
            Motif.MOUNT_DOOM -> {
                m(0.04f, 0.92f); l(0.36f, 0.42f); l(0.44f, 0.48f); l(0.56f, 0.48f); l(0.64f, 0.42f); l(0.96f, 0.92f); close()
                m(0.5f, 0.38f); q(0.36f, 0.27f, 0.5f, 0.18f); q(0.64f, 0.1f, 0.52f, 0.02f)
                m(0.5f, 0.5f); q(0.44f, 0.64f, 0.52f, 0.78f)
            }
            Motif.WARDEN_SPEAR -> {
                m(0.16f, 0.9f); l(0.72f, 0.34f)
                m(0.7f, 0.3f); q(0.8f, 0.14f, 0.95f, 0.05f); q(0.86f, 0.2f, 0.76f, 0.36f); close()
                m(0.62f, 0.34f); l(0.72f, 0.44f)
                circle(0.14f, 0.92f, 0.03f)
            }
            Motif.WARDEN_SHIELD -> {
                m(0.18f, 0.12f); l(0.82f, 0.12f); l(0.82f, 0.48f); q(0.82f, 0.8f, 0.5f, 0.96f); q(0.18f, 0.8f, 0.18f, 0.48f); close()
                m(0.3f, 0.52f); l(0.5f, 0.34f); l(0.7f, 0.52f)
                m(0.3f, 0.68f); l(0.5f, 0.5f); l(0.7f, 0.68f)
            }
            Motif.LORIEN_LEAF -> {
                m(0.5f, 0.04f); q(0.88f, 0.42f, 0.5f, 0.8f); q(0.12f, 0.42f, 0.5f, 0.04f); close()
                m(0.5f, 0.12f); l(0.5f, 0.96f)
                m(0.5f, 0.34f); l(0.64f, 0.24f)
                m(0.5f, 0.5f); l(0.36f, 0.4f)
                m(0.5f, 0.64f); l(0.62f, 0.56f)
            }
            Motif.WHITE_TREE -> {
                m(0.5f, 0.92f); l(0.5f, 0.2f)
                m(0.5f, 0.62f); q(0.36f, 0.52f, 0.24f, 0.38f)
                m(0.5f, 0.62f); q(0.64f, 0.52f, 0.76f, 0.38f)
                m(0.5f, 0.46f); q(0.42f, 0.36f, 0.36f, 0.24f)
                m(0.5f, 0.46f); q(0.58f, 0.36f, 0.64f, 0.24f)
                m(0.5f, 0.92f); l(0.38f, 0.97f); m(0.5f, 0.92f); l(0.62f, 0.97f)
                circle(0.28f, 0.12f, 0.035f); circle(0.5f, 0.07f, 0.035f); circle(0.72f, 0.12f, 0.035f)
            }
            Motif.HOBBIT_DOOR -> {
                circle(0.5f, 0.5f, 0.4f)
                circle(0.5f, 0.5f, 0.31f)
                circle(0.5f, 0.5f, 0.04f)
                m(0.5f, 0.19f); l(0.5f, 0.42f); m(0.5f, 0.58f); l(0.5f, 0.81f)
                m(0.06f, 0.94f); l(0.94f, 0.94f)
            }
            Motif.DURIN_STAR -> {
                m(0.16f, 0.96f); l(0.16f, 0.46f); q(0.16f, 0.06f, 0.5f, 0.06f); q(0.84f, 0.06f, 0.84f, 0.46f); l(0.84f, 0.96f)
                star(0.5f, 0.44f, 0.2f, 0.08f, 8)
            }
            Motif.HORN -> {
                m(0.06f, 0.3f); q(0.44f, 0.94f, 0.94f, 0.4f)
                m(0.13f, 0.25f); q(0.46f, 0.76f, 0.84f, 0.24f)
                m(0.06f, 0.3f); l(0.13f, 0.25f)
                m(0.84f, 0.24f); l(0.94f, 0.4f)
                m(0.2f, 0.32f); q(0.5f, 0.06f, 0.78f, 0.28f)
            }
            Motif.POTION -> {
                m(0.4f, 0.1f); l(0.6f, 0.1f)
                m(0.44f, 0.1f); l(0.44f, 0.36f)
                m(0.56f, 0.1f); l(0.56f, 0.36f)
                circle(0.5f, 0.63f, 0.28f)
                m(0.25f, 0.65f); q(0.5f, 0.56f, 0.75f, 0.65f)
            }
            Motif.COMBO_KEYS -> {
                circle(0.17f, 0.5f, 0.13f); circle(0.5f, 0.5f, 0.13f); circle(0.83f, 0.5f, 0.13f)
                m(0.31f, 0.5f); l(0.36f, 0.5f); m(0.64f, 0.5f); l(0.69f, 0.5f)
                m(0.14f, 0.45f); l(0.18f, 0.42f); l(0.18f, 0.58f)
                m(0.46f, 0.46f); q(0.5f, 0.38f, 0.54f, 0.46f); l(0.46f, 0.58f); l(0.55f, 0.58f)
                m(0.79f, 0.43f); q(0.89f, 0.44f, 0.82f, 0.5f); q(0.89f, 0.56f, 0.79f, 0.57f)
            }
            Motif.SWORD -> {
                m(0.46f, 0.68f); l(0.46f, 0.15f); l(0.5f, 0.05f); l(0.54f, 0.15f); l(0.54f, 0.68f)
                m(0.3f, 0.68f); l(0.7f, 0.68f)
                m(0.5f, 0.68f); l(0.5f, 0.87f)
                circle(0.5f, 0.91f, 0.04f)
            }
            Motif.STAR -> star(0.5f, 0.5f, 0.44f, 0.19f, 5)
            Motif.NAZGUL_CROWN -> {
                m(0.2f, 0.9f); l(0.24f, 0.42f); l(0.32f, 0.18f); l(0.4f, 0.38f); l(0.5f, 0.1f)
                l(0.6f, 0.38f); l(0.68f, 0.18f); l(0.76f, 0.42f); l(0.8f, 0.9f)
                m(0.24f, 0.52f); q(0.5f, 0.6f, 0.76f, 0.52f)
            }
            Motif.SCROLL -> {
                m(0.25f, 0.2f); l(0.25f, 0.8f); m(0.75f, 0.2f); l(0.75f, 0.8f)
                oval(0.18f, 0.14f, 0.82f, 0.26f)
                oval(0.18f, 0.74f, 0.82f, 0.86f)
                m(0.35f, 0.38f); l(0.65f, 0.38f)
                m(0.35f, 0.5f); l(0.65f, 0.5f)
                m(0.35f, 0.62f); l(0.55f, 0.62f)
            }
        }
    }
}
