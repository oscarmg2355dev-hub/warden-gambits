package com.wardengambits.engine.story

/** Shape of every zone tree and the destination rules between nodes. */
object TreeShape {
    val LEVEL_SIZES = listOf(1, 2, 4, 8, 4, 2, 1)
    const val LEVELS = 7

    /** Number of nodes at [level] (1..7). */
    fun size(level: Int): Int = LEVEL_SIZES[level - 1]

    /** All nodes of a zone, by level then index. The final zone has only [NodeId.FINAL]. */
    fun nodesOf(zoneId: String): List<NodeId> =
        if (zoneId == Route.FINAL_ZONE_ID) listOf(NodeId.FINAL) else treeNodes(zoneId)

    /** The full 1-2-4-8-4-2-1 tree of [zoneId], whatever its place in the route (pending zones included). */
    fun treeNodes(zoneId: String): List<NodeId> =
        (1..LEVELS).flatMap { level -> (0 until size(level)).map { NodeId(zoneId, level, it) } }

    /** Next nodes, sorted by index and distinct; empty only for [NodeId.FINAL]. */
    fun destinations(node: NodeId): List<NodeId> {
        if (node == NodeId.FINAL) return emptyList()
        if (node.level == LEVELS) return listOf(firstNodeOfNextZone(node.zoneId))
        return nextInZone(node)
    }

    /** Next nodes inside the tree of [node]'s zone, sorted and distinct; empty at level 7. Ignores the route. */
    fun nextInZone(node: NodeId): List<NodeId> {
        if (node.level >= LEVELS) return emptyList()
        val current = size(node.level)
        val next = size(node.level + 1)
        val indices = if (next > current) {
            listOf(2 * node.index, 2 * node.index + 1)
        } else {
            val half = node.index / 2
            listOf(half, (half + 1) % next)
        }
        return indices.distinct().sorted().map { NodeId(node.zoneId, node.level + 1, it) }
    }

    private fun firstNodeOfNextZone(zoneId: String): NodeId {
        val nextId = Route.zones[Route.indexOf(zoneId) + 1].id
        return if (nextId == Route.FINAL_ZONE_ID) NodeId.FINAL else NodeId(nextId, 1, 0)
    }
}
