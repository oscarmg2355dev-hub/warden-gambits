package com.wardengambits.app.ui.combat

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.SemanticsPropertyKey
import androidx.compose.ui.semantics.SemanticsPropertyReceiver
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.wardengambits.app.R
import com.wardengambits.app.ui.common.KeyColors
import com.wardengambits.app.ui.common.PausableClock
import com.wardengambits.engine.combo.Input
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/** Most sparks alive at once over the whole row; the oldest are dropped first. */
const val MAX_LIVE_SPARKS = 60

/** Test tag of the row (it also carries the [LiveSparkCount] debug semantics). */
const val ACTION_BUTTONS_TAG = "action_buttons"

/** Debug semantics: how many sparks are alive over the row. */
val LiveSparkCount = SemanticsPropertyKey<Int>("LiveSparkCount")
private var SemanticsPropertyReceiver.liveSparkCount by LiveSparkCount

private val RowHeight = 72.dp
private val KeyGap = 6.dp
private val KeyCorner = 12.dp

/** Ledge under the face: 6 dp at rest, 2 dp fully pressed (the face goes down 4 dp). */
private val LedgeRest = 6.dp
private val PressDepth = 4.dp
private const val PRESS_MILLIS = 60
private const val RELEASE_DAMPING = 0.45f

/** Flash: brightness ×1.6 back to ×1. */
private const val FLASH_PEAK = 1.6f
private const val FLASH_MILLIS = 250

/** Ring born at the touch: up to 1.4× the key width (1.8× when ready). */
private const val WAVE_MILLIS = 350
private const val WAVE_SCALE = 1.4f
private const val WAVE_SCALE_READY = 1.8f

/** Sparks: 10 (18 when ready) at 120–260 dp/s, falling, gone in 450 ms. */
private const val SPARKS = 10
private const val SPARKS_READY = 18
private const val SPARK_MIN_SPEED = 120f
private const val SPARK_SPEED_RANGE = 140f
const val SPARK_LIFE_MILLIS = 450L
private const val SPARK_GRAVITY_DP = 900f

/** Ready halo: gold, alpha 0.25–0.6, one beat every 900 ms. */
private val ReadyGold = Color(0xFFF4D98A)
private const val HALO_MIN = 0.25f
private const val HALO_MAX = 0.6f
private const val HALO_BEAT_MILLIS = 900

/** Share of the colour a disabled key keeps (desaturated to 40 %). */
private const val DISABLED_SATURATION = 0.4f

/** One spark of a burst: its direction (radians), speed and how far its colour is lightened (0–1). */
data class Spark(val angle: Float, val speedDpPerSec: Float, val colorShift: Float)

/** [count] sparks in random directions at 120–260 dp/s; [random] is UI-only. */
fun sparkBurst(count: Int, random: Random): List<Spark> = List(count) {
    Spark(
        angle = random.nextFloat() * 2f * PI.toFloat(),
        speedDpPerSec = SPARK_MIN_SPEED + random.nextFloat() * SPARK_SPEED_RANGE,
        colorShift = random.nextFloat(),
    )
}

/** A spark launched from [origin] (px, in the row) with [color] at [bornMillis] of the field clock. */
private class LiveSpark(val spark: Spark, val origin: Offset, val color: Color, val bornMillis: Long)

/** Live sparks of the row and their clock, which only advances while the fight runs. */
@Stable
private class SparkField {
    var sparks: List<LiveSpark> by mutableStateOf(emptyList())
        private set
    var clock by mutableLongStateOf(0L)
        private set

    fun burst(origin: Offset, color: Color, count: Int, random: Random) {
        val added = sparks + sparkBurst(count, random).map { LiveSpark(it, origin, color, clock) }
        sparks = added.takeLast(MAX_LIVE_SPARKS)
    }

    fun advance(millis: Long) {
        clock += millis
        if (sparks.any { clock - it.bornMillis >= SPARK_LIFE_MILLIS }) {
            sparks = sparks.filter { clock - it.bornMillis < SPARK_LIFE_MILLIS }
        }
    }
}

/**
 * The four 3D combat keys (1, 2, 3 and the Action sword) filling the width, with a spark field drawn over the whole
 * row without clipping. [ready] (a combo matches the buffer) makes the Action key pulse with a gold halo; a press
 * flashes the key, sends a ring and a burst of sparks. [running] = false freezes the sparks and the halo.
 */
