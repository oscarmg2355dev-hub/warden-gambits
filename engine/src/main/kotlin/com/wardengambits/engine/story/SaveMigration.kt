package com.wardengambits.engine.story

/**
 * Save migration for the road to Mordor (spec 2026-10-16 §4): Lothlórien stops being the end of the adventure and
 * becomes a rest zone whose first node is the old final node. Applied on load, before the profile is validated.
 */
object SaveMigration {
    private val OLD_FINAL = NodeId("lothlorien", 1, 0)

    /** A run finished at the old final node goes back to choosing there; any other profile is returned unchanged. */
    fun migrate(profile: StoryProfile): StoryProfile {
        val run = profile.run
        if (run.node != OLD_FINAL || run.phase != Phase.LAP_COMPLETE) return profile
        return profile.copy(run = run.copy(phase = Phase.CHOOSING))
    }
}
