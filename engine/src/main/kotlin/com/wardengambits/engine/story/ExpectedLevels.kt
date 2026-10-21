package com.wardengambits.engine.story

import com.wardengambits.engine.story.content.StoryContent
import kotlin.math.roundToInt

/**
 * Expected player level when arriving at a node: 1 + expected wins before it. All 32 paths of a zone are
 * equally likely (uniform choices). Wins before = sum over previous zones of the average encounters per
 * path + average encounters at lower levels of this zone over the paths that go through the node.
 */
class ExpectedLevels(private val content: StoryContent) {
    private val pathsByZone: Map<String, List<List<NodeId>>> =
        Route.treeZones.associate { it.id to paths(NodeId(it.id, 1, 0)) }

    fun hasEncounter(id: NodeId): Boolean = content.node(id)?.encounter != null

    fun averageEncountersPerPath(zoneId: String): Double =
        pathsByZone.getValue(zoneId).map { path -> path.count(::hasEncounter) }.average()

    fun expectedWinsBefore(node: NodeId): Double {
        val zoneIndex = Route.indexOf(node.zoneId)
        val previous = Route.treeZones.take(zoneIndex).sumOf { averageEncountersPerPath(it.id) }
        val through = pathsByZone.getValue(node.zoneId).filter { node in it }
        val within = through.map { path -> path.count { it.level < node.level && hasEncounter(it) } }.average()
        return previous + within
    }

    fun expectedLevel(node: NodeId): Int = 1 + expectedWinsBefore(node).roundToInt()

    private fun paths(from: NodeId): List<List<NodeId>> {
        if (from.level == TreeShape.LEVELS) return listOf(listOf(from))
        return TreeShape.destinations(from).flatMap { next -> paths(next).map { listOf(from) + it } }
    }
}
