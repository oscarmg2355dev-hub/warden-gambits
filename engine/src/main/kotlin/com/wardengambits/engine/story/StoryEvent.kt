package com.wardengambits.engine.story

sealed interface StoryEvent {
    data class NodeEntered(val node: NodeId) : StoryEvent
    data class ZoneEntered(val zoneId: String) : StoryEvent
    /** A lost fight at [from] sent the Warden back to [to]; followed by [NodeEntered] of [to]. */
    data class ReturnedToPreviousNode(val from: NodeId, val to: NodeId) : StoryEvent
    data class LeveledUp(val newLevel: Int) : StoryEvent
    data class RunEnded(val record: RunRecord) : StoryEvent
    data class LapStarted(val lap: Int) : StoryEvent
    data class StoryRejected(val reason: StoryRejectReason) : StoryEvent
}

data class StoryStep(val profile: StoryProfile, val events: List<StoryEvent>)
