package com.wardengambits.app.ui.common

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import com.wardengambits.app.ui.theme.BadgeColors
import com.wardengambits.engine.combo.ComboDefinition
import com.wardengambits.engine.combo.ComboEffect
import com.wardengambits.engine.combo.ComboFamily
import com.wardengambits.engine.combo.Combos
import com.wardengambits.engine.model.ActiveEffect
import com.wardengambits.engine.model.EffectKind

/** Marks a badge with no duration bar (the Daze summary). */
const val SUMMARY_FRACTION = -1f

/** Outline of a badge: tiered families are pills; shields, multipliers, drains and Dazed get their own drawing. */
enum class BadgeShape { PILL, SHIELD, DIAMOND, DROP, STAR }

/**
 * Presentation of one active effect instance: its source-combo [code], whether it is a debuff (sorting only), the
 * duration left (0..1), and its [shape], fill [color] and text [contentColor] (from family and tier).
 */
data class EffectBadgeUi(
    val code: String,
    val debuff: Boolean,
    val remainingFraction: Float,
    val shape: BadgeShape = BadgeShape.PILL,
    val color: Color = BadgeColors.Neutral,
    val contentColor: Color = contentColorOn(color),
)

/** Fill, outline and text colour of a badge. */
private data class BadgeLook(val shape: BadgeShape, val color: Color)

/** Text colour with the best contrast on [background]: dark on light tones, white on dark ones. */
fun contentColorOn(background: Color): Color =
    if (background.luminance() > 0.18f) BadgeColors.DarkText else BadgeColors.LightText

/** The four tones (tier 1 lightest to tier 4 darkest) of a tiered family; empty for the rest. */
fun tierTones(family: ComboFamily): List<Color> = with(BadgeColors) {
    when (family) {
        ComboFamily.SPEAR_A -> listOf(SpearA1, SpearA2, SpearA3, SpearA4)
        ComboFamily.SPEAR_B -> listOf(SpearB1, SpearB2, SpearB3, SpearB4)
        ComboFamily.DAZE -> listOf(Daze1, Daze2, Daze3, Daze4)
        ComboFamily.AREA -> listOf(Area1, Area2, Area3, Area4)
        ComboFamily.HEAL -> listOf(Heal1, Heal2, Heal3, Heal4)
        ComboFamily.DRAIN, ComboFamily.MULTIPLIER, ComboFamily.SHIELD, ComboFamily.DIRECT, ComboFamily.RECOVER -> emptyList()
    }
}

private fun tone(family: ComboFamily, tier: Int): Color = tierTones(family).let { it[(tier - 1).coerceIn(0, it.lastIndex)] }

/** Family whose tones an effect kind takes when its source combo is unknown. */
private fun kindFamily(kind: EffectKind): ComboFamily = when (kind) {
    EffectKind.DOT -> ComboFamily.SPEAR_A
    EffectKind.COT -> ComboFamily.HEAL
    EffectKind.DAZE_CHANCE -> ComboFamily.DAZE
    EffectKind.DRAIN -> ComboFamily.DRAIN
    EffectKind.FURY, EffectKind.GUARD, EffectKind.VIGOR -> ComboFamily.MULTIPLIER
    EffectKind.SHIELD_MASTER, EffectKind.SHIELD_TACTICS -> ComboFamily.SHIELD
    EffectKind.DAZED -> ComboFamily.DIRECT
}

/** Shape and colour of the effect [kind] left by [sourceCombo]: family hue, tier tone. */
private fun badgeLook(sourceCombo: String, kind: EffectKind): BadgeLook {
    val combo = Combos.find(sourceCombo)
    val tier = combo?.tier?.takeIf { it > 0 } ?: 1
    return when (kind) {
        EffectKind.DAZED -> BadgeLook(BadgeShape.STAR, BadgeColors.Dazed)
        EffectKind.FURY -> BadgeLook(BadgeShape.DIAMOND, BadgeColors.Fury)
        EffectKind.GUARD -> BadgeLook(BadgeShape.DIAMOND, BadgeColors.Guard)
        EffectKind.VIGOR -> BadgeLook(BadgeShape.DIAMOND, BadgeColors.Vigor)
        EffectKind.SHIELD_MASTER -> BadgeLook(BadgeShape.SHIELD, BadgeColors.ShieldMaster)
        EffectKind.SHIELD_TACTICS -> BadgeLook(BadgeShape.SHIELD, BadgeColors.ShieldTactics)
        EffectKind.DRAIN -> BadgeLook(BadgeShape.DROP, if (tier >= 2) BadgeColors.Drain2 else BadgeColors.Drain1)
        EffectKind.DOT, EffectKind.COT, EffectKind.DAZE_CHANCE -> {
            val family = combo?.family?.takeIf { tierTones(it).isNotEmpty() } ?: kindFamily(kind)
            BadgeLook(BadgeShape.PILL, tone(family, tier))
        }
    }
}

private fun badge(code: String, debuff: Boolean, fraction: Float, look: BadgeLook) =
    EffectBadgeUi(code, debuff, fraction, look.shape, look.color)

/** The Dazed star badge with [fraction] (0..1) of its duration left. */
fun dazedBadge(fraction: Float): EffectBadgeUi = badge("DZ", true, fraction, badgeLook("", EffectKind.DAZED))

/** DoT, Daze chance and Dazed are debuffs; every other effect is a buff. */
private fun isDebuff(kind: EffectKind): Boolean = when (kind) {
    EffectKind.DOT, EffectKind.DAZE_CHANCE, EffectKind.DAZED -> true
    EffectKind.COT, EffectKind.FURY, EffectKind.GUARD, EffectKind.VIGOR,
    EffectKind.SHIELD_MASTER, EffectKind.SHIELD_TACTICS, EffectKind.DRAIN,
    -> false
}

