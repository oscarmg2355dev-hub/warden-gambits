package com.wardengambits.app.ui.combat

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.layout
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.constrainHeight
import androidx.compose.ui.unit.constrainWidth
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wardengambits.app.R
import com.wardengambits.app.i18n.trText
import com.wardengambits.app.i18n.trTextResource
import com.wardengambits.app.ui.common.BadgeShape
import com.wardengambits.app.ui.common.DazeCountdownStar
import com.wardengambits.app.ui.common.EffectIcons
import com.wardengambits.app.ui.common.EffectRowHeight
import com.wardengambits.app.ui.common.Portrait
import com.wardengambits.app.ui.theme.WardenColors
import kotlin.math.roundToInt

// Style C (spec §3): brown gradient frame, thin gold border, top inner gloss and a 3 dp bottom shadow.
private val CardShape = RoundedCornerShape(8.dp)
private val FrameTop = Color(0xFF3A2D22)
private val FrameBottom = Color(0xFF221A13)
private val FrameBorder = Color(0xFF9B7B3D)
private val FrameGloss = Color(0x26FFFFFF)
private val FrameShadow = Color.Black
private val ShadowDepth = 3.dp
private val NameGold = Color(0xFFF4D98A)
private val StatsColor = Color(0xFFCFC3A8)
private val PortraitBorder = Color(0xFFC9A24A)
private val PortraitBackground = Brush.radialGradient(listOf(Color(0xFF6B5A45), Color(0xFF1B140F)))
private val PortraitShape = RoundedCornerShape(6.dp)

private val RivalPortraitSize = 44.dp

/** Bosses fight alone, so their portrait is bigger and its «ELITE» ribbon stays legible. */
private val BossPortraitSize = 60.dp
private val PlayerPortraitSize = 58.dp
private val CardPadding = 5.dp
private val CardGap = 3.dp

/** Margin of the numbers' stage below the effects and above the card's bottom, and its least height. */
private val StageMargin = 4.dp
private val StageMinHeight = 40.dp

private val RivalNameStyle = TextStyle(fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold, fontSize = 12.sp, lineHeight = 13.sp)
private val PlayerNameStyle = TextStyle(fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold, fontSize = 14.sp, lineHeight = 16.sp)
private val StatsStyle = TextStyle(fontWeight = FontWeight.Bold, fontSize = 10.sp, lineHeight = 11.sp)
private val MarkStyle = TextStyle(fontSize = 13.sp, lineHeight = 13.sp)

/** Opacity of a defeated fighter's card content (on top of the grey filter). */
private const val DEFEATED_ALPHA = 0.55f

/**
 * Common frame of the combat cards: gradient, border (2 dp gold when [highlighted]), gloss and shadow.
 * [content] is drawn inside; callers put their [FloatingNumbersLayer] last so it is over everything.
 */
@Composable
fun CardFrame(
    highlighted: Boolean,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    clickEnabled: Boolean = true,
    content: @Composable BoxScope.() -> Unit,
) {
    Box(
        modifier
            .drawBehind {
                // Bottom shadow: the same rounded shape, 3 dp lower, behind the card.
                val radius = CornerRadius(8.dp.toPx())
                drawRoundRect(FrameShadow, topLeft = Offset(0f, ShadowDepth.toPx()), size = size, cornerRadius = radius)
            }
            .clip(CardShape)
            // Inside the clip, so the ripple has the card's rounded corners.
            .then(if (onClick == null) Modifier else Modifier.clickable(enabled = clickEnabled, role = Role.Button, onClick = onClick))
            .background(Brush.verticalGradient(listOf(FrameTop, FrameBottom)))
            .drawBehind {
                // Inner gloss along the top edge.
                drawLine(FrameGloss, Offset(0f, 1.5.dp.toPx()), Offset(size.width, 1.5.dp.toPx()), strokeWidth = 1.dp.toPx())
            }
            .border(if (highlighted) 2.dp else 1.dp, if (highlighted) WardenColors.Primary else FrameBorder, CardShape),
        content = content,
    )
}