@Composable
fun ActionButtons(
    onPress: (Input) -> Unit,
    onExecute: () -> Unit,
    enabled: Boolean,
    ready: Boolean,
    running: Boolean,
    modifier: Modifier = Modifier,
) {
    val field = remember { SparkField() }
    val random = Random.Default
    PausableClock(field, running && field.sparks.isNotEmpty()) { field.advance(it) }
    Box(
        modifier.fillMaxWidth().height(RowHeight).graphicsLayer { clip = false }
            .semantics { liveSparkCount = field.sparks.size }.testTag(ACTION_BUTTONS_TAG),
    ) {
        Row(Modifier.matchParentSize(), horizontalArrangement = Arrangement.spacedBy(KeyGap)) {
            Input.entries.forEach { input ->
                val base = KeyColors.background(input.code)
                ActionKey(
                    base = base,
                    content = KeyColors.content(input.code),
                    enabled = enabled,
                    ready = false,
                    running = running,
                    onClick = { onPress(input) },
                    onBurst = { at, _ -> field.burst(at, sparkColor(base), SPARKS, random) },
                    modifier = Modifier.weight(1f).testTag("btn_${input.name.lowercase()}"),
                ) { color, _ -> KeyLabel(input.code.toString(), color) }
            }
            val readyNow = ready && enabled
            ActionKey(
                base = KeyColors.actionBackground,
                content = KeyColors.actionContent,
                enabled = enabled,
                ready = readyNow,
                running = running,
                onClick = onExecute,
                onBurst = { at, burstReady ->
                    if (burstReady) field.burst(at, ReadyGold, SPARKS_READY, random)
                    else field.burst(at, sparkColor(KeyColors.actionBackground), SPARKS, random)
                },
                modifier = Modifier.weight(1f).testTag("btn_execute"),
            ) { color, glow -> SwordLabel(color, glow) }
        }
        Canvas(Modifier.matchParentSize()) { drawSparks(field) }
    }
}

/**
 * One key: a rounded face over a darker ledge. [onBurst] gets the touch point (px, in the row) and whether the key
 * was ready when pressed. [label] gets the label colour and the ready glow (0 when not ready).
 */
@Composable
private fun ActionKey(
    base: Color,
    content: Color,
    enabled: Boolean,
    ready: Boolean,
    running: Boolean,
    onClick: () -> Unit,
    onBurst: (Offset, Boolean) -> Unit,
    modifier: Modifier,
    label: @Composable (Color, () -> Float) -> Unit,
) {
    val interactions = remember { MutableInteractionSource() }
    val press = remember { Animatable(0f) }
    val flash = remember { Animatable(1f) }
    val wave = remember { Animatable(1f) }
    val halo = remember { Animatable(HALO_MIN) }
    var waveOrigin by remember { mutableStateOf(Offset.Zero) }
    var waveScale by remember { mutableStateOf(WAVE_SCALE) }
    var keyOffset by remember { mutableStateOf(Offset.Zero) }
    val currentReady by rememberUpdatedState(ready)
    val currentBurst by rememberUpdatedState(onBurst)
    val waving by remember { derivedStateOf { wave.value < 1f } }

    LaunchedEffect(interactions) {
        var pressJob: Job? = null
        interactions.interactions.collect { interaction ->
            when (interaction) {
                is PressInteraction.Press -> {
                    val burstReady = currentReady
                    waveOrigin = interaction.pressPosition
                    waveScale = if (burstReady) WAVE_SCALE_READY else WAVE_SCALE
                    currentBurst(keyOffset + interaction.pressPosition, burstReady)
                    pressJob = launch { press.animateTo(1f, tween(PRESS_MILLIS, easing = LinearEasing)) }
                    launch {
                        flash.snapTo(FLASH_PEAK)
                        flash.animateTo(1f, tween(FLASH_MILLIS))
                    }
                    launch {
                        wave.snapTo(0f)
                        wave.animateTo(1f, tween(WAVE_MILLIS, easing = LinearEasing))
                    }
                }
                is PressInteraction.Release, is PressInteraction.Cancel -> {
                    val pressing = pressJob
                    launch {
                        pressing?.join()
                        press.animateTo(0f, spring(dampingRatio = RELEASE_DAMPING))
                    }
                }
                else -> Unit
            }
        }
    }

    // The halo beats only while ready and running; pausing freezes it where it is.
    LaunchedEffect(ready, running) {
        if (!ready) {
            halo.snapTo(HALO_MIN)
            return@LaunchedEffect
        }
        if (!running) return@LaunchedEffect
        // From wherever it froze: up and down, one full beat every 900 ms.
        halo.animateTo(HALO_MIN, tween(((halo.value - HALO_MIN) / (HALO_MAX - HALO_MIN) * HALO_BEAT_MILLIS / 2).toInt()))
        halo.animateTo(HALO_MAX, infiniteRepeatable(tween(HALO_BEAT_MILLIS / 2), RepeatMode.Reverse))
    }

    val readyDescription = stringResource(R.string.combat_action_ready)
    val labelColor = if (enabled) content else content.desaturated()
    Box(
        modifier
            .fillMaxHeight()
            .onPlaced { keyOffset = it.positionInParent() }
            .zIndex(if (ready || waving) 1f else 0f)
            .drawWithContent {
                drawKey(base, enabled, ready, press.value, flash.value, halo.value)
                drawContent()
                if (enabled && wave.value < 1f) drawWave(waveOrigin, wave.value, waveScale, ready)
            }
            .clickable(interactionSource = interactions, indication = null, enabled = enabled, onClick = onClick)
            .semantics { if (ready) stateDescription = readyDescription },
        contentAlignment = Alignment.Center,
    ) {
        Box(
            Modifier.graphicsLayer {
                // Centre of the face: it sits 3 dp above the key centre at rest and goes down with the press.
                translationY = if (enabled) (press.value * PressDepth.toPx()) - LedgeRest.toPx() / 2f else 0f
            },
            contentAlignment = Alignment.Center,
        ) {
            label(labelColor) { if (ready) (halo.value - HALO_MIN) / (HALO_MAX - HALO_MIN) else 0f }
        }
    }
}

