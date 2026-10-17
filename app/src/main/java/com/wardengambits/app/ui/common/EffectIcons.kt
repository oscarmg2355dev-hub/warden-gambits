package com.wardengambits.app.ui.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.Snapshot
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.addOutline
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.SemanticsPropertyKey
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wardengambits.app.ui.theme.BadgeColors
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/** Height of every medallion, and so of one row of effect icons. */
private val MedallionHeight = 26.dp

/** Height of one row of effect icons. */
internal val EffectRowHeight: Dp = MedallionHeight

private val BadgeGap = 3.dp

/** Outer duration ring, then the metal rim; the gem fills the rest. */
private val RingWidth = 2.dp
private val RimWidth = 1.5.dp
private val GlintWidth = 1.5.dp

/** Corner of a pill medallion and the room between its text and its sides (ring + rim + air). */
private val PillCorner = 9.dp
private val PillPadH = 7.dp

private val BadgeStyle = TextStyle(
    fontSize = 11.sp,
    fontWeight = FontWeight.Bold,
    lineHeight = 13.sp,
    lineHeightStyle = LineHeightStyle(LineHeightStyle.Alignment.Center, LineHeightStyle.Trim.Both),
)
private val TextOutline = Color(0xE6000000)
private val TextOutlineWidth = 2.5.dp
private val RingTrack = Color(0x66000000)

/** Safety margin added to every measured badge so rendering differences never cause an unbudgeted wrap. */
private val BadgeMargin = 2.dp

// Animation timings (ms) and sizes, spec §4.
private const val POP_MS = 300L
private const val POP_PEAK_MS = 180L
private const val POP_PEAK = 1.2f
private const val POP_FLASH = 0.85f
private const val BEAT_MS = 200L
private const val BEAT_PEAK = 1.12f
private const val LOW_FRACTION = 0.25f
private const val BLINK_MS = 700L
private const val BLINK_MIN = 0.6f
private const val STAR_TURN_MS = 4_000L
private const val SHINE_EVERY_MS = 3_000L
private const val SHINE_MS = 700L
private const val SHIELD_GLINT_MS = 2_400L
private const val SHIELD_GLINT_LENGTH = 0.16f
private const val DROP_PULSE_MS = 1_200L
private const val COUNT_PULSE_MS = 250L
private const val COUNT_PULSE_PEAK = 1.15f

/** Largest side of the big Daze star over the player's card. */
private val DazeStarMaxSize = 90.dp

/** A badge born while paused starts this old: already popped and past its first shine. */
private const val SETTLED_MS = SHINE_MS

/** Width of a drawn badge shape; null for a pill, whose width follows its text. */
internal fun shapeWidth(shape: BadgeShape): Dp? = when (shape) {
    BadgeShape.PILL -> null
    BadgeShape.SHIELD -> 28.dp
    BadgeShape.DIAMOND -> 34.dp
    BadgeShape.DROP -> 26.dp
    BadgeShape.STAR -> 30.dp
}

/** Flat top, straight sides down to the middle, then curving into a point. */
private val ShieldOutline = GenericShape { size: Size, _ ->
    val w = size.width
    val h = size.height
    moveTo(0f, 0f)
    lineTo(w, 0f)
    lineTo(w, h * 0.45f)
    cubicTo(w, h * 0.75f, w * 0.72f, h * 0.9f, w / 2f, h)
    cubicTo(w * 0.28f, h * 0.9f, 0f, h * 0.75f, 0f, h * 0.45f)
    close()
}

private val DiamondOutline = GenericShape { size: Size, _ ->
    moveTo(size.width / 2f, 0f)
    lineTo(size.width, size.height / 2f)
    lineTo(size.width / 2f, size.height)
    lineTo(0f, size.height / 2f)
    close()
}

/** Teardrop: a point on top and a round bottom. */
private val DropOutline = GenericShape { size: Size, _ ->
    val w = size.width
    val h = size.height
    val cx = w / 2f
    moveTo(cx, 0f)
    cubicTo(cx + w * 0.15f, h * 0.25f, w, h * 0.42f, w, h * 0.62f)
    cubicTo(w, h * 0.88f, w * 0.78f, h, cx, h)
    cubicTo(w * 0.22f, h, 0f, h * 0.88f, 0f, h * 0.62f)
    cubicTo(0f, h * 0.42f, cx - w * 0.15f, h * 0.25f, cx, 0f)
    close()
}

