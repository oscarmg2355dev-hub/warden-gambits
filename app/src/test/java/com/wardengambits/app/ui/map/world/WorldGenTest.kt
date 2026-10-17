package com.wardengambits.app.ui.map.world

import com.wardengambits.engine.story.Route
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.roundToInt

class WorldGenTest {
    private val ids = Route.zones.map { it.id }
    private val zc = WorldGen.ZONE_COLS
    private val world by lazy { WorldGen.generate(ids, 200) }

    private fun rgbDiff(a: Int, b: Int): Int =
        abs((a shr 16 and 0xFF) - (b shr 16 and 0xFF)) +
            abs((a shr 8 and 0xFF) - (b shr 8 and 0xFF)) +
            abs((a and 0xFF) - (b and 0xFF))

    private fun columnDiff(map: WorldMap, x: Int): Double {
        var sum = 0L
        for (y in 0 until map.height) sum += rgbDiff(map.pixels[y * map.width + x], map.pixels[y * map.width + x + 1])
        return sum.toDouble() / map.height
    }

    private fun waterSet(map: WorldMap): Set<Int> = map.water.toHashSet()

    private val world40 by lazy { WorldGen.generate(ids, 40) }

    private fun zoneColumns(id: String): IntRange = ids.indexOf(id).let { it * zc until (it + 1) * zc }

    @Test fun deterministic() {
        val other = WorldGen.generate(ids, 200)
        assertArrayEquals(world.pixels, other.pixels)
        assertArrayEquals(world.water, other.water)
        assertArrayEquals(world.lava, other.lava)
        assertEquals(world.wisps, other.wisps)
    }

    @Test fun parallelMatchesSequential() {
        for ((zones, rows) in listOf(ids to 197, listOf("durin", "moria", "sombrio") to 61, listOf("cienagas", "gorgoroth", "orodruin") to 53)) {
            val seq = WorldGen.generate(zones, rows, parallel = false)
            val par = WorldGen.generate(zones, rows, parallel = true)
            assertArrayEquals(seq.pixels, par.pixels)
            assertArrayEquals(seq.water, par.water)
            assertArrayEquals(seq.lava, par.lava)
            assertEquals(seq.chimneys, par.chimneys)
            assertEquals(seq.torches, par.torches)
            assertEquals(seq.mallorns, par.mallorns)
            assertEquals(seq.stops, par.stops)
            assertEquals(seq.wisps, par.wisps)
            assertEquals(seq.vents, par.vents)
        }
    }

    @Test fun widthIs56PerZone() {
        assertEquals(24, ids.size)
        assertEquals(24 * 56, world.width)
        assertEquals(200, world.height)
        assertEquals(world.width * world.height, world.pixels.size)
    }

    @Test fun noSeamsAtZoneBorders() {
        for (map in listOf(world, world40)) for (k in 1 until ids.size) {
            val border = zc * k - 1
            // Local baseline: the neighbouring column pairs, so dense high-contrast biomes don't hide or fake a seam.
            val baseline = (zc * k - 9..zc * k + 7).filter { it != border }.map { columnDiff(map, it) }.average()
            val diff = columnDiff(map, border)
            assertTrue("rows=${map.height} border $k (${ids[k - 1]}|${ids[k]}): $diff > 1.6 × $baseline", diff <= 1.6 * baseline)
        }
    }

    @Test fun lavaGlowsInGorgorothAndOrodruin() {
        for (id in listOf("gorgoroth", "orodruin")) {
            val cols = zoneColumns(id)
            val count = world.lava.count { it % world.width in cols }
            assertTrue("$id lava tiles: $count", count >= 40)
        }
        for (id in listOf("bree", "lothlorien", "anduin", "ithilien")) {
            assertTrue("no lava in $id", world.lava.none { it % world.width in zoneColumns(id) })
        }
    }

    @Test fun lavaNeverCoversTheRoad() {
        for (map in listOf(world, world40)) {
            for (i in map.lava) {
                val x = i % map.width
                val y = i / map.width
                val ry = WorldGen.roadY(x, map.height).roundToInt()
                assertTrue("rows=${map.height} lava on the road at x=$x y=$y", abs(y - ry) > 3)
            }
        }
    }

    @Test fun wispsFloatOverTheDeadMarshes() {
        val cols = zoneColumns("cienagas")
        assertTrue("wisps: ${world.wisps.size}", world.wisps.size >= 4)
        for (wisp in world.wisps) assertTrue("wisp $wisp outside the marshes", wisp.x in cols.first - 12..cols.last + 12)
        assertTrue(world.wisps.count { it.x in cols } >= world.wisps.size * 3 / 4)
    }

