package com.wardengambits.app.ui.character

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.hypot
import kotlin.math.max

/** Palette of the parchment character sheet (mockup style B, "códice élfico"). */
internal object ParchmentColors {
    val Ink = Color(0xFF3B2A17)
    val Rubric = Color(0xFF7A2A1A)
    val Rule = Color(0x736E4619)
    val Gold = Color(0xFFF6DC8A)
    val Leader = Color(0x735A3714)
    val ButtonTop = Color(0xFF7A2A1A)
    val ButtonBottom = Color(0xFF4E170E)
    val ButtonShadow = Color(0xFF2A0B06)
}

/** Parchment sheets never grow wider than this, so they don't stretch on tablets. */
internal val SHEET_MAX_WIDTH = 520.dp

private val SheetShape = RoundedCornerShape(6.dp)
private val Vignette = Color(0x8C5A3714)
private val OuterLine = Color(0x8C6E4619)
private val InnerLine = Color(0x596E4619)

/**
 * Parchment card: radial gradient #efe0c2 → #dcc296 → #a98754 centred at 35 % of the height, a dark inner
 * vignette along the edges and two thin border lines inset 6 dp and 10 dp.
 */
internal fun Modifier.parchment(): Modifier = this
    .shadow(6.dp, SheetShape)
    .drawBehind {
        val center = Offset(size.width / 2f, size.height * 0.35f)
        val radius = hypot(max(center.x, size.width - center.x), max(center.y, size.height - center.y))
        drawRect(
            Brush.radialGradient(
                0f to Color(0xFFEFE0C2),
                0.55f to Color(0xFFDCC296),
                1f to Color(0xFFA98754),
                center = center,
                radius = radius,
            ),
        )
        drawVignette(24.dp.toPx())
    }
    .drawWithContent {
        drawContent()
        inset(6.dp.toPx(), 2.dp.toPx(), 4.dp.toPx(), OuterLine)
        inset(10.dp.toPx(), 1.dp.toPx(), 3.dp.toPx(), InnerLine)
    }

private fun DrawScope.drawVignette(depth: Float) {
    val w = size.width
    val h = size.height
    drawRect(Brush.horizontalGradient(listOf(Vignette, Color.Transparent), 0f, depth), size = Size(depth, h))
    drawRect(
        Brush.horizontalGradient(listOf(Color.Transparent, Vignette), w - depth, w),
        topLeft = Offset(w - depth, 0f),
        size = Size(depth, h),
    )
    drawRect(Brush.verticalGradient(listOf(Vignette, Color.Transparent), 0f, depth), size = Size(w, depth))
    drawRect(
        Brush.verticalGradient(listOf(Color.Transparent, Vignette), h - depth, h),
        topLeft = Offset(0f, h - depth),
        size = Size(w, depth),
    )
}

private fun DrawScope.inset(by: Float, stroke: Float, corner: Float, color: Color) {
    val half = stroke / 2f
    drawRoundRect(
        color = color,
        topLeft = Offset(by + half, by + half),
        size = Size(size.width - 2 * (by + half), size.height - 2 * (by + half)),
        cornerRadius = CornerRadius(corner),
        style = Stroke(stroke),
    )
}

/** Colours of a wax seal: highlight (at 35 %/30 %), body and rim. */
internal data class SealColors(val light: Color, val mid: Color, val dark: Color) {
    companion object {
        val Red = SealColors(Color(0xFFD2443A), Color(0xFF8F1B17), Color(0xFF5E0D0B))
        val Green = SealColors(Color(0xFF5FAE5A), Color(0xFF2E6B2A), Color(0xFF16401A))
    }
}

/** A round wax seal rotated −8°, with a darker inner ring; [content] is centred on it. */
@Composable
internal fun WaxSeal(
    colors: SealColors,
    size: Dp,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    Box(
        modifier
            .rotate(-8f)
            .size(size)
            .shadow(4.dp, CircleShape)
            .drawBehind {
                drawCircle(
                    Brush.radialGradient(
                        0f to colors.light,
                        0.6f to colors.mid,
                        1f to colors.dark,
                        center = Offset(this.size.width * 0.35f, this.size.height * 0.3f),
                        radius = this.size.minDimension * 0.85f,
                    ),
                )
                val ring = 4.dp.toPx()
                drawCircle(Color(0x2E000000), radius = this.size.minDimension / 2f - ring / 2f, style = Stroke(ring))
            },
        contentAlignment = Alignment.Center,
        content = content,
    )
}

/**
 * Scrollable page area that centres its [content] horizontally (at most [SHEET_MAX_WIDTH] wide) and vertically
 * when it is shorter than the available space; taller content scrolls from the top.
 */
@Composable
internal fun CenteredSheetPage(modifier: Modifier = Modifier, content: @Composable BoxScope.() -> Unit) {
    BoxWithConstraints(modifier) {
        Box(
            Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).heightIn(min = maxHeight)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            contentAlignment = Alignment.Center,
        ) {
            Box(Modifier.widthIn(max = SHEET_MAX_WIDTH).fillMaxWidth(), content = content)
        }
    }
}
