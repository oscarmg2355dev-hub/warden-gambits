package com.wardengambits.app.ui.combat

import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.text.TextLayoutResult
import com.wardengambits.engine.combo.ComboTuning
import com.wardengambits.engine.model.LevelStats
import com.wardengambits.engine.model.RivalScaling
import com.wardengambits.engine.rules.DamageFormula

/**
 * The widest numbers the combat screen has to fit at the end of the first lap (Lothlórien is reached around level 41;
 * level 45 leaves some margin), computed from the engine so they follow the stat scale and the balance.
 */
object LateGameNumbers {
    const val PLAYER_LEVEL = 45
    private const val RIVAL_LEVEL = 40
    private const val BOSS_LEVEL = 46
    private const val MAX_RIVALS = 4
    private const val CHAINED_DRAIN = 2

    private val tuning = ComboTuning.DEFAULT
    val player = LevelStats.forLevel(PLAYER_LEVEL)
    val boss = LevelStats.forRival(BOSS_LEVEL, RivalScaling.BOSS)
    private val common = LevelStats.forRival(RIVAL_LEVEL)

    /** Number over a rival: the player's Golpe directo with Furia on a common rival ("−14716 GD" with the current numbers). */
    val rivalHitText: String
        get() {
            val hit = DamageFormula.directHit(DamageFormula.spearUnit(player.attack, tuning), true, common.defense, false, false)
            return "−$hit GD"
        }

    /** Number over the player: a chained Robo de vida (tier 2) pulse taking from four rivals ("+26880 RV2"). */
    val playerHealText: String
        get() = "+${MAX_RIVALS * CHAINED_DRAIN * DamageFormula.cureUnit(player.heal, tuning)} RV2"
}

private fun SemanticsNodeInteraction.textLayout(): TextLayoutResult {
    val action = fetchSemanticsNode().config.getOrNull(SemanticsActions.GetTextLayoutResult)!!.action!!
    val out = mutableListOf<TextLayoutResult>()
    action(out)
    return out.single()
}

/** Width in px of the first line of text of this node (what it needs, not the box it was given). */
fun SemanticsNodeInteraction.textWidthPx(): Float = textLayout().let { it.getLineRight(0) - it.getLineLeft(0) }

/** Width in px available to the text of this node (its layout constraint). */
fun SemanticsNodeInteraction.availableWidthPx(): Int = textLayout().layoutInput.constraints.maxWidth

/**
 * True when the text takes at most [maxLines] lines, none wider than the width it was given and nothing cut by its
 * own max lines (so nothing is clipped or ellipsized).
 */
fun SemanticsNodeInteraction.fitsIn(maxLines: Int = 1): Boolean {
    val layout = textLayout()
    val width = layout.layoutInput.constraints.maxWidth
    return layout.lineCount <= maxLines && !layout.multiParagraph.didExceedMaxLines &&
        (0 until layout.lineCount).all { layout.getLineRight(it) - layout.getLineLeft(it) <= width }
}