/** A rival's card: portrait, name and stats, then its bars and effects; tap to make it the target. */
@Composable
fun RivalCard(
    fighter: FighterUi,
    numbers: List<FloatingNumber>,
    clock: () -> Long,
    running: Boolean,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val stage = remember { NumberStage() }
    CardFrame(
        highlighted = fighter.isTarget && fighter.isAlive,
        modifier = modifier,
        onClick = onSelect,
        clickEnabled = fighter.isAlive,
    ) {
        Column(
            Modifier.fillMaxSize().onGloballyPositioned(stage::onCard).padding(CardPadding).defeated(!fighter.isAlive),
            verticalArrangement = Arrangement.spacedBy(CardGap),
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(CardPadding)) {
                FramedPortrait(fighter, if (fighter.isBoss) BossPortraitSize else RivalPortraitSize)
                Column(Modifier.weight(1f)) {
                    Text(
                        trText(fighter.name),
                        style = RivalNameStyle,
                        color = NameGold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    // Big stats wrap after "·" (each label and its number are joined by a no-break space).
                    Text(
                        trTextResource(R.string.combat_card_rival_stats, fighter.level, fighter.attack, fighter.defense),
                        style = StatsStyle,
                        color = StatsColor,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            Vitals(fighter, running, stage, Modifier.weight(1f).fillMaxWidth())
        }
        if (fighter.isAlive && fighter.isTarget) {
            Text(
                "◎",
                style = MarkStyle,
                color = WardenColors.Primary,
                modifier = Modifier.align(Alignment.TopEnd).padding(3.dp).testTag("target_marker_${fighter.id.value}"),
            )
        }
        FloatingNumbersLayer(numbers, clock, Modifier.matchParentSize().numberStage(stage))
    }
}

/** The Warden's card, as wide as the screen: portrait, name and stats with the bars beside it, then its effects. */
@Composable
fun PlayerCard(
    fighter: FighterUi,
    numbers: List<FloatingNumber>,
    clock: () -> Long,
    running: Boolean,
    modifier: Modifier = Modifier,
) {
    val stage = remember { NumberStage() }
    CardFrame(highlighted = false, modifier = modifier) {
        Column(
            Modifier.fillMaxSize().onGloballyPositioned(stage::onCard).padding(CardPadding).defeated(!fighter.isAlive),
            verticalArrangement = Arrangement.spacedBy(CardGap),
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(CardPadding + CardGap)) {
                FramedPortrait(fighter, PlayerPortraitSize)
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(CardGap)) {
                    Text(
                        trTextResource(R.string.combat_card_player_title, fighter.name, fighter.level),
                        style = PlayerNameStyle,
                        color = NameGold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        trTextResource(R.string.combat_stats_player, fighter.attack, fighter.heal, fighter.defense),
                        style = StatsStyle,
                        color = StatsColor,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    GameBar(fighter.hp, fighter.maxHp, GameBarKind.HP, running)
                    GameBar(fighter.power, fighter.maxPower, GameBarKind.POWER, running)
                }
            }
            Effects(fighter, running, stage, Modifier.weight(1f).fillMaxWidth())
        }
        if (fighter.isAlive && fighter.dazedTicksLeft > 0) {
            // Centred on the numbers' stage, under the numbers; the ring follows the DZ badge's duration.
            val fraction = fighter.badges.firstOrNull { it.shape == BadgeShape.STAR }?.remainingFraction ?: 1f
            DazeCountdownStar(fighter.dazedTicksLeft, fraction, running, Modifier.matchParentSize().numberStage(stage))
        }
        FloatingNumbersLayer(numbers, clock, Modifier.matchParentSize().numberStage(stage))
    }
}

/** HP and power bars, then the effects (or «Derrotado»). */
@Composable
private fun Vitals(fighter: FighterUi, running: Boolean, stage: NumberStage, effectsModifier: Modifier) {
    GameBar(fighter.hp, fighter.maxHp, GameBarKind.HP, running)
    GameBar(fighter.power, fighter.maxPower, GameBarKind.POWER, running)
    Effects(fighter, running, stage, effectsModifier)
}

/** The effects (or «Derrotado») at the top of [modifier]'s area; the bottom of what is shown starts the [stage]. */
@Composable
private fun Effects(fighter: FighterUi, running: Boolean, stage: NumberStage, modifier: Modifier) {
    // The area always holds at least one row of icons: on a short card it overflows (clipped by the card) and
    // the numbers' stage falls back to the lower half of the card instead of the icons vanishing.
    val minHeight = if (fighter.isAlive) EffectRowHeight else 0.dp
    Box(modifier.minHeightOverflow(minHeight)) {
        // Inside the Box the content wraps its height, so its bottom is that of the last row of icons.
        val measured = Modifier.fillMaxWidth().onGloballyPositioned(stage::onEffects)
        if (fighter.isAlive) {
            EffectIcons(fighter.badges, running, measured)
        } else {
            Text(trTextResource(R.string.combat_defeated), style = StatsStyle, color = StatsColor, maxLines = 1, modifier = measured)
        }
    }
}

/**
 * Gives the content at least [min] of height to lay out in, even when the parent offers less; the size reported stays
 * within the parent's constraints (never below their minimum, which would make the parent centre it), and the content
 * is placed at the top.
 */
private fun Modifier.minHeightOverflow(min: Dp): Modifier = layout { measurable, constraints ->
    val placeable = measurable.measure(constraints.copy(minHeight = 0, maxHeight = maxOf(constraints.maxHeight, min.roundToPx())))
    layout(constraints.constrainWidth(placeable.width), constraints.constrainHeight(placeable.height)) { placeable.place(0, 0) }
}

/** Where the floating numbers' stage of a card starts: the bottom of its effects row, in the card's coordinates. */
@Stable
private class NumberStage {
    private var card: LayoutCoordinates? = null
    private var effects: LayoutCoordinates? = null

    /** Top of the stage (px from the card's top), before its margin; NaN until both are laid out. */
    var top by mutableFloatStateOf(Float.NaN)
        private set

    fun onCard(coordinates: LayoutCoordinates) {
        card = coordinates
        update()
    }

    fun onEffects(coordinates: LayoutCoordinates) {
        effects = coordinates
        update()
    }

    private fun update() {
        val card = card ?: return
        val effects = effects ?: return
        if (card.isAttached && effects.isAttached) {
            top = card.localPositionOf(effects, Offset(0f, effects.size.height.toFloat())).y
        }
    }
}

/**
 * Lays the numbers layer (as big as the card) out over the [stage]: from 4 dp below the effects to 4 dp above the
 * card's bottom; the lower half of the card when that is shorter than 40 dp. Read in layout only, so a new stage
 * never recomposes the card.
 */
private fun Modifier.numberStage(stage: NumberStage): Modifier = layout { measurable, constraints ->
    val height = constraints.maxHeight
    val margin = StageMargin.roundToPx()
    val bottom = height - margin
    val measured = stage.top
    val top = if (measured.isNaN() || bottom - (measured + margin) < StageMinHeight.toPx()) height / 2 else (measured + margin).roundToInt()
    val placeable = measurable.measure(Constraints.fixed(constraints.maxWidth, (bottom - top).coerceAtLeast(0)))
    layout(constraints.maxWidth, height) { placeable.place(0, top) }
}

/** Label of the boss ribbon: the same in every language and always in Latin letters, like the combo codes. */
internal const val ELITE_LABEL = "ELITE"

private val EliteRed = Color(0xFFB3261E)

/** Portrait in a gold frame with an inner shadow; a boss wears the crown in its corner and the «ELITE» ribbon. */
@Composable
private fun FramedPortrait(fighter: FighterUi, size: Dp) {
    Box(
        Modifier.size(size).clip(PortraitShape).background(PortraitBackground).border(2.dp, PortraitBorder, PortraitShape),
        contentAlignment = Alignment.Center,
    ) {
        // Inner shadow over the picture only (a dark rim fading inwards), so the boss marks stay bright on top.
        Box(
            Modifier.matchParentSize().drawWithContent {
                drawContent()
                drawRoundRect(
                    Brush.radialGradient(listOf(Color.Transparent, Color(0xCC000000)), radius = this.size.minDimension * 0.75f),
                    cornerRadius = CornerRadius(6.dp.toPx()),
                )
            },
            contentAlignment = Alignment.Center,
        ) {
            Portrait(fighter.portrait, size - 4.dp, dimmed = !fighter.isAlive)
        }
        if (fighter.isBoss) {
            Text(
                trTextResource(R.string.combat_boss),
                style = MarkStyle,
                color = WardenColors.Primary,
                modifier = Modifier.align(Alignment.TopStart).padding(start = 2.dp),
            )
            EliteRibbon(size)
        }
    }
}

/**
 * Red diagonal band across the top-right corner of a boss portrait of [size], with [ELITE_LABEL] in white. The band is
 * placed centred on the corner's diagonal and rotated 45°; the portrait's clip trims its ends.
 */
@Composable
private fun BoxScope.EliteRibbon(size: Dp) {
    val band = size * 0.95f
    val thickness = size * 0.2f
    Box(
        Modifier
            .align(Alignment.TopStart)
            .layout { measurable, _ ->
                val w = band.roundToPx()
                val h = thickness.roundToPx()
                val placeable = measurable.measure(Constraints.fixed(w, h))
                val box = size.roundToPx()
                val inset = (box * 0.24f).roundToInt()
                layout(0, 0) { placeable.place(box - inset - w / 2, inset - h / 2) }
            }
            .graphicsLayer { rotationZ = 45f }
            .background(EliteRed)
            .testTag("elite_ribbon"),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            ELITE_LABEL,
            color = Color.White,
            fontWeight = FontWeight.Black,
            fontSize = (size.value * 0.14f).sp,
            lineHeight = (size.value * 0.14f).sp,
            letterSpacing = 0.5.sp,
            maxLines = 1,
        )
    }
}

/** Paint of the layer a defeated card is drawn through: no saturation (works on every API level), dimmed. */
private val DefeatedPaint = Paint().apply {
    colorFilter = ColorFilter.colorMatrix(ColorMatrix().apply { setToSaturation(0f) })
    alpha = DEFEATED_ALPHA
}

/** Greys out (and fades) a defeated fighter's card content; the bars, already empty, turn grey with it. */
private fun Modifier.defeated(defeated: Boolean): Modifier = if (!defeated) this else this
    .drawWithContent {
        drawIntoCanvas { canvas ->
            canvas.saveLayer(Rect(Offset.Zero, size), DefeatedPaint)
            drawContent()
            canvas.restore()
        }
    }
