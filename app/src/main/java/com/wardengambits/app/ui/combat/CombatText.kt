package com.wardengambits.app.ui.combat

import androidx.compose.ui.graphics.Color
import com.wardengambits.app.ui.theme.WardenColors
import com.wardengambits.app.ui.common.effectCode
import com.wardengambits.engine.event.CombatEvent
import com.wardengambits.engine.model.FighterId

/** Code of a rival potion ("+150 PO"). */
const val POTION_CODE = "PO"

/** Colour family of a floating number: damage (red), healing (green) or power gained (blue, the power bar colour). */
enum class CombatTextTone { DAMAGE, HEAL, POWER }

internal val DamageRed = Color(0xFFFF6B6B)
internal val HealGreen = Color(0xFF7CD992)

fun combatTextColor(tone: CombatTextTone): Color = when (tone) {
    CombatTextTone.DAMAGE -> DamageRed
    CombatTextTone.HEAL -> HealGreen
    CombatTextTone.POWER -> WardenColors.Power
}

/**
 * One floating number over the card of [target]: "−4 SP2" (red), "+3 CU1" (green) or "+150 PO" (blue, a rival potion).
 * [amount] is the signed value of the event and [rollPct] its roll (100 when it does not roll).
 */
data class CombatTextLine(
    val target: FighterId,
    val text: String,
    val tone: CombatTextTone,
    val amount: Int = 0,
    val rollPct: Int = 100,
)

/**
 * The floating number of an event: one per [CombatEvent.EffectResolved] (except one that changed nothing, amount 0) and
 * one per [CombatEvent.PotionUsed] ("+150 PO"), none for the rest.
 */
fun combatTextFor(event: CombatEvent): CombatTextLine? = when (event) {
    is CombatEvent.EffectResolved -> if (event.amount == 0) null else {
        val value = if (event.amount < 0) "−${-event.amount}" else "+${event.amount}"
        val code = effectCode(event.sourceCombo, event.kind)
        val tone = if (event.amount < 0) CombatTextTone.DAMAGE else CombatTextTone.HEAL
        CombatTextLine(event.target, if (code == null) value else "$value $code", tone, event.amount, event.rollPct)
    }
    is CombatEvent.PotionUsed -> CombatTextLine(event.fighter, "+${event.amount} $POTION_CODE", CombatTextTone.POWER, event.amount)
    is CombatEvent.ComboQueued,
    is CombatEvent.ComboRejected,
    is CombatEvent.ComboResolved,
    is CombatEvent.EffectApplied,
    is CombatEvent.EffectExpired,
    is CombatEvent.FighterDefeated,
    is CombatEvent.CombatEnded,
    -> null
}