    @Test fun anduinIsAWideRiverCrossedByBoat() {
        val water = waterSet(world)
        val cols = zoneColumns("anduin")
        // A wide band of water along the zone: most columns have at least 8 water tiles.
        val wide = cols.count { x -> (0 until world.height).count { y -> y * world.width + x in water } >= 8 }
        assertTrue("wide columns: $wide", wide >= zc * 3 / 4)
        // The crossing: water right above and below the dry road band.
        val crossing = cols.filter { x ->
            val y0 = WorldGen.roadY(x, 200).roundToInt()
            (y0 - 6) * world.width + x in water && (y0 + 6) * world.width + x in water
        }
        assertTrue("boat crossing columns: ${crossing.size}", crossing.isNotEmpty())
    }

    @Test fun argonathStatuesFlankTheRiver() {
        val builder = WorldBuilder(ids, 200)
        val map = builder.build()
        val water = waterSet(map)
        val statues = map.spriteAnchors.filterIndexed { k, _ -> builder.spriteTypes[k] == WorldSprites.STATUE }
        assertEquals("statues: $statues", 2, statues.size)
        for (s in statues) assertTrue("statue $s outside the Argonath", s.x in zoneColumns("argonath"))
        val (west, east) = statues.sortedBy { it.x }
        val row = west.y - 14
        assertTrue("river between the statues", (west.x..east.x).any { x -> row * map.width + x in water })
    }

    @Test fun amonHenHasItsStoneSeat() {
        val builder = WorldBuilder(ids, 200)
        val map = builder.build()
        val seats = map.spriteAnchors.filterIndexed { k, _ -> builder.spriteTypes[k] == WorldSprites.SEAT }
        assertEquals(1, seats.size)
        assertTrue("seat ${seats[0]} outside Amon Hen", seats[0].x in zoneColumns("amonhen"))
    }

    @Test fun orodruinConeIsHighestAtTheCentreOfTheFinalZone() {
        for (rows in listOf(200, 40)) {
            val builder = WorldBuilder(ids, rows)
            val map = builder.build()
            val cols = zoneColumns("orodruin")
            var best = -1
            for (y in 0 until rows) for (x in cols) {
                val i = y * map.width + x
                if (best < 0 || builder.elevation[i] > builder.elevation[best]) best = i
            }
            val centre = cols.first + zc / 2
            assertTrue("rows=$rows highest point at x=${best % map.width}", abs(best % map.width - centre) <= 3)
            assertTrue("rows=$rows smoke vent", map.vents.any { abs(it.x - centre) <= 3 })
        }
    }

    @Test fun roadIsContinuous() {
        for (rows in listOf(40, 120, 200, 300)) {
            for (x in 0 until ids.size * zc - 1) {
                val step = abs(WorldGen.roadY(x + 1, rows) - WorldGen.roadY(x, rows))
                assertTrue("rows=$rows x=$x step=$step", step <= 2f)
            }
        }
    }

    @Test fun stopsOnRoadAndDry() {
        val water = waterSet(world)
        assertEquals(ids.size, world.stops.size)
        world.stops.forEachIndexed { i, stop ->
            assertEquals(i * zc + zc / 2, stop.x)
            assertTrue("stop $i off road", abs(stop.y - WorldGen.roadY(stop.x, 200)) <= 1f)
            assertFalse("stop $i in water", stop.y * world.width + stop.x in water)
        }
    }

    @Test fun riversCrossedByBridgeOrFord() {
        val water = waterSet(world)
        var crossings = 0
        for (x in 0 until world.width) {
            val y0 = WorldGen.roadY(x, 200).roundToInt()
            val near = (y0 - 4..y0 + 4).filter { it in 0 until world.height }
            // By design only the central road rows -1..+1 must be dry: ford stones on rows ±2 may leave water gaps.
            val roadRows = (y0 - 1..y0 + 1).filter { it in 0 until world.height }
            if (near.any { y -> y * world.width + x in water }) {
                crossings++
                for (y in roadRows) assertFalse("road in water at x=$x y=$y", y * world.width + x in water)
            }
        }
        assertTrue("expected the road to cross rivers", crossings >= 4)
    }