private fun familyCode(family: ComboFamily): String? = when (family) {
    ComboFamily.SPEAR_A -> "SP"
    ComboFamily.SPEAR_B -> "LC"
    ComboFamily.DAZE -> "AT"
    ComboFamily.AREA -> "PA"
    ComboFamily.HEAL -> "CU"
    ComboFamily.DRAIN -> "RV"
    ComboFamily.MULTIPLIER, ComboFamily.SHIELD, ComboFamily.DIRECT, ComboFamily.RECOVER -> null
}

private fun kindCode(kind: EffectKind): String = when (kind) {
    EffectKind.DOT -> "SP"
    EffectKind.COT -> "CU"
    EffectKind.DAZE_CHANCE -> "AT"
    EffectKind.DAZED -> "DZ"
    EffectKind.FURY -> "FU"
    EffectKind.GUARD -> "GU"
    EffectKind.VIGOR -> "BR"
    EffectKind.SHIELD_MASTER -> "EM"
    EffectKind.SHIELD_TACTICS -> "ET"
    EffectKind.DRAIN -> "RV"
}

/** Code of the direct hit (`123`), which leaves no effect behind. */
private const val DIRECT_HIT_CODE = "GD"

/**
 * Two-letter family code plus tier of the combo [sourceCombo] (buffs and Dazed have no tier); the code of [kind]
 * when the combo has no family code; "GD" for a direct hit (no [kind]); null when nothing applies.
 */
fun effectCode(sourceCombo: String, kind: EffectKind?): String? {
    if (kind == EffectKind.DAZED) return "DZ"
    val combo = Combos.find(sourceCombo)
    val family = combo?.let { familyCode(it.family) }
    return when {
        combo != null && family != null -> "$family${combo.tier}"
        kind != null -> kindCode(kind)
        combo?.family == ComboFamily.DIRECT -> DIRECT_HIT_CODE
        else -> null
    }
}

private fun codeFor(effect: ActiveEffect): String = effectCode(effect.sourceCombo, effect.kind) ?: kindCode(effect.kind)

fun badgeFor(effect: ActiveEffect): EffectBadgeUi {
    val fraction = if (effect.totalTicks <= 0) 0f else (effect.remainingTicks.toFloat() / effect.totalTicks).coerceIn(0f, 1f)
    return badge(codeFor(effect), isDebuff(effect.kind), fraction, badgeLook(effect.sourceCombo, effect.kind))
}

/** Effect kind a combo leaves on its target, or null when it leaves none (direct hit, Recuperar). */
private fun leftKind(effect: ComboEffect): EffectKind? = when (effect) {
    is ComboEffect.SpearDot, is ComboEffect.AreaDot -> EffectKind.DOT
    is ComboEffect.DazeChance -> EffectKind.DAZE_CHANCE
    is ComboEffect.HealOverTime -> EffectKind.COT
    is ComboEffect.Drain -> EffectKind.DRAIN
    is ComboEffect.Buff -> effect.kind
    ComboEffect.DirectHit, is ComboEffect.PowerRestore -> null
}

/** The badge (full duration) of the effect [def] leaves, for the help screen; null when it leaves none. */
fun comboBadge(def: ComboDefinition): EffectBadgeUi? {
    val kind = leftKind(def.effect) ?: return null
    val code = effectCode(def.sequence, kind) ?: return null
    return badge(code, isDebuff(kind), 1f, badgeLook(def.sequence, kind))
}

/**
 * The badges of a fighter on [tick]: one per effect instance; DoT, CoT, Daze chance and Dazed are always shown, the
 * rest only once active. Debuffs first, then buffs, each by remaining time ascending. With several Daze-chance
 * instances a summary badge "AT n%" (the accumulated [dazeChancePct], no duration bar) goes first.
 */
fun badgesFor(effects: List<ActiveEffect>, tick: Int, dazeChancePct: Int = 0): List<EffectBadgeUi> {
    val shown = effects.filter {
        when (it.kind) {
            EffectKind.DOT, EffectKind.COT, EffectKind.DAZE_CHANCE, EffectKind.DAZED -> true
            EffectKind.FURY, EffectKind.GUARD, EffectKind.VIGOR, EffectKind.SHIELD_MASTER,
            EffectKind.SHIELD_TACTICS, EffectKind.DRAIN,
            -> it.isActive(tick)
        }
    }.sortedWith(compareBy<ActiveEffect> { !isDebuff(it.kind) }.thenBy { it.remainingTicks })
    val summary = if (shown.count { it.kind == EffectKind.DAZE_CHANCE } >= 2) {
        listOf(EffectBadgeUi("AT $dazeChancePct%", true, SUMMARY_FRACTION, BadgeShape.PILL, BadgeColors.Neutral))
    } else {
        emptyList()
    }
    return summary + shown.map(::badgeFor)
}

/** Tier of a badge, from the last character of its [code] when it is a digit ("SP3" -> 3); 0 without one ("EM"). */
internal fun badgeTier(code: String): Int = code.lastOrNull()?.takeIf { it.isDigit() }?.digitToInt() ?: 0

private val SilverRim = Color(0xFFC8CCD4) to Color(0xFF6A6E78)
private val GoldRim = Color(0xFFF4D98A) to Color(0xFF8A6420)
private val RedRim = Color(0xFFF08A7A) to Color(0xFF7A1E16)

/** Light and dark ends of a medallion's metal rim: reddish on debuffs, gold from tier 3, silver below. */
internal fun rimColors(tier: Int, debuff: Boolean): Pair<Color, Color> = when {
    debuff -> RedRim
    tier >= 3 -> GoldRim
    else -> SilverRim
}
