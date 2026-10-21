package com.wardengambits.app.ui.combat

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wardengambits.app.ui.common.PausableClock
import com.wardengambits.engine.model.FighterId
import com.wardengambits.engine.model.PLAYER_ID
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.math.sqrt
import kotlin.random.Random

/** Life of a floating number (spec §3). */
const val FLOATING_NUMBER_LIFE_MILLIS = 1100L

/** Most numbers alive at once over one card; the oldest is dropped first. */
const val FLOATING_NUMBERS_MAX_PER_CARD = 8

/** Smallest and largest font size (sp) of a number. */
const val FLOATING_NUMBER_MIN_SP = 13f
const val FLOATING_NUMBER_MAX_SP = 36f

/** Share of the target's maximum at which a number reaches [FLOATING_NUMBER_MAX_SP]. */
private const val SIZE_SATURATION = 0.15

/** A roll at or above this percentage is a strong hit (halo). */
const val STRONG_ROLL_PCT = 130

/** Rise (dp) of a number once it starts to fade. */
private const val RISE_DP = 26f

private const val PEAK_AT = 0.15f
private const val SETTLE_AT = 0.30f
private const val START_SCALE = 0.3f
private const val PEAK_SCALE = 1.25f

/** Share of the life after which a number fades out and rises. */
private const val FADE_AT = 0.70f

/** Grids of cells of the stage (columns to rows): 3×2 over a rival, 4×2 over the Warden. */
private val RIVAL_GRID = 3 to 2
private val PLAYER_GRID = 4 to 2

/** Most a number moves at random inside its cell, as a share of the cell's size. */
private const val JITTER = 0.25f

/** Outline drawn under every number. */
private val OutlineColor = Color(0xE6000000)
private val OutlineWidth = 3.dp

/** Font size (sp) of a number of [amount] against the target's [max]: 13 + 23 × sqrt(clamp(amount / max / 0.15)). */
fun floatingNumberSize(amount: Int, max: Int): Float {
    val ratio = if (max <= 0) 1.0 else (abs(amount).toDouble() / max / SIZE_SATURATION).coerceIn(0.0, 1.0)
    return (FLOATING_NUMBER_MIN_SP + (FLOATING_NUMBER_MAX_SP - FLOATING_NUMBER_MIN_SP) * sqrt(ratio)).toFloat()
}

/**
 * A [CombatTextLine] placed over its card's stage: [cx]/[cy] are its centre as fractions of the stage, [cell] the
 * cell of the stage grid it took; it starts at [startMillis].
 */
data class FloatingNumber(
    val key: Long,
    val target: FighterId,
    val text: String,
    val tone: CombatTextTone,
    val sizeSp: Float,
    val strong: Boolean,
    val cx: Float,
    val cy: Float,
    val cell: Int = 0,
    val startMillis: Long,
) {
    /** 0 when it appears, 1 when it is gone. */
    fun progress(now: Long): Float = (now - startMillis).toFloat() / FLOATING_NUMBER_LIFE_MILLIS
}

/** Scale of a number at [progress]: 0.3 → 1.25 (at 15 %) → 1.0 (at 30 %). */
fun floatingNumberScale(progress: Float): Float = when {
    progress <= 0f -> START_SCALE
    progress < PEAK_AT -> START_SCALE + (PEAK_SCALE - START_SCALE) * progress / PEAK_AT
    progress < SETTLE_AT -> PEAK_SCALE + (1f - PEAK_SCALE) * (progress - PEAK_AT) / (SETTLE_AT - PEAK_AT)
    else -> 1f
}

/** Opacity at [progress]: fully visible until 70 %, then fading to 0; hidden once its life is over. */
fun floatingNumberAlpha(progress: Float): Float = when {
    progress >= 1f -> 0f
    progress <= FADE_AT -> 1f
    else -> (1f - progress) / (1f - FADE_AT)
}

/** Rise (dp) at [progress]: none until 70 %, then up to 26 dp by the end, while it fades. */
fun floatingNumberRise(progress: Float): Float =
    if (progress <= FADE_AT) 0f else RISE_DP * ((progress.coerceAtMost(1f) - FADE_AT) / (1f - FADE_AT))

/** Grid (columns to rows) of the stage of [target]'s card. */
fun floatingNumberGrid(target: FighterId): Pair<Int, Int> = if (target == PLAYER_ID) PLAYER_GRID else RIVAL_GRID

/**
 * A cell of a [cols] × [rows] grid for a new number: one that no number in [occupied] (the cells of the live numbers
 * of the card) uses, or else one of the least used; chosen with the UI-only [random].
 */
fun stageCell(cols: Int, rows: Int, occupied: List<Int>, random: Random): Int {
    val counts = IntArray(cols * rows)
    occupied.forEach { if (it in counts.indices) counts[it]++ }
    val least = counts.min()
    val candidates = counts.indices.filter { counts[it] == least }
    return candidates[random.nextInt(candidates.size)]
}

