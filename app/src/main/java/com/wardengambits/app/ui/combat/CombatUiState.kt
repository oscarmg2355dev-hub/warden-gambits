package com.wardengambits.app.ui.combat

import com.wardengambits.engine.combo.ComboChain
import com.wardengambits.engine.combo.ComboDefinition
import com.wardengambits.app.ui.common.EffectBadgeUi
import com.wardengambits.engine.model.FighterId
import com.wardengambits.engine.model.Outcome

data class FighterUi(
    val id: FighterId,
    val name: String,
    val level: Int,
    val hp: Int,
    val maxHp: Int,
    val power: Int,
    val maxPower: Int,
    val badges: List<EffectBadgeUi>,
    val isAlive: Boolean,
    val isTarget: Boolean,
    /** Combo executed and waiting for the next tick. */
    val queued: ComboDefinition? = null,
    /** Presentation copies of the engine stats shown on the card's first line. */
    val attack: Int = 0,
    val defense: Int = 0,
    val heal: Int = 0,
    val isBoss: Boolean = false,
    /** Portrait key drawn on the card (`Portrait`). */
    val portrait: String = "warden",
    /** Ticks until the fighter can act again because of a Daze (pending included); 0 when not Dazed. */
    val dazedTicksLeft: Int = 0,
)

data class CombatUiState(
    val zoneName: String,
    /** Node level within the zone, e.g. "3/7". */
    val levelLabel: String,
    val tick: Int,
    val paused: Boolean,
    val rivals: List<FighterUi>,
    val player: FighterUi,
    val buffer: String,
    val bufferMatch: ComboDefinition?,
    /** True when casting the buffered combo now would continue the player's chain. */
    val chainContinues: Boolean = false,
    val playerChain: ComboChain = ComboChain(),
    val outcome: Outcome?,
    /** Player level when the combat started. */
    val levelBefore: Int,
    /** Run wins when the combat started. */
    val winsBefore: Int,
    /** False while paused, over, or the Warden is Dazed: combat keys are disabled. */
    val inputsEnabled: Boolean = true,
)