@Composable
private fun KeyLabel(text: String, color: Color) {
    val shadow = if (color.luminance() > 0.5f) Color(0xA6000000) else Color(0x66FFFFFF)
    Text(
        text,
        style = TextStyle(
            color = color,
            fontSize = 32.sp,
            fontWeight = FontWeight.Black,
            shadow = Shadow(shadow, Offset(0f, 3f), 4f),
        ),
    )
}

@Composable
private fun SwordLabel(color: Color, glow: () -> Float) {
    Box(
        Modifier.size(56.dp).drawBehind {
            val g = glow()
            if (g > 0f) {
                drawCircle(
                    Brush.radialGradient(
                        listOf(ReadyGold.copy(alpha = 0.35f + 0.5f * g), ReadyGold.copy(alpha = 0f)),
                        radius = size.minDimension / 2f,
                    ),
                )
            }
        },
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            painterResource(R.drawable.ic_sword),
            contentDescription = stringResource(R.string.key_action),
            tint = color,
            modifier = Modifier.size(38.dp).graphicsLayer {
                val g = glow()
                scaleX = 1f + 0.08f * g
                scaleY = 1f + 0.08f * g
            },
        )
    }
}

/** The key body: halo (ready), ledge, gradient face, pill gloss and bevel; flat and greyed when disabled. */
private fun DrawScope.drawKey(base: Color, enabled: Boolean, ready: Boolean, press: Float, flash: Float, halo: Float) {
    val corner = CornerRadius(KeyCorner.toPx())
    if (!enabled) {
        val flat = base.desaturated()
        drawRoundRect(flat, cornerRadius = corner)
        drawRoundRect(flat.scaled(0.75f), cornerRadius = corner, style = Stroke(1.dp.toPx()))
        return
    }
    val ledge = LedgeRest.toPx()
    val faceTop = press * PressDepth.toPx()
    val faceHeight = size.height - ledge
    if (ready) {
        for (ring in 1..4) {
            val grow = ring * 3.dp.toPx()
            drawRoundRect(
                ReadyGold.copy(alpha = halo * (1f - ring / 5f)),
                topLeft = Offset(-grow, -grow),
                size = Size(size.width + grow * 2, size.height + grow * 2),
                cornerRadius = CornerRadius(corner.x + grow),
                style = Stroke(3.dp.toPx()),
            )
        }
    }
    // Ledge: the base at 55 % brightness, showing below the face (6 dp at rest, 2 dp pressed).
    drawRoundRect(
        base.scaled(0.55f),
        topLeft = Offset(0f, faceTop + faceHeight / 2f),
        size = Size(size.width, size.height - faceTop - faceHeight / 2f),
        cornerRadius = corner,
    )
    // Face: base at 115 % on top to 90 % at the bottom, times the flash.
    drawRoundRect(
        Brush.verticalGradient(
            listOf(base.scaled(1.15f * flash), base.scaled(0.9f * flash)),
            startY = faceTop,
            endY = faceTop + faceHeight,
        ),
        topLeft = Offset(0f, faceTop),
        size = Size(size.width, faceHeight),
        cornerRadius = corner,
    )
    // Gloss: a white pill over the top 40 % of the face.
    val inset = 6.dp.toPx()
    val glossHeight = faceHeight * 0.4f
    drawRoundRect(
        Brush.verticalGradient(
            listOf(Color.White.copy(alpha = 0.35f), Color.White.copy(alpha = 0.04f)),
            startY = faceTop + 3.dp.toPx(),
            endY = faceTop + 3.dp.toPx() + glossHeight,
        ),
        topLeft = Offset(inset, faceTop + 3.dp.toPx()),
        size = Size(size.width - inset * 2, glossHeight),
        cornerRadius = CornerRadius(glossHeight / 2f),
    )
    // Bevel: 1 dp, lighter on top and darker below.
    val stroke = 1.dp.toPx()
    drawRoundRect(
        Brush.verticalGradient(
            listOf(base.scaled(1.6f), base.scaled(0.6f)),
            startY = faceTop,
            endY = faceTop + faceHeight,
        ),
        topLeft = Offset(stroke / 2f, faceTop + stroke / 2f),
        size = Size(size.width - stroke, faceHeight - stroke),
        cornerRadius = CornerRadius(corner.x - stroke / 2f),
        style = Stroke(stroke),
    )
}

