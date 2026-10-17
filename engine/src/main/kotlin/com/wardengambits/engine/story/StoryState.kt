package com.wardengambits.engine.story

import kotlinx.serialization.Serializable

@Serializable
data class StoryProfile(val run: RunState, val history: List<RunRecord>)

@Serializable
data class RunState(
    val level: Int,
    val lap: Int,
    val lapStartLevel: Int,
    val wins: Int,
    val node: NodeId,
    val path: List<NodeId>,
    val phase: Phase,
    /** Combat defeats in this lap; each one sent the Warden back to the previous node. */
    val deaths: Int = 0,
)

@Serializable
enum class Phase { COMBAT_PENDING, CHOOSING, LAP_COMPLETE }

@Serializable
data class RunRecord(
    val lap: Int,
    val node: NodeId,
    val level: Int,
    val wins: Int,
    val result: RunResult,
    val finishedAtMillis: Long,
    val deaths: Int = 0,
)

/** [DEFEATED] is only found in old saves: since 2026-10-13 a defeat no longer ends the run. */

@Serializable
enum class RunResult { DEFEATED, COMPLETED }

data class Choice(val destination: NodeId, val label: String?)

data class ZoneProgress(val zone: ZoneInfo, val level: Int, val total: Int)
