package com.wardengambits.engine.tutorial

import com.wardengambits.engine.event.CombatEvent
import com.wardengambits.engine.event.RejectReason
import com.wardengambits.engine.model.PLAYER_ID

private const val RECOVER_SEQUENCE = "321"

/** Progress through one [TutorialLesson]: current step, wrong casts so far and completion. */
data class TutorialProgress(
    val lessonId: String,
    val step: Int = 0,
    val misses: Int = 0,
    val completed: Boolean = false,
)

/** Applies the player's resolved combos in [events]: the requested one advances, any other counts a miss. */
fun TutorialProgress.advance(lesson: TutorialLesson, events: List<CombatEvent>): TutorialProgress {
    var progress = this
    for (event in events) {
        if (progress.completed) break
        when (event) {
            is CombatEvent.ComboRejected ->
                // A mistyped sequence is a miss; any other rejection (e.g. no power) is not.
                if (event.fighter == PLAYER_ID && event.reason == RejectReason.UNKNOWN_SEQUENCE) {
                    progress = progress.copy(misses = progress.misses + 1)
                }
            is CombatEvent.ComboResolved -> {
                if (event.fighter != PLAYER_ID) continue
                val sequence = event.combo.sequence
                progress = when {
                    sequence == lesson.steps[progress.step] -> {
                        val next = progress.step + 1
                        progress.copy(step = next, completed = next >= lesson.steps.size)
                    }
                    // Recuperar (power refill) is never a wrong cast.
                    sequence == RECOVER_SEQUENCE -> progress
                    else -> progress.copy(misses = progress.misses + 1)
                }
            }
            is CombatEvent.ComboQueued,
            is CombatEvent.EffectResolved,
            is CombatEvent.EffectApplied,
            is CombatEvent.EffectExpired,
            is CombatEvent.PotionUsed,
            is CombatEvent.FighterDefeated,
            is CombatEvent.CombatEnded,
            -> Unit
        }
    }
    return progress
}
