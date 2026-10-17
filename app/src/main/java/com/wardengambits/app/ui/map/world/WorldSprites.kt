package com.wardengambits.app.ui.map.world

import com.wardengambits.app.ui.map.world.WorldNoise.h01
import com.wardengambits.app.ui.map.world.WorldNoise.valueNoise
import com.wardengambits.app.ui.map.world.ZoneKeys.ASH
import com.wardengambits.app.ui.map.world.ZoneKeys.BIGRIVER
import com.wardengambits.app.ui.map.world.ZoneKeys.CLIFFS
import com.wardengambits.app.ui.map.world.ZoneKeys.DEADMARSH
import com.wardengambits.app.ui.map.world.ZoneKeys.FARM
import com.wardengambits.app.ui.map.world.ZoneKeys.FOREST
import com.wardengambits.app.ui.map.world.ZoneKeys.GOLDEN
import com.wardengambits.app.ui.map.world.ZoneKeys.GREYROCK
import com.wardengambits.app.ui.map.world.ZoneKeys.HERBS
import com.wardengambits.app.ui.map.world.ZoneKeys.HILLRUINS
import com.wardengambits.app.ui.map.world.ZoneKeys.HILLS
import com.wardengambits.app.ui.map.world.ZoneKeys.HOLLY
import com.wardengambits.app.ui.map.world.ZoneKeys.LAVA
import com.wardengambits.app.ui.map.world.ZoneKeys.MARSH
import com.wardengambits.app.ui.map.world.ZoneKeys.MEADOW
import com.wardengambits.app.ui.map.world.ZoneKeys.MORGUL
import com.wardengambits.app.ui.map.world.ZoneKeys.MOUNT
import com.wardengambits.app.ui.map.world.ZoneKeys.ROCK
import com.wardengambits.app.ui.map.world.ZoneKeys.SNOW
import com.wardengambits.app.ui.map.world.ZoneKeys.UNDER
import com.wardengambits.app.ui.map.world.ZoneKeys.VALLEY
import kotlin.math.ceil
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

/** Procedural, shaded sprites (light from the north-west, shadow cast to the south-east). */
internal class WorldSprites(private val c: WorldCanvas) {

