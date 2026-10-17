package com.wardengambits.engine.story.content

import com.wardengambits.engine.story.NodeId
import kotlinx.serialization.Serializable

@Serializable
data class RivalContent(
    val name: String,
    /** Portrait key (content spec: 12 common types or `boss_<zone>`); required in the JSON. */
    val portrait: String,
    val levelOffset: Int = 0,
    val pattern: List<String>,
    val everyNTicks: Int = 1,
    /** HP reaction (rival-healing spec §2); null for rivals that never react. */
    val reaction: ReactionContent? = null,
    /** Boss attack and defense modes (boss-modes spec §1); null for rivals without modes. */
    val modes: ModesContent? = null,
)

/** JSON form of [com.wardengambits.engine.model.Reaction]: HP thresholds (descending %), optional shield, combos. */
@Serializable
data class ReactionContent(val atHpPct: List<Int>, val shield: String? = null, val combos: List<String>)

/** JSON form of [com.wardengambits.engine.ai.BossModes]. */
@Serializable
data class ModesContent(val defenseBelowPct: Int, val attackAbovePct: Int, val maxDefenses: Int, val defense: List<String>)

@Serializable
data class EncounterContent(val rivals: List<RivalContent>)

@Serializable
data class NodeContent(
    val level: Int,
    val index: Int,
    val title: String,
    val text: String,
    val imagePrompt: String,
    val choiceLabel: String? = null,
    val encounter: EncounterContent? = null,
)

@Serializable
data class ZoneContent(
    val id: String,
    val name: String,
    val baseLevel: Int,
    val nodes: List<NodeContent>,
)

/** All zone contents of the story, with lookups by zone id and node id. */
class StoryContent(val zones: List<ZoneContent>) {
    fun zone(id: String): ZoneContent? = zones.firstOrNull { it.id == id }

    fun node(id: NodeId): NodeContent? =
        zone(id.zoneId)?.nodes?.firstOrNull { it.level == id.level && it.index == id.index }
}