/** Live floating numbers of every card (pure; the time is the combat clock, frozen while paused). */
class FloatingNumbersState(
    val perCardLimit: Int = FLOATING_NUMBERS_MAX_PER_CARD,
    private val numbers: List<FloatingNumber> = emptyList(),
    private val nextKey: Long = 0L,
) {
    /**
     * Adds [lines] at [now], each in a free cell of its card's stage grid ([gridOf] gives columns to rows; the least
     * used cell when all are taken) and moved at random inside it ([random] is UI-only). [maxOf] gives the maximum
     * vitals of a target (max HP, or max power for the POWER tone) to size the number. Beyond [perCardLimit] per card
     * the oldest are dropped.
     */
    fun add(
        lines: List<CombatTextLine>,
        maxOf: (FighterId) -> Int,
        now: Long,
        random: Random,
        gridOf: (FighterId) -> Pair<Int, Int> = ::floatingNumberGrid,
    ): FloatingNumbersState {
        var key = nextKey
        val all = lines.fold(numbers) { acc, line ->
            val (cols, rows) = gridOf(line.target)
            val cell = stageCell(cols, rows, acc.filter { it.target == line.target }.map { it.cell }, random)
            val number = FloatingNumber(
                key = key++,
                target = line.target,
                text = line.text,
                tone = line.tone,
                sizeSp = floatingNumberSize(line.amount, maxOf(line.target)),
                strong = line.rollPct >= STRONG_ROLL_PCT,
                cx = (cell % cols + 0.5f + jitter(random)) / cols,
                cy = (cell / cols + 0.5f + jitter(random)) / rows,
                cell = cell,
                startMillis = now,
            )
            val added = acc + number
            val sameCard = added.filter { it.target == line.target }
            if (sameCard.size > perCardLimit) added - sameCard.first() else added
        }
        return FloatingNumbersState(perCardLimit, all, key)
    }

    /** Drops the numbers whose life is over at [now]. */
    fun prune(now: Long): FloatingNumbersState =
        FloatingNumbersState(perCardLimit, numbers.filter { it.progress(now) < 1f }, nextKey)

    private fun jitter(random: Random): Float = (random.nextFloat() * 2f - 1f) * JITTER

    fun forTarget(id: FighterId): List<FloatingNumber> = numbers.filter { it.target == id }

    val isEmpty: Boolean get() = numbers.isEmpty()

    fun anyExpired(now: Long): Boolean = numbers.any { it.progress(now) >= 1f }
}

/** Holds the floating numbers and their clock, which only advances while [rememberFloatingNumbers] is running. */
@Stable
class FloatingNumbersController {
    var state: FloatingNumbersState by mutableStateOf(FloatingNumbersState())
        private set
    private var clock by mutableLongStateOf(0L)

    fun now(): Long = clock

    /** Drops every number on screen (a new fight starts). */
    fun clear() {
        state = FloatingNumbersState()
    }

    fun add(
        lines: List<CombatTextLine>,
        maxOf: (FighterId) -> Int,
        random: Random = Random,
        gridOf: (FighterId) -> Pair<Int, Int> = ::floatingNumberGrid,
    ) {
        if (lines.isNotEmpty()) state = state.prune(clock).add(lines, maxOf, clock, random, gridOf)
    }

    internal fun advance(millis: Long) {
        clock += millis
        if (state.anyExpired(clock)) state = state.prune(clock)
    }
}

/**
 * The floating numbers of the screen. Its clock follows the frame clock only while [running] (the fight is not
 * paused) and there is something to show, so a paused fight freezes them and a finished one lets them run out.
 */
@Composable
fun rememberFloatingNumbers(running: Boolean): FloatingNumbersController {
    val controller = remember { FloatingNumbersController() }
    val busy = !controller.state.isEmpty
    PausableClock(controller, running && busy) { controller.advance(it) }
    return controller
}

/** Where a floating number goes in its card (px) and the extra scale that keeps it whole. */
data class NumberPlacement(val x: Float, val y: Float, val fit: Float)

/**
 * Top-left corner ([x], [y], px) of a number of [textWidth] × [textHeight] px laid out in a card of [cardWidth] ×
 * [cardHeight] px, and the [fit] scale (≤ 1) that keeps it whole: the number is scaled around its centre, so at its
 * 1.25 peak and at the end of its [maxRise] (px) it must still be inside the card. [xFrac]/[yFrac] are the wished
 * position; it is moved in as little as needed.
 */