/** Five-pointed star, nudged down so it sits centred in its box. */
private val StarOutline = GenericShape { size: Size, _ ->
    val outer = minOf(size.width, size.height) / 2f
    val inner = outer * 0.55f
    val cx = size.width / 2f
    val cy = size.height / 2f + outer * 0.095f
    (0 until 10).forEach { i ->
        val r = if (i % 2 == 0) outer else inner
        val a = Math.toRadians(-90.0 + i * 36.0)
        val x = cx + r * cos(a).toFloat()
        val y = cy + r * sin(a).toFloat()
        if (i == 0) moveTo(x, y) else lineTo(x, y)
    }
    close()
}

/** Silhouette of [shape] for a box inset by [inset] px (a pill's corner shrinks with it). */
private fun outline(shape: BadgeShape, inset: Float, density: Density): Shape = when (shape) {
    BadgeShape.PILL -> RoundedCornerShape((with(density) { PillCorner.toPx() } - inset).coerceAtLeast(0f))
    BadgeShape.SHIELD -> ShieldOutline
    BadgeShape.DIAMOND -> DiamondOutline
    BadgeShape.DROP -> DropOutline
    BadgeShape.STAR -> StarOutline
}

/** The outline of [shape] in a box of [size] shrunk by [inset] px on every side, as a path (clockwise from its top). */
private fun insetPath(shape: BadgeShape, size: Size, inset: Float, density: Density): Path {
    val inner = Size((size.width - 2 * inset).coerceAtLeast(0f), (size.height - 2 * inset).coerceAtLeast(0f))
    return Path().apply {
        addOutline(outline(shape, inset, density).createOutline(inner, LayoutDirection.Ltr, density))
        translate(Offset(inset, inset))
    }
}

/** Where the code sits inside each shape (its widest part). */
private fun textAlignment(shape: BadgeShape): Alignment = when (shape) {
    BadgeShape.PILL, BadgeShape.DIAMOND -> Alignment.Center
    BadgeShape.SHIELD -> BiasAlignment(0f, -0.3f)
    BadgeShape.DROP -> BiasAlignment(0f, 0.3f)
    BadgeShape.STAR -> BiasAlignment(0f, 0.12f)
}

/** Length of the duration ring left for [fraction] of a ring [total] long; none for a summary (no duration). */
internal fun ringLength(total: Float, fraction: Float): Float = if (fraction < 0f) 0f else total * fraction.coerceIn(0f, 1f)

/** Scale of a medallion [elapsed] ms after appearing: 0 → 1.2 (eased out) → 1 in 300 ms. */
internal fun popScale(elapsed: Long): Float = when {
    elapsed <= 0L -> 0f
    elapsed < POP_PEAK_MS -> POP_PEAK * (1f - (1f - elapsed.toFloat() / POP_PEAK_MS).let { it * it })
    elapsed < POP_MS -> POP_PEAK - (POP_PEAK - 1f) * (elapsed - POP_PEAK_MS).toFloat() / (POP_MS - POP_PEAK_MS)
    else -> 1f
}

/** White flash over the gem while it pops. */
private fun popFlash(elapsed: Long): Float = if (elapsed in 0L until POP_MS) POP_FLASH * (1f - elapsed.toFloat() / POP_MS) else 0f

/** Scale of the heartbeat [sinceBeat] ms after the duration went down: 1 → 1.12 → 1 in 200 ms. */
internal fun beatScale(sinceBeat: Long): Float = pulseScale(sinceBeat, BEAT_MS, BEAT_PEAK)

/** Scale of the Daze countdown [sinceChange] ms after its number changed: 1 → 1.15 → 1 in 250 ms. */
internal fun countdownPulseScale(sinceChange: Long): Float = pulseScale(sinceChange, COUNT_PULSE_MS, COUNT_PULSE_PEAK)

/** A linear 1 → [peak] → 1 pulse lasting [durationMs], [since] ms after it started. */
private fun pulseScale(since: Long, durationMs: Long, peak: Float): Float {
    if (since !in 0L until durationMs) return 1f
    val half = durationMs / 2
    val t = if (since < half) since.toFloat() / half else (durationMs - since).toFloat() / half
    return 1f + (peak - 1f) * t
}

