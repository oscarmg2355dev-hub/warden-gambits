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
 * App-wide background: [base] colour plus a faint, static, WhatsApp-wallpaper-like tile of fantasy RPG objects drawn
 * as thin gold line-art. The tile is rendered once into an [ImageBitmap] and repeated with an [ImageShader].
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
    val TILE = 240.dp
    private val MOTIF = 36.dp
    private val STROKE = 1.2.dp
    private val LineColor = WardenColors.Primary.copy(alpha = 0.06f)

    enum class Motif { SWORD, SHIELD, POTION, SCROLL, RING, HELMET, AXE, BOW, RUNE_CIRCLE, LEAF, STAR, KEY }

    /** Centre (fraction of the tile), rotation in degrees and scale of each motif: a jittered 4 × 3 grid. */
    private data class Placement(val motif: Motif, val x: Float, val y: Float, val degrees: Float, val scale: Float = 1f)

    private val placements = listOf(
        Placement(Motif.SWORD, 0.13f, 0.17f, -18f),
        Placement(Motif.POTION, 0.38f, 0.14f, 10f, 0.9f),
        Placement(Motif.STAR, 0.63f, 0.19f, 12f, 0.7f),
        Placement(Motif.SHIELD, 0.87f, 0.15f, -8f),
        Placement(Motif.RING, 0.12f, 0.50f, 8f, 0.85f),
        Placement(Motif.SCROLL, 0.36f, 0.52f, -14f),
        Placement(Motif.AXE, 0.62f, 0.48f, 20f),
        Placement(Motif.LEAF, 0.88f, 0.52f, -25f, 0.85f),
        Placement(Motif.HELMET, 0.14f, 0.84f, 6f),
        Placement(Motif.KEY, 0.39f, 0.86f, -10f, 0.9f),
        Placement(Motif.RUNE_CIRCLE, 0.63f, 0.82f, 0f, 0.95f),
        Placement(Motif.BOW, 0.87f, 0.85f, 15f),
    )

    val motifCount: Int get() = placements.map { it.motif }.distinct().size

    fun drawTile(scope: DrawScope) = with(scope) {
        val stroke = Stroke(width = STROKE.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
        for (p in placements) {
            val s = MOTIF.toPx() * p.scale
            val cx = p.x * size.width
            val cy = p.y * size.height
            rotate(p.degrees, pivot = Offset(cx, cy)) {
                translate(cx - s / 2f, cy - s / 2f) { drawPath(motifPath(p.motif, s), LineColor, style = stroke) }
            }
        }
    }

    /** Line-art outline of [motif] inside an [s] × [s] box; coordinates below are fractions of the box. */
    private fun motifPath(motif: Motif, s: Float): Path = Path().apply {
        fun m(x: Float, y: Float) = moveTo(x * s, y * s)
        fun l(x: Float, y: Float) = lineTo(x * s, y * s)
        fun q(x1: Float, y1: Float, x2: Float, y2: Float) = quadraticTo(x1 * s, y1 * s, x2 * s, y2 * s)
        fun circle(x: Float, y: Float, r: Float) = addOval(Rect(Offset(x * s, y * s), r * s))
        when (motif) {
            Motif.SWORD -> {
                m(0.46f, 0.68f); l(0.46f, 0.15f); l(0.5f, 0.05f); l(0.54f, 0.15f); l(0.54f, 0.68f)
                m(0.3f, 0.68f); l(0.7f, 0.68f)
                m(0.5f, 0.68f); l(0.5f, 0.87f)
                circle(0.5f, 0.91f, 0.04f)
            }
            Motif.SHIELD -> {
                m(0.2f, 0.15f); l(0.8f, 0.15f); l(0.8f, 0.5f); q(0.8f, 0.8f, 0.5f, 0.95f); q(0.2f, 0.8f, 0.2f, 0.5f); close()
                m(0.5f, 0.15f); l(0.5f, 0.95f)
                m(0.2f, 0.45f); l(0.8f, 0.45f)
            }
            Motif.POTION -> {
                m(0.4f, 0.1f); l(0.6f, 0.1f)
                m(0.44f, 0.1f); l(0.44f, 0.36f)
                m(0.56f, 0.1f); l(0.56f, 0.36f)
                circle(0.5f, 0.63f, 0.28f)
                m(0.25f, 0.65f); q(0.5f, 0.56f, 0.75f, 0.65f)
            }
            Motif.SCROLL -> {
                m(0.25f, 0.2f); l(0.25f, 0.8f); m(0.75f, 0.2f); l(0.75f, 0.8f)
                addOval(Rect(0.18f * s, 0.14f * s, 0.82f * s, 0.26f * s))
                addOval(Rect(0.18f * s, 0.74f * s, 0.82f * s, 0.86f * s))
                m(0.35f, 0.38f); l(0.65f, 0.38f)
                m(0.35f, 0.5f); l(0.65f, 0.5f)
                m(0.35f, 0.62f); l(0.55f, 0.62f)
            }
            Motif.RING -> {
                circle(0.5f, 0.62f, 0.3f)
                circle(0.5f, 0.62f, 0.24f)
                m(0.5f, 0.1f); l(0.61f, 0.22f); l(0.5f, 0.32f); l(0.39f, 0.22f); close()
            }
            Motif.HELMET -> {
                m(0.2f, 0.72f); l(0.2f, 0.45f)
                cubicTo(0.2f * s, 0.12f * s, 0.8f * s, 0.12f * s, 0.8f * s, 0.45f * s)
                l(0.8f, 0.72f); l(0.6f, 0.72f); l(0.6f, 0.5f); l(0.4f, 0.5f); l(0.4f, 0.72f); close()
                m(0.5f, 0.5f); l(0.5f, 0.86f)
                m(0.5f, 0.2f); l(0.5f, 0.06f)
            }
            Motif.AXE -> {
                m(0.45f, 0.06f); l(0.45f, 0.95f)
                m(0.45f, 0.16f); l(0.7f, 0.08f); q(0.94f, 0.3f, 0.7f, 0.52f); l(0.45f, 0.44f)
                m(0.45f, 0.2f); l(0.3f, 0.26f); l(0.3f, 0.38f); l(0.45f, 0.4f)
            }
            Motif.BOW -> {
                m(0.35f, 0.08f); q(0.95f, 0.5f, 0.35f, 0.92f)
                m(0.35f, 0.08f); l(0.35f, 0.92f)
                m(0.08f, 0.5f); l(0.82f, 0.5f)
                m(0.74f, 0.44f); l(0.82f, 0.5f); l(0.74f, 0.56f)
                m(0.08f, 0.5f); l(0.04f, 0.44f); m(0.08f, 0.5f); l(0.04f, 0.56f)
            }
            Motif.RUNE_CIRCLE -> {
                circle(0.5f, 0.5f, 0.42f)
                circle(0.5f, 0.5f, 0.3f)
                m(0.5f, 0.28f); l(0.5f, 0.72f)
                m(0.5f, 0.4f); l(0.62f, 0.32f)
                m(0.5f, 0.56f); l(0.38f, 0.48f)
                m(0.5f, 0.08f); l(0.5f, 0.2f); m(0.5f, 0.8f); l(0.5f, 0.92f)
                m(0.08f, 0.5f); l(0.2f, 0.5f); m(0.8f, 0.5f); l(0.92f, 0.5f)
            }
            Motif.LEAF -> {
                m(0.5f, 0.06f); q(0.92f, 0.45f, 0.5f, 0.84f); q(0.08f, 0.45f, 0.5f, 0.06f); close()
                m(0.5f, 0.14f); l(0.5f, 0.96f)
                m(0.5f, 0.38f); l(0.66f, 0.28f)
                m(0.5f, 0.54f); l(0.34f, 0.44f)
                m(0.5f, 0.66f); l(0.64f, 0.58f)
            }
            Motif.STAR -> {
                for (i in 0 until 10) {
                    val r = if (i % 2 == 0) 0.44f else 0.19f
                    val a = -PI / 2 + i * PI / 5
                    val x = 0.5f + r * cos(a).toFloat()
                    val y = 0.5f + r * sin(a).toFloat()
                    if (i == 0) m(x, y) else l(x, y)
                }
                close()
            }
            Motif.KEY -> {
                circle(0.3f, 0.3f, 0.16f)
                circle(0.3f, 0.3f, 0.06f)
                m(0.41f, 0.41f); l(0.88f, 0.88f)
                m(0.7f, 0.7f); l(0.78f, 0.62f)
                m(0.8f, 0.8f); l(0.88f, 0.72f)
            }
        }
    }
}