    companion object {
        const val TREE = 0
        const val OAK = 1
        const val PINE = 2
        const val HOUSE = 3
        const val ROCK_S = 4
        const val BOULDER = 5
        const val REEDS = 6
        const val DEAD = 7
        const val RUINS = 8
        const val HOLLY_S = 9
        const val PEAK = 10
        const val PILLAR = 11
        const val MALLORN = 12
        const val CROP = 13
        const val STATUE = 14
        const val SEAT = 15
        const val FLOWERS = 16
        const val SPIRE = 17
        const val CRAG = 18
        const val CINDER = 19

        /** Chance that a grid cell of each biome holds a sprite. */
        val DENSITY = floatArrayOf(
            0.55f, 0.35f, 0.9f, 0.5f, 0.4f, 0.45f, 0.55f, 0.6f, 0.35f, 0.85f, 0.6f, 0.6f,
            0.45f, 0.45f, 0.6f, 0.55f, 0.45f, 0.7f, 0.45f, 0.25f, 0.15f,
        )

        /** Which sprite a biome gets for a uniform roll [r]; ruins are 7 % and dead trees 10 % of their biomes. */
        fun pick(biome: Int, r: Float): Int = when (biome) {
            FARM -> if (r < 0.25f) HOUSE else if (r < 0.5f) CROP else if (r < 0.8f) TREE else ROCK_S
            MEADOW -> if (r < 0.55f) TREE else if (r < 0.75f) OAK else ROCK_S
            FOREST -> if (r < 0.55f) OAK else if (r < 0.9f) PINE else ROCK_S
            MARSH -> if (r < 0.75f) REEDS else if (r < 0.85f) DEAD else ROCK_S
            HILLS -> if (r < 0.4f) BOULDER else if (r < 0.47f) RUINS else if (r < 0.85f) TREE else ROCK_S
            ROCK -> if (r < 0.55f) BOULDER else if (r < 0.9f) PINE else DEAD
            SNOW, MOUNT -> if (r < 0.6f) PEAK else BOULDER
            UNDER -> if (r < 0.8f) PILLAR else ROCK_S
            GOLDEN -> if (r < 0.75f) MALLORN else OAK
            HOLLY -> if (r < 0.55f) HOLLY_S else if (r < 0.62f) RUINS else OAK
            VALLEY -> if (r < 0.5f) PINE else if (r < 0.85f) OAK else HOUSE
            BIGRIVER -> if (r < 0.4f) TREE else if (r < 0.65f) OAK else if (r < 0.85f) REEDS else ROCK_S
            CLIFFS -> if (r < 0.45f) CRAG else if (r < 0.75f) BOULDER else if (r < 0.92f) PINE else ROCK_S
            HILLRUINS -> if (r < 0.4f) OAK else if (r < 0.62f) PINE else if (r < 0.8f) RUINS else BOULDER
            GREYROCK -> if (r < 0.5f) CRAG else if (r < 0.85f) BOULDER else ROCK_S
            DEADMARSH -> if (r < 0.45f) DEAD else if (r < 0.85f) REEDS else CINDER
            HERBS -> if (r < 0.35f) FLOWERS else if (r < 0.6f) TREE else if (r < 0.85f) OAK else if (r < 0.93f) RUINS else PINE
            MORGUL -> if (r < 0.55f) SPIRE else if (r < 0.85f) CINDER else DEAD
            ASH -> if (r < 0.45f) CINDER else if (r < 0.75f) DEAD else SPIRE
            LAVA -> if (r < 0.7f) CINDER else SPIRE
            else -> TREE
        }

        /** Size factor of a sprite anchored at (x, y). */
        fun scaleOf(type: Int, x: Int, y: Int): Float = when (type) {
            PEAK -> 0.9f + h01(x, y, 5) * 0.9f
            SPIRE, CRAG -> 0.6f + h01(x, y, 7) * 0.6f
            else -> 0.8f + h01(x, y, 6) * 0.5f
        }

        /** Rows of clearance the road keeps above a sprite of this type. */
        fun roadClearance(type: Int): Int = when (type) {
            PEAK -> 18
            SPIRE, CRAG -> 14
            HOUSE, MALLORN -> 9
            else -> 6
        }

        /**
         * Conservative footprint (shadow included) relative to the anchor, written into [out] as
         * (half width, rows above, rows below).
         */
        fun extent(type: Int, s: Float, out: IntArray) {
            fun set(hw: Float, up: Float, down: Float) {
                out[0] = ceil(hw).toInt(); out[1] = ceil(up).toInt(); out[2] = ceil(down).toInt()
            }
            when (type) {
                TREE -> set(4 * s + 3, 7.2f * s + 1, 1.5f * s + 2)
                OAK -> set(5 * s + 3, 9.2f * s + 1, 2 * s + 2)
                PINE -> set(3.4f * s + 3, 9 * s + 3, 1.2f * s + 2)
                HOUSE -> set(8f, 13f, 3f)
                ROCK_S -> set(3 * s + 3, 3.2f * s + 1, 1.2f * s + 2)
                BOULDER -> set(4 * s + 3, 4.6f * s + 1, 1.5f * s + 2)
                REEDS -> set(3f, 8f, 1f)
                DEAD -> set(5f, 7f, 2f)
                RUINS -> set(8f, 9f, 3f)
                HOLLY_S -> set(6f, 7f, 3f)
                PEAK -> set(12 * s + 4, 14 * s + 1, 2 * s + 2)
                PILLAR -> set(5f, 11f, 2f)
                MALLORN -> set(6 * s + 3, 14 * s + 1, 2 * s + 2)
                STATUE -> set(10f, 28f, 3f)
                SEAT -> set(8f, 11f, 3f)
                SPIRE -> set(6 * s + 5, 16 * s + 1, 2 * s + 2)
                CRAG -> set(8 * s + 5, 10 * s + 1, 2 * s + 2)
                CINDER -> set(3 * s + 3, 3.2f * s + 1, 1.2f * s + 2)
                else -> set(7f, 5f, 1f)
            }
        }

        private const val STONE = 0x948E80
        private val FLOWER_COLOURS = intArrayOf(0xE8D040, 0xF0EEE0, 0xC060A0, 0xE07848)

        private const val LX = -0.6f
        private const val LY = -0.8f
    }

