package com.wardengambits.engine.story

import kotlinx.serialization.Serializable

/** A node of a zone tree: `level` is 1..7, `index` is 0 until the level size. */
@Serializable
data class NodeId(val zoneId: String, val level: Int, val index: Int) {
    val key: String
        get() = if (this == FINAL) "${Route.FINAL_ZONE_ID}_final" else "${zoneId}_${level}_$index"

    companion object {
        val START = NodeId("bree", 1, 0)
        val FINAL = NodeId(Route.FINAL_ZONE_ID, 1, 0)
    }
}
