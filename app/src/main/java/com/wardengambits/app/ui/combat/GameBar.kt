package com.wardengambits.app.ui.combat

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.graphics.Shadow
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wardengambits.app.ui.theme.WardenColors

enum class GameBarKind { HP, POWER }

/** Colour of the trailing ghost segment and whether the fill moves before it (damage) or after it (heal). */
data class BarTrail(val ghostColor: Color, val fillFirst: Boolean)

private val DamageGhost = Color(0xFFF3E0A0)
private val HealGhostHp = Color(0xFF7DFF9A)
private val HealGhostPower = Color(0xFF8FC4FF)

internal fun barTrail(previous: Float, current: Float, kind: GameBarKind = GameBarKind.HP): BarTrail =
    if (current < previous) BarTrail(DamageGhost, fillFirst = true)
    else BarTrail(if (kind == GameBarKind.POWER) HealGhostPower else HealGhostHp, fillFirst = false)

/** Whether a change of the bar should flash: any HP change; for power only changes of at least 5 % of the maximum. */
internal fun barFlashes(previous: Int, current: Int, max: Int, kind: GameBarKind): Boolean =
    when (kind) {
        GameBarKind.HP -> previous != current
        GameBarKind.POWER -> kotlin.math.abs(current - previous) * 100 >= max * 5
    }

/** Largest font size in [minSp, maxSp] whose text width ([widthAt] in px for a size in sp) fits [availablePx]. */
internal fun fitFontSize(maxSp: Float, minSp: Float, availablePx: Float, widthAt: (Float) -> Float): Float {
    var size = maxSp
    while (size > minSp && widthAt(size) > availablePx) size = (size - 0.5f).coerceAtLeast(minSp)
    return size
}

private val BAR_HEIGHT = 22.dp
private const val MAX_FONT_SP = 13f
private const val MIN_FONT_SP = 9f
private val TextShadow = Shadow(Color.Black, Offset(0f, 1f), 3f)

private const val FILL_MS = 250
private const val GHOST_DELAY_MS = 350
private const val GHOST_MS = 900
private const val HEAL_FILL_DELAY_MS = 300
private const val FLASH_MS = 350
private const val HALO_MS = 700

private val HpColors = listOf(Color(0xFFFF8A7A), Color(0xFFD8392B), Color(0xFF8E1C13))
private val PowerColors = listOf(Color(0xFF8FC4FF), Color(0xFF2F74D0), Color(0xFF163F80))
private val ChannelColors = listOf(Color(0xFF1A1410), Color(0xFF3A2F27))
private val HaloColorHp = Color(0xFF7DFF9A)
private val HaloColorPower = Color(0xFF8FC4FF)

/**
 * 3D-looking bar with a "value/max" label. When [value] changes the fill and a ghost trail animate;
 * with [running] = false the animations stop and resume when it becomes true again.
 */
@Composable
fun GameBar(value: Int, max: Int, kind: GameBarKind, running: Boolean, modifier: Modifier = Modifier) {
    val target = if (max <= 0) 0f else value.coerceIn(0, max).toFloat() / max
    val fill = remember { Animatable(target) }
    val ghost = remember { Animatable(target) }
    val flash = remember { Animatable(1f) }
    val halo = remember { Animatable(0f) }
    val lastTarget = remember { mutableStateOf(target) }
    val lastValue = remember { mutableStateOf(value) }
    val trail = remember { mutableStateOf(barTrail(target, target, kind)) }

    LaunchedEffect(target, running) {
        if (!running) return@LaunchedEffect // leaving the effect cancels (stops) the animations
        val changed = target != lastTarget.value
        val flashes = changed && barFlashes(lastValue.value, value, max, kind)
        lastValue.value = value
        if (changed) {
            trail.value = barTrail(lastTarget.value, target, kind)
            lastTarget.value = target
        }
        val t = trail.value
        val healing = !t.fillFirst
        val ghostAnim = tween<Float>(GHOST_MS, if (healing) 0 else GHOST_DELAY_MS, LinearEasing)
        val fillAnim = tween<Float>(FILL_MS, if (healing) HEAL_FILL_DELAY_MS else 0, LinearEasing)
        if (flashes) {
            flash.snapTo(2f)
            if (healing) halo.snapTo(1f)
        }
        // On damage the ghost stays at the old value and drains late; on heal it rises first.
        launch { fill.animateTo(target, fillAnim) }
        launch { ghost.animateTo(target, ghostAnim) }
        launch { flash.animateTo(1f, tween(FLASH_MS, easing = LinearEasing)) }
        launch { halo.animateTo(0f, tween(HALO_MS, easing = LinearEasing)) }
    }

    val colors = if (kind == GameBarKind.HP) HpColors else PowerColors
    val measurer = rememberTextMeasurer()
    val density = LocalDensity.current
    val label = "$value/$max"
    BoxWithConstraints(
        modifier.fillMaxWidth().height(BAR_HEIGHT).testTag("gameBar"),
        contentAlignment = Alignment.Center,
    ) {
        val availablePx = with(density) { (maxWidth - 8.dp).toPx() }
        val fontSp = remember(label, availablePx, density) {
            fitFontSize(MAX_FONT_SP, MIN_FONT_SP, availablePx) { sp ->
                measurer.measure(label, TextStyle(fontSize = sp.sp, fontWeight = FontWeight.Bold), maxLines = 1)
                    .size.width.toFloat()
            }
        }
        Canvas(Modifier.fillMaxSize()) {
            val corner = CornerRadius(size.height / 2)
            drawRoundRect(Brush.verticalGradient(ChannelColors), size = size, cornerRadius = corner)
            fun segment(fraction: Float, brush: Brush) {
                val w = size.width * fraction.coerceIn(0f, 1f)
                if (w > 0f) drawRoundRect(brush, size = Size(w, size.height), cornerRadius = corner)
            }
            segment(ghost.value, Brush.verticalGradient(listOf(trail.value.ghostColor, trail.value.ghostColor)))
            segment(fill.value, Brush.verticalGradient(colors))
            val fw = size.width * fill.value.coerceIn(0f, 1f)
            if (fw > 0f) {
                // Gloss: white 45 % on the top 35 %, doubled right after a change.
                val alpha = (0.45f * flash.value).coerceAtMost(1f)
                drawRoundRect(
                    Color.White.copy(alpha = alpha),
                    topLeft = Offset(0f, 0f),
                    size = Size(fw, size.height * 0.35f),
                    cornerRadius = CornerRadius(size.height * 0.175f),
                )
            }
            if (halo.value > 0f) {
                // Inset half a stroke so the halo is drawn fully inside the bar.
                val stroke = 3.dp.toPx()
                val haloColor = if (kind == GameBarKind.POWER) HaloColorPower else HaloColorHp
                drawRoundRect(
                    haloColor.copy(alpha = halo.value * 0.8f),
                    topLeft = Offset(stroke / 2, stroke / 2),
                    size = Size(size.width - stroke, size.height - stroke),
                    cornerRadius = CornerRadius((size.height - stroke) / 2),
                    style = Stroke(width = stroke),
                )
            }
        }
        Text(
            label,
            style = TextStyle(
                fontSize = fontSp.sp,
                fontWeight = FontWeight.Bold,
                shadow = TextShadow,
            ),
            color = WardenColors.Text,
            maxLines = 1,
            softWrap = false,
            overflow = TextOverflow.Clip,
        )
    }
}