/** The ring of a press: born at [origin], growing to [scale] × the key width while it fades. */
private fun DrawScope.drawWave(origin: Offset, progress: Float, scale: Float, ready: Boolean) {
    val radius = size.width * scale / 2f * progress
    val fade = 1f - progress
    val color = if (ready) ReadyGold else Color.White
    drawCircle(color.copy(alpha = 0.25f * fade), radius = radius, center = origin, style = Stroke(10.dp.toPx() * fade + 1f))
    drawCircle(color.copy(alpha = 0.85f * fade), radius = radius, center = origin, style = Stroke(3.dp.toPx() * fade + 1f))
}

/** Every live spark: thrown from its origin, pulled down by gravity, shrinking and fading over its life. */
private fun DrawScope.drawSparks(field: SparkField) {
    val now = field.clock
    val dp = 1.dp.toPx()
    field.sparks.forEach { live ->
        val age = (now - live.bornMillis).coerceAtLeast(0L)
        val life = age.toFloat() / SPARK_LIFE_MILLIS
        if (life >= 1f) return@forEach
        val t = age / 1000f
        val speed = live.spark.speedDpPerSec * dp
        val vx = cos(live.spark.angle) * speed
        val vy = sin(live.spark.angle) * speed
        val gravity = SPARK_GRAVITY_DP * dp
        val position = live.origin + Offset(vx * t, vy * t + gravity * t * t / 2f)
        val trail = live.origin + Offset(vx * (t - 0.03f).coerceAtLeast(0f), vy * (t - 0.03f).coerceAtLeast(0f) +
            gravity * (t - 0.03f).coerceAtLeast(0f).let { it * it } / 2f)
        val fade = 1f - life
        val color = lerp(live.color, Color.White, live.spark.colorShift * 0.6f)
        val radius = (1.2f + 2.3f * fade) * dp
        drawLine(color.copy(alpha = 0.55f * fade), trail, position, strokeWidth = radius)
        drawCircle(color.copy(alpha = 0.3f * fade), radius = radius * 2.4f, center = position)
        drawCircle(color.copy(alpha = fade), radius = radius, center = position)
    }
}

/** The base colour lightened for its sparks. */
private fun sparkColor(base: Color): Color = lerp(base, Color.White, 0.45f)

/** This colour with its RGB multiplied by [factor] (clamped). */
private fun Color.scaled(factor: Float): Color = Color(
    red = (red * factor).coerceIn(0f, 1f),
    green = (green * factor).coerceIn(0f, 1f),
    blue = (blue * factor).coerceIn(0f, 1f),
    alpha = alpha,
)

/** This colour keeping 40 % of its saturation (and slightly dimmed), for disabled keys. */
private fun Color.desaturated(): Color {
    val grey = red * 0.299f + green * 0.587f + blue * 0.114f
    return lerp(Color(grey, grey, grey, alpha), this, DISABLED_SATURATION).scaled(0.8f)
}