fun floatingNumberPlacement(
    xFrac: Float,
    yFrac: Float,
    textWidth: Float,
    textHeight: Float,
    cardWidth: Float,
    cardHeight: Float,
    maxRise: Float,
): NumberPlacement {
    if (textWidth <= 0f || textHeight <= 0f) return NumberPlacement(xFrac * cardWidth, yFrac * cardHeight, 1f)
    val fit = minOf(1f, cardWidth / (textWidth * PEAK_SCALE), cardHeight / (textHeight * PEAK_SCALE))
    val peakWidth = textWidth * fit * PEAK_SCALE
    val peakHeight = textHeight * fit * PEAK_SCALE
    val xMin = (peakWidth - textWidth) / 2f
    val xMax = cardWidth - (textWidth + peakWidth) / 2f
    val yMin = maxOf((peakHeight - textHeight) / 2f, maxRise + (textHeight * fit - textHeight) / 2f)
    val yMax = cardHeight - (textHeight + peakHeight) / 2f
    val x = (xFrac * cardWidth).coerceAtLeast(xMin).coerceAtMost(xMax)
    val y = (yFrac * cardHeight).coerceAtLeast(yMin).coerceAtMost(yMax)
    return NumberPlacement(x, y, fit)
}

/**
 * Draws [numbers] over a card's stage (fill the stage with it). Each is centred at its fraction of the stage, moved
 * in (and shrunk if it is bigger than its cell of the stage grid) so that it never leaves its cell, even at its 1.25
 * peak. [clock] is read only while placing its layer: the pop, the rise and the fade never recompose nor relayout it.
 */
@Composable
fun FloatingNumbersLayer(numbers: List<FloatingNumber>, clock: () -> Long, modifier: Modifier = Modifier) {
    val outline = with(LocalDensity.current) { Stroke(width = OutlineWidth.toPx(), join = StrokeJoin.Round) }
    // Not clipped to the stage: when a cell is too short to keep the 26 dp rise inside the stage, the fading number
    // rises over the card above it (the card clips it; the stage starts at least half a card below its top).
    Box(modifier.fillMaxSize()) {
        numbers.forEach { number ->
            key(number.key) {
                val text = numberText(number)
                val color = combatTextColor(number.tone)
                val style = TextStyle(
                    fontFamily = FontFamily.SansSerif,
                    fontWeight = FontWeight.Bold,
                    fontSize = number.sizeSp.sp,
                    color = color,
                    shadow = if (number.strong) Shadow(color, blurRadius = 12f) else null,
                )
                Box(
                    Modifier.layout { measurable, constraints ->
                        val placeable = measurable.measure(Constraints())
                        layout(constraints.maxWidth, constraints.maxHeight) {
                            val cell = numberCell(number, constraints.maxWidth.toFloat(), constraints.maxHeight.toFloat())
                            val textWidth = placeable.width.toFloat()
                            val textHeight = placeable.height.toFloat()
                            // Kept (and shrunk) inside its own cell, so numbers in different cells never overlap; room
                            // for its rise is kept above it inside the stage when the cell allows it.
                            val placement = floatingNumberPlacement(
                                cell.xFrac - if (cell.width > 0f) textWidth / 2f / cell.width else 0f,
                                cell.yFrac - if (cell.height > 0f) textHeight / 2f / cell.height else 0f,
                                textWidth, textHeight, cell.width, cell.height,
                                (RISE_DP.dp.toPx() - cell.top).coerceAtLeast(0f),
                            )
                            placeable.placeWithLayer((cell.left + placement.x).roundToInt(), (cell.top + placement.y).roundToInt()) {
                                val progress = number.progress(clock())
                                val scale = floatingNumberScale(progress) * placement.fit
                                scaleX = scale
                                scaleY = scale
                                translationY = -floatingNumberRise(progress).dp.toPx()
                                alpha = floatingNumberAlpha(progress)
                            }
                        }
                    },
                ) {
                    // Solid outline first (no semantics, so the number is one node), then the fill.
                    Text(
                        text = text,
                        style = style.copy(color = OutlineColor, shadow = null, drawStyle = outline),
                        softWrap = false,
                        maxLines = 1,
                        modifier = Modifier.clearAndSetSemantics {},
                    )
                    Text(text = text, style = style, softWrap = false, maxLines = 1)
                }
            }
        }
    }
}

/** The cell of a number in a stage of [width] × [height] px (px) and its centre as fractions of that cell. */
private class NumberCell(val left: Float, val top: Float, val width: Float, val height: Float, val xFrac: Float, val yFrac: Float)

private fun numberCell(number: FloatingNumber, width: Float, height: Float): NumberCell {
    val (cols, rows) = floatingNumberGrid(number.target)
    val cell = number.cell.coerceIn(0, cols * rows - 1)
    val col = cell % cols
    val row = cell / cols
    return NumberCell(
        left = width * col / cols,
        top = height * row / rows,
        width = width / cols,
        height = height / rows,
        xFrac = number.cx * cols - col,
        yFrac = number.cy * rows - row,
    )
}

/** "−1840 SP2" with the combo code at half the size. */
private fun numberText(number: FloatingNumber) = buildAnnotatedString {
    val space = number.text.indexOf(' ')
    if (space < 0) {
        append(number.text)
    } else {
        append(number.text.substring(0, space + 1))
        withStyle(SpanStyle(fontSize = (number.sizeSp / 2f).sp)) { append(number.text.substring(space + 1)) }
    }
}