/** Opacity at [now]: a soft blink between 0.6 and 1 once [fraction] ≤ 0.25; steady otherwise (and for summaries). */
internal fun blinkAlpha(now: Long, fraction: Float): Float {
    if (fraction < 0f || fraction > LOW_FRACTION) return 1f
    val phase = Math.floorMod(now, BLINK_MS).toFloat() / BLINK_MS
    return (1f + BLINK_MIN) / 2f + (1f - BLINK_MIN) / 2f * cos(2f * PI.toFloat() * phase)
}

/** Angle (degrees) of the Dazed star at [now]: a full turn every 4 s. */
internal fun starRotation(now: Long): Float = Math.floorMod(now, STAR_TURN_MS) * 360f / STAR_TURN_MS

/** Progress (0..1) of the white sweep [elapsed] ms after appearing (on appearing, then every 3 s); negative between sweeps. */
internal fun shineProgress(elapsed: Long): Float {
    if (elapsed < 0L) return -1f
    val phase = elapsed % SHINE_EVERY_MS
    return if (phase < SHINE_MS) phase.toFloat() / SHINE_MS else -1f
}

/** What a medallion's animations do to it at a moment: exposed in semantics for tests. */
internal data class BadgeMotion(val scale: Float, val rotation: Float, val alpha: Float)

/** Test-only semantics: the current [BadgeMotion] of an animated medallion. */
internal val BadgeMotionKey = SemanticsPropertyKey<() -> BadgeMotion>("BadgeMotion")

/** The badges' animation clock: advances with the frame clock only while the fight runs. */
@Stable
private class BadgeClock {
    var now by mutableLongStateOf(0L)
        private set

    /** The time, without making the caller depend on it (for composition-time reads). */
    fun peek(): Long = Snapshot.withoutReadObservation { now }

    fun advance(millis: Long) {
        now += millis
    }
}

/** Per-medallion animation state: when it appeared and when its duration last went down. */
@Stable
private class MedallionMotion(private val clock: BadgeClock, val bornAt: Long, private var lastFraction: Float) {
    var beatAt by mutableLongStateOf(NEVER)
        private set

    val now: Long get() = clock.now

    fun observe(fraction: Float) {
        if (fraction >= 0f && fraction < lastFraction) beatAt = clock.peek()
        lastFraction = fraction
    }

    fun at(badge: EffectBadgeUi): BadgeMotion {
        val now = now
        return BadgeMotion(
            scale = popScale(now - bornAt) * beatScale(now - beatAt),
            rotation = if (badge.shape == BadgeShape.STAR) starRotation(now) else 0f,
            alpha = blinkAlpha(now, badge.remainingFraction),
        )
    }

    companion object {
        const val NEVER = Long.MIN_VALUE / 4
    }
}

/**
 * How many badges of [widths] fit in [lines] rows of [maxWidth] (greedy wrapping, [gap] between badges).
 * When not all fit, room is left for the "+N" badge, whose width for N hidden badges is [plusWidth].
 * Every width is widened by [margin]. With no room for a single row ([lines] < 1) nothing is shown.
 */
internal fun visibleBadgeCount(
    widths: List<Int>,
    plusWidth: (Int) -> Int,
    maxWidth: Int,
    lines: Int,
    gap: Int,
    margin: Int = 0,
): Int {
    if (lines < 1) return 0
    fun fits(raw: List<Int>): Boolean {
        val row = raw.map { it + margin }
        var used = 1
        var x = 0
        row.forEach { w ->
            if (x > 0 && x + gap + w > maxWidth) {
                used++
                x = w
            } else {
                x += if (x > 0) gap + w else w
            }
        }
        return used <= lines
    }
    if (fits(widths)) return widths.size
    return (widths.size - 1 downTo 0).first { c -> c == 0 || fits(widths.take(c) + plusWidth(widths.size - c)) }
}

/** Diagonal white band [progress] (0..1) of the way across a box of [size]. */
private fun shineBrush(progress: Float, size: Size): Brush {
    val span = size.width + size.height
    val band = span * 0.25f
    val s = -band + progress * (span + band)
    return Brush.linearGradient(
        listOf(Color.Transparent, Color.White.copy(alpha = 0.7f), Color.Transparent),
        start = Offset(s / 2f, s / 2f),
        end = Offset((s + band) / 2f, (s + band) / 2f),
    )
}