    /** Draws a sprite; returns the chimney tile of a house, or null. */
    fun draw(type: Int, x: Int, y: Int, s: Float): Tile? {
        val fx = x.toFloat()
        val fy = y.toFloat()
        when (type) {
            TREE -> {
                shadow(fx, fy, 4 * s, 1.5f * s); rect(fx - 1, fy - 3 * s, 2f, 3 * s + 1, 0x56381E)
                blob(fx, fy - 4 * s, 3.2f * s, MEADOW)
            }
            OAK -> {
                shadow(fx, fy, 5 * s, 2 * s); rect(fx - 1, fy - 3 * s, 2f, 3 * s + 1, 0x50341C)
                blob(fx, fy - 5 * s, 4.2f * s, FOREST)
            }
            PINE -> pine(x, y, s)
            HOUSE -> return house(x, y)
            ROCK_S -> { shadow(fx, fy, 3 * s, 1.2f * s); blob(fx, fy - 1.5f * s, 2.4f * s, ROCK, 0.7f) }
            BOULDER -> { shadow(fx, fy, 4 * s, 1.5f * s); blob(fx, fy - 2 * s, 3.4f * s, ROCK, 0.75f) }
            REEDS -> reeds(x, y)
            DEAD -> dead(x, y)
            RUINS -> ruins(x, y)
            HOLLY_S -> holly(x, y)
            PEAK -> peak(x, y, s)
            PILLAR -> pillar(x, y)
            MALLORN -> mallorn(x, y, s)
            STATUE -> statue(x, y)
            SEAT -> seat(x, y)
            FLOWERS -> flowers(x, y)
            SPIRE -> pointed(x, y, 16 * s, 5 * s, MORGUL)
            CRAG -> pointed(x, y, 10 * s, 8 * s, GREYROCK)
            CINDER -> { shadow(fx, fy, 3 * s, 1.2f * s); blob(fx, fy - 1.5f * s, 2.4f * s, LAVA, 0.7f) }
            else -> crop(x, y)
        }
        return null
    }

    private fun shadow(cx: Float, cy: Float, rx: Float, ry: Float) {
        var y = -ry
        while (y <= ry) {
            var x = -rx
            while (x <= rx) {
                val nx = x / rx
                val ny = y / ry
                if (nx * nx + ny * ny <= 1f) {
                    val px = c.round(cx + x + 2)
                    val py = c.round(cy + y + 1)
                    if (c.inside(px, py) && c.kind[py * c.width + px].toInt() != WorldCanvas.SPRITE) c.scale(px, py, 0.62f)
                }
                x += 1f
            }
            y += 1f
        }
    }

    /** A shaded, leafy sphere: the canopy of trees and the body of rocks. */
    private fun blob(cx: Float, cy: Float, r: Float, palette: Int, squash: Float = 1f) {
        var y = -r
        while (y <= r) {
            var x = -r
            while (x <= r) {
                val nx = x / r
                val ny = y / (r * squash)
                val d = nx * nx + ny * ny
                val edge = 0.85f + 0.3f * valueNoise((cx + x) / 1.7f, (cy + y) / 1.7f, 71)
                if (d <= edge) {
                    val nz = sqrt(max(0f, 1 - d))
                    val lit = (-(nx * LX + ny * LY) * 0.6f + nz * 0.55f +
                        0.1f * (valueNoise((cx + x) / 1.3f, (cy + y) / 1.3f, 72) - 0.5f)).coerceIn(0f, 1f)
                    Ramps.sample(palette, lit, c.tmp)
                    c.putTmp(cx + x, cy + y, 1f, WorldCanvas.SPRITE)
                }
                x += 1f
            }
            y += 1f
        }
    }