    @Test fun durinDoorVisibleAndClear() {
        val doorX = ids.indexOf("moria") * zc + 4
        val doorY = WorldGen.roadY(doorX, 200).roundToInt()
        var doorPixels = 0
        for (y in doorY - 1..doorY + 1) for (x in doorX - 8..doorX + 8) {
            val p = world.pixels[y * world.width + x]
            if (abs((p shr 16 and 0xFF) - 236) <= 12 && abs((p shr 8 and 0xFF) - 230) <= 12 && abs((p and 0xFF) - 200) <= 12) {
                doorPixels++
            }
        }
        assertTrue("door pixels: $doorPixels", doorPixels >= 6)
        assertTrue(world.spriteAnchors.isNotEmpty())
        for (a in world.spriteAnchors) {
            assertTrue("sprite at $a too close to the door", max(abs(a.x - doorX), abs(a.y - doorY)) >= 10)
        }
    }

    @Test fun lakesExist() {
        val water = waterSet(world)
        fun wetNear(cx: Int, cy: Int) = (cy - 2..cy + 2).any { y -> (cx - 2..cx + 2).any { x -> y * world.width + x in water } }
        assertTrue("Durin lake", wetNear(ids.indexOf("durin") * zc + 20, (200 * 0.72).roundToInt()))
        assertTrue("Mirrormere", wetNear(ids.indexOf("sombrio") * zc + 32, (200 * 0.68).roundToInt()))
    }

    @Test fun extremeHeights() {
        for (rows in listOf(40, 300)) {
            val map = WorldGen.generate(ids, rows)
            assertEquals(rows, map.height)
            for (stop in map.stops) assertTrue("rows=$rows stop=$stop", stop.y in 0 until rows)
            for (x in 0 until map.width - 1) {
                assertTrue(abs(WorldGen.roadY(x + 1, rows) - WorldGen.roadY(x, rows)) <= 2f)
            }
        }
    }

    @Test fun spriteFootprintsStayOffTheRoad() {
        for (rows in listOf(200, 40)) {
            val builder = WorldBuilder(ids, rows)
            val map = builder.build()
            val ext = IntArray(3)
            assertEquals(map.spriteAnchors.size, builder.spriteTypes.size)
            map.spriteAnchors.forEachIndexed { k, a ->
                val type = builder.spriteTypes[k]
                WorldSprites.extent(type, WorldSprites.scaleOf(type, a.x, a.y), ext)
                for (x in max(0, a.x - ext[0])..minOf(map.width - 1, a.x + ext[0])) {
                    val ry = WorldGen.roadY(x, rows).roundToInt()
                    val overlaps = a.y - ext[1] <= ry + 3 && a.y + ext[2] >= ry - 3
                    assertFalse("rows=$rows sprite $type at $a reaches the road at x=$x (road row $ry)", overlaps)
                }
            }
        }
    }

    @Test fun spritesNeverPaintOverTheRoad() {
        for (rows in listOf(200, 40)) {
            val with = WorldBuilder(ids, rows).build()
            val without = WorldBuilder(ids, rows, drawSprites = false).build()
            for (x in 0 until with.width) {
                val ry = WorldGen.roadY(x, rows).roundToInt()
                for (y in ry - 3..ry + 3) {
                    if (y !in 0 until rows) continue
                    val i = y * with.width + x
                    assertEquals("rows=$rows road pixel covered at x=$x y=$y (road row $ry)", without.pixels[i], with.pixels[i])
                }
            }
        }
    }

    @Test fun unknownZoneFallsBackToMeadow() {
        val map = WorldGen.generate(listOf("bree", "zzz"), 120)
        assertEquals(112, map.width)
        assertEquals(2, map.stops.size)
    }

    /** Eyeballing aid: RENDER_WORLD=1 ./gradlew :app:testDebugUnitTest --tests "*WorldGenTest*" */
    @Test fun renderWorldWhenAsked() {
        if (System.getenv("RENDER_WORLD") == null) return
        val start = System.nanoTime()
        val map = WorldGen.generate(ids, 200)
        val ms = (System.nanoTime() - start) / 1_000_000
        val scale = 2
        val perRow = 5 * zc
        val strips = (map.width + perRow - 1) / perRow
        val gap = 8
        val img = BufferedImage(perRow * scale, strips * (map.height * scale + gap), BufferedImage.TYPE_INT_RGB)
        for (y in 0 until map.height) for (x in 0 until map.width) {
            val strip = x / perRow
            val px = (x % perRow) * scale
            val py = strip * (map.height * scale + gap) + y * scale
            for (dy in 0 until scale) for (dx in 0 until scale) img.setRGB(px + dx, py + dy, map.pixels[y * map.width + x])
        }
        val out = File("../.superpowers/sdd/road-to-mordor/world-render.png")
        ImageIO.write(img, "png", out)
        println("WorldGen.generate(${ids.size} zones, 200 rows) took $ms ms; wrote ${out.canonicalPath}")
    }
}
