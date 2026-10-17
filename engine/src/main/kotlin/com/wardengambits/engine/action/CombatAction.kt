package com.wardengambits.engine.action

import com.wardengambits.engine.combo.Input
import com.wardengambits.engine.model.FighterId

sealed interface CombatAction {
    data class Press(val fighter: FighterId, val input: Input) : CombatAction
    data class Execute(val fighter: FighterId) : CombatAction
    data class ClearBuffer(val fighter: FighterId) : CombatAction
    data class SelectTarget(val target: FighterId) : CombatAction
    data object Tick : CombatAction
}