/**
 * Draws the medallion of [badge]: the duration ring (emptying clockwise), the metal rim (gold, silver or red), the
 * gem (radial gradient of the family colour, lighter top-left) and, when animated by [motion], the shine sweep, the
 * pop flash, the star's turn, the shield's glint and the drop's pulsing glow. Animated values are read in the draw
 * phase only, so a frame never recomposes.
 */
private fun Modifier.drawMedallion(badge: EffectBadgeUi, motion: MedallionMotion?): Modifier = drawWithCache {
    val w = size.width
    val h = size.height
    val ringPath = insetPath(badge.shape, size, RingWidth.toPx() / 2f, this)
    val rimPath = insetPath(badge.shape, size, RingWidth.toPx(), this)
    val glintPath = insetPath(badge.shape, size, RingWidth.toPx() + RimWidth.toPx() / 2f, this)
    val gemPath = insetPath(badge.shape, size, (RingWidth + RimWidth).toPx(), this)
    val ringTotal = PathMeasure().run {
        setPath(ringPath, true)
        length
    }
    val ringLeft = Path().also { left ->
        val measure = PathMeasure().apply { setPath(ringPath, true) }
        measure.getSegment(ringTotal - ringLength(ringTotal, badge.remainingFraction), ringTotal, left, true)
    }
    val glintMeasure = PathMeasure().apply { setPath(glintPath, true) }
    val (rimLight, rimDark) = rimColors(badgeTier(badge.code), badge.debuff)
    val rimBrush = Brush.linearGradient(listOf(rimLight, rimDark), start = Offset.Zero, end = Offset(w, h))
    val c = badge.color
    val gemBrush = Brush.radialGradient(
        listOf(lerp(c, Color.White, 0.55f), c, lerp(c, Color.Black, 0.45f)),
        center = Offset(w * 0.32f, h * 0.28f),
        radius = maxOf(w, h) * 0.85f,
    )
    val highlight = Brush.radialGradient(
        listOf(Color.White.copy(alpha = 0.55f), Color.Transparent),
        center = Offset(w * 0.33f, h * 0.27f),
        radius = minOf(w, h) * 0.38f,
    )
    val dropGlowCenter = Offset(w / 2f, h * 0.64f)
    val dropGlowRadius = w * 0.42f
    val ringColor = lerp(c, Color.White, 0.3f)
    val ringStroke = Stroke(RingWidth.toPx(), cap = StrokeCap.Round)
    val glintStroke = Stroke(GlintWidth.toPx(), cap = StrokeCap.Round)
    onDrawBehind {
        val now = motion?.now ?: 0L
        val rotation = if (motion != null && badge.shape == BadgeShape.STAR) starRotation(now) else 0f
        rotate(rotation) {
            drawPath(ringPath, RingTrack, style = ringStroke)
            if (badge.remainingFraction >= 0f) drawPath(ringLeft, ringColor, style = ringStroke)
            drawPath(rimPath, rimBrush)
            drawPath(gemPath, gemBrush)
            clipPath(gemPath) {
                drawRect(highlight)
                if (motion != null) {
                    val elapsed = now - motion.bornAt
                    if (badge.shape == BadgeShape.DROP) {
                        val pulse = 0.5f + 0.5f * sin(2f * PI.toFloat() * Math.floorMod(now, DROP_PULSE_MS) / DROP_PULSE_MS)
                        drawCircle(
                            Brush.radialGradient(
                                listOf(Color.White.copy(alpha = 0.15f + 0.4f * pulse), Color.Transparent),
                                center = dropGlowCenter,
                                radius = dropGlowRadius,
                            ),
                            radius = dropGlowRadius,
                            center = dropGlowCenter,
                        )
                    }
                    val shine = shineProgress(elapsed)
                    if (shine >= 0f) drawRect(shineBrush(shine, size))
                    val flash = popFlash(elapsed)
                    if (flash > 0f) drawRect(Color.White.copy(alpha = flash))
                }
            }
            if (motion != null && badge.shape == BadgeShape.SHIELD) drawGlint(glintMeasure, now, glintStroke)
        }
    }
}