    private fun rect(x0: Float, y0: Float, w: Float, h: Float, rgb: Int, f: Float = 1f) {
        var y = 0f
        while (y < h) {
            var x = 0f
            while (x < w) {
                c.put(x0 + x, y0 + y, rgb, f, WorldCanvas.SPRITE)
                x += 1f
            }
            y += 1f
        }
    }

    private fun px(x: Int, y: Int, rgb: Int, f: Float = 1f) = c.put(x.toFloat(), y.toFloat(), rgb, f, WorldCanvas.SPRITE)

    private fun pine(x: Int, y: Int, s: Float) {
        shadow(x.toFloat(), y.toFloat(), 3 * s, 1.2f * s)
        rect(x.toFloat(), y - 2f, 1f, 3f, 0x462C18)
        var l = 0
        while (l < 9 * s) {
            val w = (l / (9 * s) * 3.4f * s).roundToInt()
            for (i in -w..w) {
                val lit = (0.55f - (i.toFloat() / (w + 1)) * 0.5f + 0.15f * (valueNoise((x + i).toFloat(), (y - l).toFloat(), 5) - 0.5f))
                    .coerceIn(0f, 1f)
                Ramps.sample(FOREST, lit * 0.9f + 0.05f, c.tmp)
                c.putTmp((x + i).toFloat(), y - 2 - 9 * s + l, 1f, WorldCanvas.SPRITE)
            }
            l++
        }
    }

    private fun house(x: Int, y: Int): Tile {
        shadow(x + 1f, y.toFloat(), 6f, 2f)
        val wall = 0xD8C8A0
        val roof = 0xAA402C
        rect(x - 5f, y - 6f, 11f, 7f, wall)
        for (yy in 0 until 7) px(x + 5, y - 6 + yy, wall, 0.75f)
        rect(x - 1f, y - 3f, 2f, 4f, 0x5A3A1E)
        rect(x - 4f, y - 5f, 2f, 2f, 0xF0D278)
        rect(x + 2f, y - 5f, 2f, 2f, 0xF0D278)
        for (r in 0 until 6) for (i in -6 + r..6 - r) px(x + i, y - 7 - r, roof, if (i < 0) 1.1f else 0.8f)
        rect(x + 3f, y - 12f, 2f, 4f, 0x6E605A)
        return Tile(x + 4, y - 13)
    }

    private fun reeds(x: Int, y: Int) {
        for (i in -2..2 step 2) {
            val h = 4 + (h01(x + i, y, 1) * 3).roundToInt()
            for (k in 0 until h) {
                Ramps.sample(MARSH, 0.5f + k.toFloat() / h * 0.4f, c.tmp)
                c.putTmp((x + i).toFloat(), (y - k).toFloat(), 1f, WorldCanvas.SPRITE)
            }
            px(x + i, y - h, 0x785028)
        }
    }

    private fun dead(x: Int, y: Int) {
        shadow(x.toFloat(), y.toFloat(), 3f, 1f)
        val bark = 0x46382A
        for (k in 0 until 7) px(x, y - k, bark)
        px(x - 1, y - 5, bark); px(x - 2, y - 6, bark); px(x + 1, y - 4, bark); px(x + 2, y - 5, bark)
    }

    private fun ruins(x: Int, y: Int) {
        shadow(x.toFloat(), y.toFloat(), 5f, 2f)
        val st = 0x968E7C
        rect(x - 5f, y - 1f, 11f, 2f, st, 0.8f)
        rect(x - 5f, y - 7f, 2f, 6f, st)
        rect(x + 3f, y - 5f, 2f, 4f, st)
        rect(x - 1f, y - 9f, 2f, 8f, st)
        for (k in 0 until 8) px(x, y - 1 - k, st, 0.75f)
    }

