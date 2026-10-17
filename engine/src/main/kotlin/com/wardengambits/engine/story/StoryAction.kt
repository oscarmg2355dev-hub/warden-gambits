package com.wardengambits.engine.story

import com.wardengambits.engine.model.Outcome

sealed interface StoryAction {
    data class CombatFinished(val outcome: Outcome, val nowMillis: Long) : StoryAction
    data class Choose(val option: Int) : StoryAction
    data object Continue : StoryAction
    data class StartNextLap(val nowMillis: Long) : StoryAction
}

enum class StoryRejectReason { WRONG_PHASE, INVALID_OPTION }
