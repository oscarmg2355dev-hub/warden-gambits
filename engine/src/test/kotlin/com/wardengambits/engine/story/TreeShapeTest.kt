package com.wardengambits.engine.story

import kotlin.test.Test
import kotlin.test.assertEquals

class TreeShapeTest {
    private fun n(level: Int, index: Int) = NodeId("bree", level, index)

    @Test
    fun routeHas24Zones() {
        assertEquals(24, Route.zones.size)
        assertEquals("bree", Route.zones.first().id)
        assertEquals(ZoneInfo("caradhras", "Caradhras"), Route.zones[9])
        assertEquals(ZoneInfo("lothlorien", "Lothlórien"), Route.zones[14])
        assertEquals(
            listOf("anduin", "argonath", "amonhen", "emynmuil", "cienagas", "ithilien", "cirithungol", "gorgoroth"),
            Route.zones.subList(15, 23).map { it.id },
        )
        assertEquals(ZoneInfo("orodruin", "Monte del Destino"), Route.zones.last())
        assertEquals(23, Route.treeZones.size)
        assertEquals("orodruin_final", NodeId.FINAL.key)
    }

    @Test
    fun keys() {
        assertEquals("bree_4_5", NodeId("bree", 4, 5).key)
        assertEquals("orodruin_final", NodeId.FINAL.key)
    }

    @Test
    fun finalKeyDerivesFromRoute() {
        assertEquals("${Route.FINAL_ZONE_ID}_final", NodeId.FINAL.key)
    }

    @Test
    fun restZones() {
        assertEquals(setOf("rivendel", "lothlorien"), Route.REST_ZONES)
    }

    @Test
    fun treeNodesIgnoreTheFinalZone() {
        assertEquals(22, TreeShape.treeNodes("orodruin").size)
        assertEquals(NodeId("orodruin", 1, 0), TreeShape.treeNodes("orodruin").first())
        assertEquals(
            listOf(NodeId("orodruin", 2, 0), NodeId("orodruin", 2, 1)),
            TreeShape.nextInZone(NodeId("orodruin", 1, 0)),
        )
        assertEquals(emptyList(), TreeShape.nextInZone(n(7, 0)))
    }

    @Test
    fun nodesPerZone() {
        assertEquals(22, TreeShape.nodesOf("moria").size)
        assertEquals(22, TreeShape.nodesOf("lothlorien").size)
        assertEquals(listOf(NodeId.FINAL), TreeShape.nodesOf("orodruin"))
    }

    @Test
    fun opening() {
        assertEquals(listOf(n(2, 0), n(2, 1)), TreeShape.destinations(n(1, 0)))
        assertEquals(listOf(n(4, 6), n(4, 7)), TreeShape.destinations(n(3, 3)))
    }

    @Test
    fun closingWrapsAndSorts() {
        assertEquals(listOf(n(5, 0), n(5, 3)), TreeShape.destinations(n(4, 7)))
        assertEquals(listOf(n(5, 1), n(5, 2)), TreeShape.destinations(n(4, 2)))
        assertEquals(listOf(n(6, 0), n(6, 1)), TreeShape.destinations(n(5, 2)))
    }

    @Test
    fun singleDestinations() {
        assertEquals(listOf(n(7, 0)), TreeShape.destinations(n(6, 1)))
        assertEquals(listOf(NodeId("chet", 1, 0)), TreeShape.destinations(n(7, 0)))
        assertEquals(listOf(NodeId("lothlorien", 1, 0)), TreeShape.destinations(NodeId("nimrodel", 7, 0)))
        assertEquals(listOf(NodeId.FINAL), TreeShape.destinations(NodeId("gorgoroth", 7, 0)))
        assertEquals(emptyList(), TreeShape.destinations(NodeId.FINAL))
    }

    @Test
    fun everyNodeReachableAndLevels1to5HaveTwoOptions() {
        val seen = mutableSetOf(NodeId.START)
        val queue = ArrayDeque(listOf(NodeId.START))
        while (queue.isNotEmpty()) {
            val node = queue.removeFirst()
            val dests = TreeShape.destinations(node)
            if (node.level in 1..5) assertEquals(2, dests.size, "node ${node.key}")
            dests.filter { it.zoneId == "bree" && seen.add(it) }.forEach(queue::add)
        }
        assertEquals(TreeShape.nodesOf("bree").toSet(), seen)
    }
}