    private fun holly(x: Int, y: Int) {
        shadow(x.toFloat(), y.toFloat(), 4f, 1.5f)
        blob(x.toFloat(), y - 3f, 3.2f, HOLLY)
        for (k in 0 until 4) {
            val a = h01(x, y, k) * 6.28f
            px((x + cos(a) * 2).roundToInt(), (y - 3 + sin(a) * 2).roundToInt(), 0xC8281E)
        }
    }

    private fun peak(x: Int, y: Int, s: Float) {
        val h = (14 * s).roundToInt()
        val w = (12 * s).roundToInt()
        shadow(x + 3f, y.toFloat(), w.toFloat(), 2 * s)
        for (r in 0 until h) {
            val half = (w * (r + 1).toFloat() / h).roundToInt()
            val t = r.toFloat() / h
            for (i in -half..half) {
                val side = if (i < 0) 1.15f else 0.7f
                val snowLine = 0.38f + 0.18f * valueNoise((x + i).toFloat(), y.toFloat(), 8)
                if (t < snowLine) {
                    Ramps.sample(SNOW, 0.55f + 0.4f * (1 - t), c.tmp)
                } else {
                    Ramps.sample(MOUNT, 0.35f + 0.3f * valueNoise((x + i) / 2f, (y - h + r) / 2f, 9), c.tmp)
                }
                c.putTmp((x + i).toFloat(), (y - h + r).toFloat(), side, WorldCanvas.SPRITE)
            }
        }
    }

    private fun pillar(x: Int, y: Int) {
        shadow(x.toFloat(), y.toFloat(), 3f, 1f)
        val st = 0x4E4856
        rect(x - 2f, y - 10f, 4f, 10f, st)
        for (k in 0 until 10) px(x + 1, y - 10 + k, st, 0.7f)
        rect(x - 3f, y - 11f, 6f, 1f, st, 1.2f)
        rect(x - 3f, y.toFloat(), 6f, 1f, st, 0.8f)
    }

    private fun mallorn(x: Int, y: Int, s: Float) {
        shadow(x.toFloat(), y.toFloat(), 6 * s, 2 * s)
        val trunk = 0xDCD6C4
        var k = 0
        while (k < 7 * s) {
            px(x, y - k, trunk); px(x + 1, y - k, trunk, 0.75f)
            k++
        }
        blob(x.toFloat(), y - 9 * s, 5 * s, GOLDEN, 0.85f)
        blob(x - 3 * s, y - 7 * s, 3 * s, GOLDEN)
        blob(x + 3 * s, y - 7 * s, 3 * s, GOLDEN)
    }

    /** A jagged rock without snow: Morgul spires and the crags of the Emyn Muil and the Argonath. */
    private fun pointed(x: Int, y: Int, height: Float, width: Float, palette: Int) {
        val h = height.roundToInt()
        shadow(x + 2f, y.toFloat(), width, 1.5f)
        for (r in 0 until h) {
            val t = (r + 1).toFloat() / h
            val half = (width * t + (valueNoise(x.toFloat(), (y - h + r) / 2f, 13) - 0.5f) * 2f * t).roundToInt()
            for (i in -half..half) {
                val side = if (i < 0) 1.1f else 0.7f
                Ramps.sample(palette, 0.3f + 0.45f * valueNoise((x + i) / 2f, (y - h + r) / 2f, 14), c.tmp)
                c.putTmp((x + i).toFloat(), (y - h + r).toFloat(), side, WorldCanvas.SPRITE)
            }
        }
    }