/** A short metallic reflection running round the shield's rim. */
private fun DrawScope.drawGlint(measure: PathMeasure, now: Long, stroke: Stroke) {
    val total = measure.length
    val start = Math.floorMod(now, SHIELD_GLINT_MS).toFloat() / SHIELD_GLINT_MS * total
    val end = start + total * SHIELD_GLINT_LENGTH
    val glint = Path()
    measure.getSegment(start, minOf(end, total), glint, true)
    if (end > total) measure.getSegment(0f, end - total, glint, true)
    drawPath(glint, Color.White.copy(alpha = 0.85f), style = stroke)
}

/** The code in white with a dark outline (the outline carries no semantics, so the code is one node). */
@Composable
private fun OutlinedCode(code: String, style: TextStyle, modifier: Modifier = Modifier, outlineWidth: Dp = TextOutlineWidth) {
    val stroke = with(LocalDensity.current) { Stroke(width = outlineWidth.toPx(), join = StrokeJoin.Round) }
    Box(modifier) {
        Text(
            code,
            style = style.copy(color = TextOutline, drawStyle = stroke),
            maxLines = 1,
            softWrap = false,
            modifier = Modifier.clearAndSetSemantics {},
        )
        Text(code, style = style.copy(color = Color.White), maxLines = 1, softWrap = false)
    }
}

/** One medallion, 26 dp high: as wide as its shape, or as its text for a pill; animated when [motion] is given. */
@Composable
private fun Medallion(badge: EffectBadgeUi, style: TextStyle, motion: MedallionMotion?) {
    val width = shapeWidth(badge.shape)
    val size = if (width == null) Modifier.height(MedallionHeight) else Modifier.size(width, MedallionHeight)
    val animation = if (motion == null) {
        Modifier
    } else {
        // Scaled when drawn (not as a layer), so the pop and the beat never move the badge's layout or its bounds.
        Modifier
            .graphicsLayer { alpha = motion.at(badge).alpha }
            .drawWithContent {
                val scale = motion.at(badge).scale
                scale(scale) { this@drawWithContent.drawContent() }
            }
            .semantics { this[BadgeMotionKey] = { motion.at(badge) } }
    }
    Box(
        Modifier.testTag("badge_${badge.shape.name.lowercase()}").then(size).then(animation).drawMedallion(badge, motion),
        contentAlignment = textAlignment(badge.shape),
    ) {
        OutlinedCode(badge.code, style, if (width == null) Modifier.padding(horizontal = PillPadH) else Modifier)
    }
}

/** A medallion in the fight: pops when it appears, beats when its duration goes down, freezes with [running]. */
@Composable
private fun AnimatedMedallion(badge: EffectBadgeUi, clock: BadgeClock, running: Boolean, style: TextStyle) {
    val motion = remember {
        val now = clock.peek()
        MedallionMotion(clock, if (running) now else now - SETTLED_MS, badge.remainingFraction)
    }
    SideEffect { motion.observe(badge.remainingFraction) }
    Medallion(badge, style, motion)
}

/** One badge as drawn in the help screen: a still medallion. */
@Composable
fun EffectBadge(badge: EffectBadgeUi) {
    Medallion(badge, LocalTextStyle.current.merge(BadgeStyle), motion = null)
}

/**
 * Effect medallions wrapping over as many rows as fit in the available height; the overflow collapses into "+N".
 * The area never grows with the number of effects. The animations run only while [running].
 */
