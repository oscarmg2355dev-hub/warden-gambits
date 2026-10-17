package com.wardengambits.app.ui.map

import com.wardengambits.app.ui.map.world.Tile
import com.wardengambits.app.ui.map.world.WorldGen
import com.wardengambits.app.ui.map.world.WorldMap
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class MapWorldDiskCacheTest {
    @get:Rule
    val folder = TemporaryFolder()

    private val ids = listOf("durin", "moria", "lothlorien")

    private fun assertSameWorld(expected: WorldMap, actual: WorldMap) {
        assertEquals(expected.width, actual.width)
        assertEquals(expected.height, actual.height)
        assertArrayEquals(expected.pixels, actual.pixels)
        assertArrayEquals(expected.water, actual.water)
        assertEquals(expected.chimneys, actual.chimneys)
        assertEquals(expected.torches, actual.torches)
        assertEquals(expected.mallorns, actual.mallorns)
        assertEquals(expected.stops, actual.stops)
        assertEquals(expected.spriteAnchors, actual.spriteAnchors)
        assertArrayEquals(expected.lava, actual.lava)
        assertEquals(expected.wisps, actual.wisps)
        assertEquals(expected.vents, actual.vents)
    }

    @Test
    fun savedWorldReloadsIdentical() {
        val world = WorldGen.generate(ids, 60)
        WorldDiskCache(folder.root).save(ids, 60, world)
        val loaded = WorldDiskCache(folder.root).load(ids, 60)
        assertNotNull(loaded)
        assertSameWorld(world, loaded!!)
    }

    @Test
    fun savedMordorWorldKeepsLavaWispsAndVents() {
        val mordor = listOf("cienagas", "gorgoroth", "orodruin")
        val world = WorldGen.generate(mordor, 120)
        assertTrue(world.lava.isNotEmpty() && world.wisps.isNotEmpty() && world.vents.isNotEmpty())
        WorldDiskCache(folder.root).save(mordor, 120, world)
        val loaded = WorldDiskCache(folder.root).load(mordor, 120)
        assertNotNull(loaded)
        assertSameWorld(world, loaded!!)
    }

    @Test
    fun otherVersionRowsOrZonesMiss() {
        val world = WorldGen.generate(ids, 60)
        WorldDiskCache(folder.root).save(ids, 60, world)
        assertNull(WorldDiskCache(folder.root, version = WorldGen.WORLD_GEN_VERSION + 1).load(ids, 60))
        assertNull(WorldDiskCache(folder.root).load(ids, 61))
        assertNull(WorldDiskCache(folder.root).load(listOf("durin", "moria"), 60))
    }

    @Test
    fun corruptFileMissesAndIsRemoved() {
        val store = WorldDiskCache(folder.root)
        store.save(ids, 60, WorldGen.generate(ids, 60))
        val file = folder.root.listFiles()!!.single()
        file.writeBytes(file.readBytes().copyOf(100))
        assertNull(store.load(ids, 60))
        assertEquals(0, folder.root.listFiles()!!.size)
    }

    @Test
    fun outOfRangeContentIsRejectedAndRemoved() {
        val good = WorldGen.generate(ids, 60)
        val size = good.width * good.height
        val bad = listOf(
            WorldMap(good.width, good.height, good.pixels, intArrayOf(0, size), good.chimneys, good.torches, good.mallorns, good.stops),
            WorldMap(good.width, good.height, good.pixels, intArrayOf(-1), good.chimneys, good.torches, good.mallorns, good.stops),
            WorldMap(good.width, good.height, good.pixels, good.water, listOf(Tile(good.width, 0)), good.torches, good.mallorns, good.stops),
            WorldMap(good.width, good.height, good.pixels, good.water, good.chimneys, listOf(Tile(0, -1)), good.mallorns, good.stops),
            WorldMap(good.width, good.height, good.pixels, good.water, good.chimneys, good.torches, listOf(Tile(0, good.height)), good.stops),
            WorldMap(good.width, good.height, good.pixels, good.water, good.chimneys, good.torches, good.mallorns, listOf(Tile(-1, 0))),
            WorldMap(0, 0, IntArray(0), IntArray(0), emptyList(), emptyList(), emptyList(), emptyList()),
            WorldMap(good.width, good.height, good.pixels, good.water, good.chimneys, good.torches, good.mallorns, good.stops, lava = intArrayOf(size)),
            WorldMap(good.width, good.height, good.pixels, good.water, good.chimneys, good.torches, good.mallorns, good.stops, wisps = listOf(Tile(0, good.height))),
            WorldMap(good.width, good.height, good.pixels, good.water, good.chimneys, good.torches, good.mallorns, good.stops, vents = listOf(Tile(-1, 0))),
        )
        for ((k, world) in bad.withIndex()) {
            val store = WorldDiskCache(folder.root)
            store.save(ids, 60, world)
            assertEquals("case $k saved", 1, folder.root.listFiles()!!.size)
            assertNull("case $k loaded", store.load(ids, 60))
            assertEquals("case $k not removed", 0, folder.root.listFiles()!!.size)
        }
    }

    @Test
    fun keepsAtMostTwoWorldsAndDropsOldVersions() {
        val old = WorldDiskCache(folder.root, version = WorldGen.WORLD_GEN_VERSION - 1)
        old.save(ids, 40, WorldGen.generate(ids, 40))
        val store = WorldDiskCache(folder.root)
        for (rows in 40..42) store.save(ids, rows, WorldGen.generate(ids, rows))
        val names = folder.root.listFiles()!!.map { it.name }.toSet()
        assertEquals(2, names.size)
        assertNotNull(store.load(ids, 42))
    }
}