    /**
     * One of the Argonath: a crowned stone king on a plinth with the left hand raised palm outward
     * in warning and an axe in the right.
     */
    private fun statue(x: Int, y: Int) {
        shadow(x + 2f, y.toFloat(), 8f, 2f)
        fun st(dx: Int, dy: Int, f: Float = 1f) {
            val v = 0.92f + 0.16f * valueNoise((x + dx) / 2f, (y + dy) / 2f, 15)
            c.put((x + dx).toFloat(), (y + dy).toFloat(), STONE, f * v * (if (dx < 0) 1.08f else 0.8f), WorldCanvas.SPRITE)
        }
        // Plinth: courses of blocks.
        for (dy in -5..0) for (dx in -6..6) st(dx, dy, if (dy == -5) 1.15f else if ((dx + dy * 3) % 5 == 0) 0.7f else 0.9f)
        // Robe and legs.
        for (dy in -19..-6) {
            val half = 3 + (dy + 19) / 7
            for (dx in -half..half) st(dx, dy, if (dx == 0 && dy > -11) 0.65f else 1f)
        }
        // Shoulders, then the helm with its crown.
        for (dx in -4..4) { st(dx, -20); st(dx, -21) }
        for (dy in -25..-22) for (dx in -2..2) st(dx, dy)
        st(-1, -24, 0.55f); st(1, -24, 0.55f) // eyes under the helm
        for (dx in -2..2 step 2) st(dx, -26, 1.2f)
        // Left arm raised, palm out.
        for (dy in -24..-20) st(-5, dy)
        for (dy in -27..-25) for (dx in -7..-5) st(dx, dy, 1.15f)
        // Right arm down along the body, holding an axe.
        for (dy in -20..-12) st(5, dy, 0.9f)
        for (dy in -24..-8) st(6, dy, 0.7f)
        for (dy in -24..-21) { st(7, dy, 0.85f); st(8, dy - 1, 0.75f) }
    }

    /** The Seat of Seeing on Amon Hen: a stone platform with steps, pillars and a high-backed chair. */
    private fun seat(x: Int, y: Int) {
        shadow(x + 1f, y.toFloat(), 7f, 2f)
        val pale = 0xBCB4A0
        for (dx in -6..6) px(x + dx, y, pale, 0.75f)
        for (dx in -5..5) px(x + dx, y - 1, pale, 0.9f)
        for (dx in -4..4) px(x + dx, y - 2, pale, 1.05f)
        for (side in listOf(-5, 5)) for (dy in 2..6) px(x + side, y - dy, pale, if (side < 0) 1.05f else 0.75f)
        for (dx in -6..-4) px(x + dx, y - 7, pale, 1.1f)
        for (dx in 4..6) px(x + dx, y - 7, pale, 0.8f)
        // The chair: seat, arms and a tall back.
        for (dx in -2..2) px(x + dx, y - 3, pale, 0.95f)
        px(x - 2, y - 4, pale, 1.1f); px(x + 2, y - 4, pale, 0.8f)
        for (dy in 4..9) for (dx in -1..1) px(x + dx, y - dy, pale, if (dx < 0) 1.1f else 0.85f)
        px(x, y - 10, pale, 1.15f)
    }

    /** The flowers of Ithilien: bright specks painted only on plain ground. */
    private fun flowers(x: Int, y: Int) {
        for (j in 0 until 5) for (i in -6..6) {
            val xx = x + i
            val yy = y - j
            if (!c.inside(xx, yy) || c.kind[yy * c.width + xx].toInt() != WorldCanvas.GROUND) continue
            if (h01(xx, yy, 77) > 0.28f) continue
            c.put(xx.toFloat(), yy.toFloat(), FLOWER_COLOURS[(h01(xx, yy, 78) * FLOWER_COLOURS.size).toInt()], 1f, WorldCanvas.KEEP)
        }
    }

    /** A crop field: furrows painted only on plain ground. */
    private fun crop(x: Int, y: Int) {
        for (j in 0 until 5) for (i in -6..6) {
            val xx = x + i
            val yy = y - j
            if (!c.inside(xx, yy) || c.kind[yy * c.width + xx].toInt() != WorldCanvas.GROUND) continue
            c.put(xx.toFloat(), yy.toFloat(), if (j % 2 == 1) 0xC8B860 else 0xB09C48, 1f, WorldCanvas.KEEP)
        }
    }
}