@Composable
fun EffectIcons(badges: List<EffectBadgeUi>, running: Boolean = true, modifier: Modifier = Modifier) {
    val measurer = rememberTextMeasurer()
    val density = LocalDensity.current
    val clock = remember { BadgeClock() }
    PausableClock(clock, running && badges.isNotEmpty(), infinite = true) { clock.advance(it) }
    // One style for measuring and drawing, so the budget matches what is rendered.
    val style = LocalTextStyle.current.merge(BadgeStyle)
    BoxWithConstraints(modifier.clipToBounds()) {
        val (lines, shown) = with(density) {
            val padW = (PillPadH * 2).roundToPx()
            fun width(text: String) = measurer.measure(text, style).size.width + padW
            fun width(badge: EffectBadgeUi) = shapeWidth(badge.shape)?.roundToPx() ?: width(badge.code)
            val rowHeight = EffectRowHeight.roundToPx()
            val gap = BadgeGap.roundToPx()
            val lines = if (constraints.hasBoundedHeight) {
                (constraints.maxHeight + gap) / (rowHeight + gap)
            } else {
                Int.MAX_VALUE
            }
            val maxWidth = if (constraints.hasBoundedWidth) constraints.maxWidth else Int.MAX_VALUE
            lines to visibleBadgeCount(badges.map { width(it) }, { width("+$it") }, maxWidth, lines, gap, BadgeMargin.roundToPx())
        }
        // Not even one row fits: draw nothing rather than a half-clipped row.
        if (lines >= 1) FlowRow(
            horizontalArrangement = Arrangement.spacedBy(BadgeGap),
            verticalArrangement = Arrangement.spacedBy(BadgeGap),
        ) {
            // Same code twice (two instances of one combo): keyed by occurrence, so each keeps its own animation.
            val seen = mutableMapOf<String, Int>()
            badges.take(shown).forEach { badge ->
                val occurrence = seen.getOrDefault(badge.code, 0)
                seen[badge.code] = occurrence + 1
                key(badge.code, occurrence) { AnimatedMedallion(badge, clock, running, style) }
            }
            if (shown < badges.size) {
                Medallion(EffectBadgeUi("+${badges.size - shown}", false, SUMMARY_FRACTION, BadgeShape.PILL, BadgeColors.Neutral), style, null)
            }
        }
    }
}

/**
 * The Dazed star medallion drawn big (up to 90 dp, fitting the area given) with the [ticksLeft] countdown on top. The
 * star turns, shines and pops like the DZ badge; the number pulses whenever it changes. Everything freezes while not
 * [running]. [fraction] (0..1) is the duration ring.
 */
@Composable
internal fun DazeCountdownStar(ticksLeft: Int, fraction: Float, running: Boolean, modifier: Modifier = Modifier) {
    val clock = remember { BadgeClock() }
    PausableClock(clock, running, infinite = true) { clock.advance(it) }
    val motion = remember {
        val now = clock.peek()
        MedallionMotion(clock, if (running) now else now - SETTLED_MS, fraction)
    }
    SideEffect { motion.observe(fraction) }
    val countdown = remember { CountdownChange(clock, ticksLeft) }
    SideEffect { countdown.observe(ticksLeft) }
    val badge = dazedBadge(fraction)
    val badgeWidth = shapeWidth(BadgeShape.STAR) ?: MedallionHeight
    BoxWithConstraints(modifier, contentAlignment = Alignment.Center) {
        val side = minOf(maxWidth, maxHeight, DazeStarMaxSize)
        val factor = side / MedallionHeight
        val fontSize = with(LocalDensity.current) { (side * 0.42f).toSp() }
        val style = LocalTextStyle.current.merge(BadgeStyle).copy(fontSize = fontSize, lineHeight = fontSize)
        Box(Modifier.size(side).testTag("daze_overlay"), contentAlignment = textAlignment(BadgeShape.STAR)) {
            // The DZ medallion at its own size, scaled when drawn (crisp, and its layout never moves).
            Box(
                Modifier.align(Alignment.Center).size(badgeWidth, MedallionHeight)
                    .drawWithContent {
                        val scale = factor * popScale(motion.now - motion.bornAt)
                        scale(scale) { this@drawWithContent.drawContent() }
                    }
                    .drawMedallion(badge, motion),
            )
            OutlinedCode(
                ticksLeft.toString(),
                style,
                Modifier.graphicsLayer {
                    val pulse = countdownPulseScale(clock.now - countdown.changedAt)
                    scaleX = pulse
                    scaleY = pulse
                },
                outlineWidth = TextOutlineWidth * factor / 2f,
            )
        }
    }
}

/** When the Daze countdown last changed its number, on the badges' clock. */
@Stable
private class CountdownChange(private val clock: BadgeClock, private var last: Int) {
    var changedAt by mutableLongStateOf(MedallionMotion.NEVER)
        private set

    fun observe(value: Int) {
        if (value != last) changedAt = clock.peek()
        last = value
    }
}
