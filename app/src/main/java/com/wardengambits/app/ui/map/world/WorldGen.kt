package com.wardengambits.app.ui.map.world

import com.wardengambits.app.ui.map.world.WorldNoise.fbm
import com.wardengambits.app.ui.map.world.WorldNoise.h01
import com.wardengambits.app.ui.map.world.WorldNoise.smooth
import com.wardengambits.app.ui.map.world.WorldNoise.valueNoise
import com.wardengambits.app.ui.map.world.ZoneKeys.ASH
import com.wardengambits.app.ui.map.world.ZoneKeys.BIOMES
import com.wardengambits.app.ui.map.world.ZoneKeys.CHANNELS
import com.wardengambits.app.ui.map.world.ZoneKeys.CLIFFS
import com.wardengambits.app.ui.map.world.ZoneKeys.DEADMARSH
import com.wardengambits.app.ui.map.world.ZoneKeys.ELEV
import com.wardengambits.app.ui.map.world.ZoneKeys.GOLDEN
import com.wardengambits.app.ui.map.world.ZoneKeys.GREYROCK
import com.wardengambits.app.ui.map.world.ZoneKeys.HILLRUINS
import com.wardengambits.app.ui.map.world.ZoneKeys.HILLS
import com.wardengambits.app.ui.map.world.ZoneKeys.LAVA
import com.wardengambits.app.ui.map.world.ZoneKeys.MARSH
import com.wardengambits.app.ui.map.world.ZoneKeys.MEADOW
import com.wardengambits.app.ui.map.world.ZoneKeys.MORGUL
import com.wardengambits.app.ui.map.world.ZoneKeys.MOUNT
import com.wardengambits.app.ui.map.world.ZoneKeys.SNOW
import com.wardengambits.app.ui.map.world.ZoneKeys.UNDER
import java.util.stream.IntStream
import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Generates the continuous Middle-earth map, a pure function of (zone ids, rows): only hashes and
 * seeded value noise, no Random, no clock. A port of the approved prototype
 * (`docs/superpowers/specs/assets/2026-10-15-map-proto-v2.html`) with the spec's fixes: the Durin
 * door and lake stay clear of sprites, and Moria darkens gradually following `field(under)`. The
 * road to Mordor (spec 2026-10-16 §5) adds the Anduin with its boat crossing, the Argonath statues,
 * the seat of Amon Hen, the Dead Marshes with their wisps, and the lava and cone of the Orodruin.
 */
object WorldGen {
    /** Tiles per zone. */
    const val ZONE_COLS = 56

    /** The prototype's height; vertical positions are fractions of it. */
    internal const val REFERENCE_ROWS = 200

    const val MIN_ROWS = 8

    /** Bump whenever the generated output changes: it invalidates the worlds saved on disk. */
    const val WORLD_GEN_VERSION = 3

    /** Centre row of the road at column [x]; continuous (`|roadY(x+1) − roadY(x)| ≤ 2`). */
    fun roadY(x: Int, rows: Int): Float {
        val s = min(1f, rows.toFloat() / REFERENCE_ROWS)
        val y = rows * 0.47f + s * (sin(x / 46f) * 13f + (valueNoise(x / 22f, 0f, 7) - 0.5f) * 16f)
        return y.coerceIn(2f, rows - 3f)
    }

    /**
     * The world for [zoneIds] and [rows]. The per-tile passes run on row bands across the available
     * cores when [parallel]; every tile is computed by the same code either way, so the output is
     * identical to the sequential one.
     */
    fun generate(zoneIds: List<String>, rows: Int, parallel: Boolean = true): WorldMap {
        require(zoneIds.isNotEmpty()) { "No zones" }
        require(rows >= MIN_ROWS) { "Too few rows: $rows" }
        return WorldBuilder(zoneIds, rows, parallel).build()
    }
}

/** Five-stop dark-to-light colour ramps of every biome (the prototype's `RAMP`). */
internal object Ramps {
    private val HEX = arrayOf(
        intArrayOf(0x4e7a2a, 0x6a9a34, 0x8cb446, 0xb4c45a, 0xd2c878), // farm
        intArrayOf(0x2f6a26, 0x447f2e, 0x5c9a3a, 0x7cb24a, 0xa2c862), // meadow
        intArrayOf(0x132e14, 0x1c4019, 0x28561f, 0x386c28, 0x4c8034), // forest
        intArrayOf(0x2a3a24, 0x3a4e2e, 0x4c6238, 0x60724a, 0x7a8a5e), // marsh
        intArrayOf(0x4e5a2c, 0x68743a, 0x84904a, 0xa2a85e, 0xc0bc7a), // hills
        intArrayOf(0x3e3a36, 0x56514a, 0x6e6860, 0x8a847a, 0xaaa498), // rock
        intArrayOf(0x8a98b0, 0xaebccc, 0xcad6e2, 0xe2eaf2, 0xfafcff), // snow
        intArrayOf(0x2e2c36, 0x44424e, 0x5c5a66, 0x78767e, 0x9a98a0), // mount
        intArrayOf(0x0c0a10, 0x141218, 0x1c1922, 0x26222c, 0x302b36), // under
        intArrayOf(0x7a5a1a, 0xa07c26, 0xc49e34, 0xdcbc4a, 0xf0d870), // golden
        intArrayOf(0x1e3a1c, 0x2a4e26, 0x3a6232, 0x4e763e, 0x66894c), // holly
        intArrayOf(0x1e5a26, 0x2e7432, 0x40903e, 0x58a84c, 0x7cc064), // valley
        intArrayOf(0x3a5a2a, 0x4e7034, 0x66883e, 0x86a052, 0xa8b46c), // bigriver (banks)
        intArrayOf(0x3a3632, 0x524c44, 0x6c655a, 0x8a8274, 0xa8a090), // cliffs
        intArrayOf(0x34502a, 0x486636, 0x5e7e42, 0x7a9452, 0x9aac6a), // hillruins
        intArrayOf(0x3c3e42, 0x55585c, 0x6e7276, 0x8a8e90, 0xa8acac), // greyrock
        intArrayOf(0x26281e, 0x363828, 0x484a32, 0x5c5c3e, 0x726e4e), // deadmarsh
        intArrayOf(0x2a6a2a, 0x3c8634, 0x52a03e, 0x70b84e, 0x96cc66), // herbs
        intArrayOf(0x1c2020, 0x2a302e, 0x3a423e, 0x4c5850, 0x627066), // morgul
        intArrayOf(0x2c2826, 0x403a36, 0x56504a, 0x6e6660, 0x8a827a), // ash
        intArrayOf(0x1a1210, 0x2a1a16, 0x3c241c, 0x502e22, 0x663a2a), // lava (basalt)
    )

    private val RGB = FloatArray(BIOMES * 15).also { a ->
        for (b in 0 until BIOMES) for (i in 0 until 5) {
            val c = HEX[b][i]
            a[b * 15 + i * 3] = (c shr 16 and 0xFF).toFloat()
            a[b * 15 + i * 3 + 1] = (c shr 8 and 0xFF).toFloat()
            a[b * 15 + i * 3 + 2] = (c and 0xFF).toFloat()
        }
    }

    /** Colour of [biome] at brightness [v] (0..1), written into [out]. */
    fun sample(biome: Int, v: Float, out: FloatArray) {
        val p = v.coerceIn(0f, 0.999f) * 4
        val i = p.toInt()
        val t = p - i
        val a = biome * 15 + i * 3
        val b = biome * 15 + min(4, i + 1) * 3
        out[0] = RGB[a] + (RGB[b] - RGB[a]) * t
        out[1] = RGB[a + 1] + (RGB[b + 1] - RGB[a + 1]) * t
        out[2] = RGB[a + 2] + (RGB[b + 2] - RGB[a + 2]) * t
    }
}

/** Float RGB pixel buffer plus a per-tile kind (ground, water, road, cliff, sprite). */
internal class WorldCanvas(val width: Int, val height: Int) {
    val img = FloatArray(width * height * 3)
    val kind = ByteArray(width * height)

    /** Scratch colour shared by the ramp sampling helpers. */
    val tmp = FloatArray(3)

    companion object {
        const val KEEP = -1
        const val GROUND = 0
        const val WATER = 1
        const val ROAD = 2
        const val CLIFF = 3
        const val SPRITE = 4
        const val LAVA = 5
    }

    fun round(v: Float): Int = floor(v + 0.5f).toInt()

    fun inside(x: Int, y: Int): Boolean = x in 0 until width && y in 0 until height

    fun set(i: Int, r: Float, g: Float, b: Float, k: Int) {
        img[i * 3] = r; img[i * 3 + 1] = g; img[i * 3 + 2] = b
        if (k != KEEP) kind[i] = k.toByte()
    }

    /** Paints [rgb] × [f] at the rounded position (x, y), ignoring positions outside the map. */
    fun put(x: Float, y: Float, rgb: Int, f: Float, k: Int) {
        val ix = round(x)
        val iy = round(y)
        if (!inside(ix, iy)) return
        set(iy * width + ix, (rgb shr 16 and 0xFF) * f, (rgb shr 8 and 0xFF) * f, (rgb and 0xFF) * f, k)
    }

    /** Paints [tmp] × [f] at the rounded position (x, y). */
    fun putTmp(x: Float, y: Float, f: Float, k: Int) {
        val ix = round(x)
        val iy = round(y)
        if (!inside(ix, iy)) return
        set(iy * width + ix, tmp[0] * f, tmp[1] * f, tmp[2] * f, k)
    }

    fun scale(x: Int, y: Int, f: Float) {
        val i = (y * width + x) * 3
        img[i] *= f; img[i + 1] *= f; img[i + 2] *= f
    }

    fun lerpTo(i: Int, rgb: Int, t: Float) {
        val o = i * 3
        img[o] += ((rgb shr 16 and 0xFF) - img[o]) * t
        img[o + 1] += ((rgb shr 8 and 0xFF) - img[o + 1]) * t
        img[o + 2] += ((rgb and 0xFF) - img[o + 2]) * t
    }
}

internal class WorldBuilder(
    private val zoneIds: List<String>,
    private val rows: Int,
    private val parallel: Boolean = true,
    private val drawSprites: Boolean = true,
) {
    private val zc = WorldGen.ZONE_COLS
    private val n = zoneIds.size
    private val w = n * zc
    private val h = rows
    private val yScale = rows.toFloat() / WorldGen.REFERENCE_ROWS

    private val keys = FloatArray(n * CHANNELS).also { a ->
        for (i in 0 until n) ZoneKeys.weights(zoneIds[i]).copyInto(a, i * CHANNELS)
    }
    private val c = WorldCanvas(w, h)
    private val warp = FloatArray(w * h)
    private val elev = FloatArray(w * h)
    private val best = ByteArray(w * h)
    private val road = FloatArray(w) { WorldGen.roadY(it, rows) }
    private val roadRow = IntArray(w) { c.round(road[it]) }

    /** Elevation of every tile, for tests. */
    internal val elevation: FloatArray get() = elev

    /** Under / snow / golden shares of the ground blend on the 7 road rows of every column. */
    private val roadShares = FloatArray(w * 7 * 3)

    private val durin = zoneIds.indexOf("durin")
    private val moria = zoneIds.indexOf("moria")
    private val sombrio = zoneIds.indexOf("sombrio")
    private val doorX = if (durin >= 0 && moria == durin + 1) moria * zc + 4 else -1

    // Rivers: base x, half width, ford (stepping stones) or bridge.
    private val riverX: FloatArray
    private val riverW: FloatArray
    private val riverFord: BooleanArray
    private val riverCx: FloatArray // [river * h + y]

    // Lakes: centre and radii.
    private val lakes: FloatArray // [cx, cy, rx, ry] per lake

    // The Anduin: a wide band along the road, per column (centre row NaN where there is no river).
    private val anduinCy = FloatArray(w) { Float.NaN }
    private val anduinHw = FloatArray(w)
    private val anduinLo = FloatArray(w) { Float.MAX_VALUE }
    private val anduinHi = FloatArray(w) { -Float.MAX_VALUE }

    /** 1 on the Anduin's water tiles. */
    private val bigWater = ByteArray(w * h)

    /** 1 on the tiles of the Orodruin cone, which keeps its own lava flows instead of cracks. */
    private val coneTile = ByteArray(w * h)

    /** Dead-marsh share (0..255) of the marsh pools, 0 elsewhere: where the wisps may float. */
    private val deadPool = ByteArray(w * h)

    // Amon Hen hill and the Orodruin cone (column −1 when the zone is absent).
    private val amonHen = zoneIds.indexOf("amonhen")
    private val hillX = if (amonHen >= 0) amonHen * zc + zc / 2 else -1
    private val hillY = if (hillX >= 0) road[hillX] - 28 * yScale else 0f
    private val orodruin = zoneIds.indexOf("orodruin")
    private val coneX = if (orodruin >= 0) orodruin * zc + zc / 2 else -1
    private val coneBase: Int
    private val coneApex: Int
    private val coneElevY: Float

    /** Sprite type of every entry of [WorldMap.spriteAnchors], in the same order (filled by [build]). */
    internal var spriteTypes = IntArray(0)
        private set

    init {
        val specs = listOf(
            Triple("puente", 32, 3.6f) to false,
            Triple("bruinen", 24, 4.4f) to true,
            Triple("nimrodel", 24, 3f) to true,
            Triple("rivendel", 40, 1.8f) to false,
        ).filter { zoneIds.indexOf(it.first.first) >= 0 }
        riverX = FloatArray(specs.size) { (zoneIds.indexOf(specs[it].first.first) * zc + specs[it].first.second).toFloat() }
        riverW = FloatArray(specs.size) { specs[it].first.third }
        riverFord = BooleanArray(specs.size) { specs[it].second }
        riverCx = FloatArray(specs.size * h)
        for (r in specs.indices) for (y in 0 until h) {
            val rx = riverX[r]
            riverCx[r * h + y] = rx + sin(y / 18f + rx) * 6 + (valueNoise(rx, y / 12f, 3) - 0.5f) * 8
        }
        val ry = max(1.5f, 9 * yScale)
        val lakeList = ArrayList<Float>()
        if (durin >= 0) lakeList += listOf((durin * zc + 20).toFloat(), h * 0.72f, 14f, ry)
        if (sombrio >= 0) lakeList += listOf((sombrio * zc + 32).toFloat(), h * 0.68f, 16f, ry)
        lakes = lakeList.toFloatArray()
        anduinCourse()
        if (coneX >= 0) {
            var top = h
            for (x in max(0, coneX - CONE_HALF - 3)..min(w - 1, coneX + CONE_HALF + 3)) top = min(top, roadRow[x])
            coneBase = top - 5
            coneApex = max(1, coneBase - c.round(max(10f, 78 * yScale)))
            coneElevY = coneApex + (coneBase - coneApex) * 0.3f
        } else {
            coneBase = 0; coneApex = 0; coneElevY = 0f
        }
    }

    /**
     * Course of the Anduin as (column, rows from the road at 200 rows, half width) control points,
     * joined by straight lines and then smoothed: it comes from the north through the golden wood
     * of Lothlórien, runs along the road, crosses it (the boat), drops south through the gorge of the Argonath
     * and leaves the map south of Amon Hen.
     */
    private fun anduinCourse() {
        if (zoneIds.indexOf("anduin") < 0) return
        val pts = ArrayList<FloatArray>()
        fun pt(zone: String, dx: Int, off: Float, hw: Float) {
            val z = zoneIds.indexOf(zone)
            if (z >= 0) pts += floatArrayOf((z * zc + dx).toFloat(), off, hw)
        }
        pt("lothlorien", 26, -130f, 6.5f); pt("lothlorien", 36, -44f, 6.5f); pt("lothlorien", 48, -26f, 7f)
        pt("anduin", 10, -18f, 8f); pt("anduin", 28, -16f, 8.5f); pt("anduin", 38, 0f, 8.5f); pt("anduin", 48, 16f, 8.5f)
        pt("argonath", 8, 24f, 8f); pt("argonath", 20, 30f, 8f); pt("argonath", 24, 36f, 8f); pt("argonath", 32, 72f, 8f); pt("argonath", 48, 78f, 8.5f)
        pt("amonhen", 16, 76f, 8.5f); pt("amonhen", 34, 82f, 8.5f); pt("amonhen", 56, 150f, 8.5f)
        // Taper the river where a missing zone cuts it short.
        if (pts.first()[1] > -120f) pts.add(0, floatArrayOf(pts.first()[0] - 16, pts.first()[1], 0f))
        if (pts.last()[1] < 120f) pts += floatArrayOf(pts.last()[0] + 16, pts.last()[1], 0f)
        val x0 = pts.first()[0].toInt()
        val x1 = pts.last()[0].toInt()
        val len = x1 - x0 + 1
        val off = FloatArray(len)
        val hw = FloatArray(len)
        var seg = 0
        for (k in 0 until len) {
            val x = x0 + k
            while (seg < pts.size - 2 && x > pts[seg + 1][0]) seg++
            val a = pts[seg]
            val b = pts[seg + 1]
            val t = ((x - a[0]) / (b[0] - a[0])).coerceIn(0f, 1f)
            off[k] = a[1] + (b[1] - a[1]) * t + (valueNoise(x / 9f, 0f, 401) - 0.5f) * 3
            hw[k] = a[2] + (b[2] - a[2]) * t
        }
        repeat(2) { boxBlur(off, RIVER_SMOOTH); boxBlur(hw, RIVER_SMOOTH) }
        val hwScale = max(yScale, 0.35f)
        for (k in 0 until len) {
            val x = x0 + k
            if (x !in 0 until w) continue
            val half = max(0f, hw[k]) * hwScale
            // Short maps squeeze the river towards the road but keep it off the road band.
            val clear = 4.5f + half
            val o = off[k]
            val eff = if (o < 0) -max(-o * yScale, min(-o, clear)) else max(o * yScale, min(o, clear))
            anduinCy[x] = road[x] + eff
            anduinHw[x] = half
        }
        for (x in 0 until w) {
            var lo = Float.MAX_VALUE
            var hi = -Float.MAX_VALUE
            for (xx in max(0, x - RIVER_WIN)..min(w - 1, x + RIVER_WIN)) {
                if (anduinCy[xx].isNaN()) continue
                lo = min(lo, anduinCy[xx] - anduinHw[xx] - 1)
                hi = max(hi, anduinCy[xx] + anduinHw[xx] + 1)
            }
            anduinLo[x] = lo
            anduinHi[x] = hi
        }
    }

    /** In-place moving average of radius [r], clamped at the ends. */
    private fun boxBlur(a: FloatArray, r: Int) {
        val src = a.copyOf()
        for (i in a.indices) {
            var sum = 0f
            for (j in i - r..i + r) sum += src[j.coerceIn(0, a.size - 1)]
            a[i] = sum / (2 * r + 1)
        }
    }

    /** Depth (0..1) of the Anduin at (x, y) from the true distance to its centre line, or −1 when dry. */
    private fun anduinDepth(x: Int, y: Int): Float {
        if (y < anduinLo[x] || y > anduinHi[x]) return -1f
        var depth = -1f
        for (xx in max(0, x - RIVER_WIN) until min(w - 1, x + RIVER_WIN)) {
            val ca = anduinCy[xx]
            val cb = anduinCy[xx + 1]
            if (ca.isNaN() || cb.isNaN()) continue
            for (s in 0 until 4) {
                val t = s / 4f
                val hw = anduinHw[xx] + (anduinHw[xx + 1] - anduinHw[xx]) * t
                if (hw <= 0f) continue
                val dx = xx + t - x
                val dy = ca + (cb - ca) * t - y
                val d = sqrt(dx * dx + dy * dy)
                if (d < hw) depth = max(depth, 1 - d / hw)
            }
        }
        return depth
    }

    /** Zone keyframes interpolated at a warped x: one per worker, so passes can run in parallel. */
    private inner class Lerp {
        private var fi = 0
        private var fj = 0
        private var ft = 0f

        fun locate(wx: Float) {
            val p = (wx - zc / 2f) / zc
            val i = floor(p).toInt().coerceIn(0, n - 1)
            fi = i
            fj = min(n - 1, i + 1)
            ft = smooth((p - i).coerceIn(0f, 1f))
        }

        fun fv(f: Int): Float = keys[fi * CHANNELS + f] * (1 - ft) + keys[fj * CHANNELS + f] * ft
    }

    /** Runs [body] over row bands [y0, y1) covering the map, in parallel when allowed. */
    private inline fun bands(crossinline body: (y0: Int, y1: Int) -> Unit) {
        if (!parallel) {
            body(0, h)
            return
        }
        val count = min(h, Runtime.getRuntime().availableProcessors() * 4)
        IntStream.range(0, count).parallel().forEach { b -> body(h * b / count, h * (b + 1) / count) }
    }

    private fun e(x: Int, y: Int): Float = elev[y.coerceIn(0, h - 1) * w + x.coerceIn(0, w - 1)]

    private fun lakeD(x: Int, y: Int): Float {
        var d = Float.MAX_VALUE
        var k = 0
        while (k < lakes.size) {
            val dx = (x - lakes[k]) / lakes[k + 2]
            val dy = (y - lakes[k + 1]) / lakes[k + 3]
            d = min(d, dx * dx + dy * dy)
            k += 4
        }
        return d
    }

    fun build(): WorldMap {
        fields()
        ground()
        cone()
        water()
        lavaCracks()
        shore()
        cliff()
        roadPass()
        door()
        val sprites = if (drawSprites) sprites() else SpriteResult(emptyList(), emptyList(), emptyList(), IntArray(0))
        spriteTypes = sprites.types
        moriaDarkness()
        val pixels = quantise()
        val stops = List(n) { val x = it * zc + zc / 2; Tile(x, roadRow[x]) }
        val vents = if (coneX >= 0) listOf(Tile(coneX, coneApex)) else emptyList()
        return WorldMap(
            w, h, pixels, indicesOf(WorldCanvas.WATER), sprites.chimneys, torches(), sprites.mallorns, stops, sprites.anchors,
            lava = indicesOf(WorldCanvas.LAVA), wisps = wisps(), vents = vents,
        )
    }

    private fun indicesOf(kind: Int): IntArray {
        var count = 0
        for (k in c.kind) if (k.toInt() == kind) count++
        val out = IntArray(count)
        count = 0
        for (i in c.kind.indices) if (c.kind[i].toInt() == kind) out[count++] = i
        return out
    }

    // ---- warp and elevation
    private fun fields() = bands { y0, y1 ->
        val l = Lerp()
        for (y in y0 until y1) {
            val edge = min(y, h - 1 - y) / (h / 2f)
            for (x in 0 until w) {
                val i = y * w + x
                val wx = x + (valueNoise(x / 18f, y / 18f, 91) - 0.5f) * 34 + (valueNoise(x / 6f, y / 6f, 92) - 0.5f) * 8
                warp[i] = wx
                l.locate(wx)
                // A term with a zero weight adds exactly +0, so its noise is skipped.
                val hills = l.fv(HILLS) + 0.7f * l.fv(GREYROCK) + 0.6f * l.fv(CLIFFS) + 0.5f * l.fv(HILLRUINS)
                val hillTerm = if (hills > 0f) hills * 0.35f * fbm(x / 14f, y / 14f, 2) else 0f
                var z = 0.25f * fbm(x / 40f, y / 40f, 1) + hillTerm + l.fv(ELEV) * 0.25f
                val mount = l.fv(MOUNT) * max(0f, 1 - edge * 1.25f)
                if (mount > 0f) {
                    val ridge = 1 - abs(fbm(x / 26f, y / 26f, 40, 4) * 2 - 1)
                    z += mount * (0.6f + 0.8f * ridge)
                }
                if (hillX >= 0) {
                    val dx = (x - hillX) / 20f
                    val dy = (y - hillY) / max(4f, 18 * yScale)
                    val d = dx * dx + dy * dy
                    if (d < 1f) z += 0.35f * smooth(1 - sqrt(d))
                }
                if (coneX >= 0) {
                    // A straight cone: its single highest point is the centre of the final zone.
                    val dx = (x - coneX) / 30f
                    val dy = (y - coneElevY) / max(6f, 40 * yScale)
                    val d = sqrt(dx * dx + dy * dy)
                    if (d < 1f) z += 1.2f * (1 - d)
                }
                elev[i] = z
            }
        }
    }

    // ---- ground colour: weighted blend of every biome present, plus hillshade
    private fun ground() = bands { y0, y1 ->
        val l = Lerp()
        val col = FloatArray(3)
        val tmp = FloatArray(3)
        for (y in y0 until y1) for (x in 0 until w) {
            val i = y * w + x
            val z = elev[i]
            l.locate(warp[i])
            val tex = fbm(x / 5f, y / 5f, 7, 3)
            var tw = 0f
            var bw = -1f
            var bestB = MEADOW
            var underW = 0f
            var snowW = 0f
            var goldenW = 0f
            var morgulW = 0f
            col[0] = 0f; col[1] = 0f; col[2] = 0f
            for (k in 0 until BIOMES) {
                var wt = l.fv(k)
                if (wt <= 0f) continue
                wt *= 0.6f + 0.8f * valueNoise(x / 11f, y / 11f, k * 13 + 5)
                if (k == SNOW) wt *= ((z - 0.55f) * 3).coerceIn(0f, 1.5f)
                if (k == MOUNT) wt *= ((z - 0.35f) * 3).coerceIn(0f, 1.4f)
                wt *= wt
                if (wt <= 0f) continue
                if (wt > bw) { bw = wt; bestB = k }
                Ramps.sample(k, brightness(k, x, y, tex), tmp)
                col[0] += tmp[0] * wt; col[1] += tmp[1] * wt; col[2] += tmp[2] * wt
                tw += wt
                when (k) {
                    UNDER -> underW = wt
                    SNOW -> snowW = wt
                    GOLDEN -> goldenW = wt
                    MORGUL -> morgulW = wt
                }
            }
            if (tw > 0f) {
                col[0] /= tw; col[1] /= tw; col[2] /= tw
                underW /= tw; snowW /= tw; goldenW /= tw; morgulW /= tw
            } else {
                col[0] = 90f; col[1] = 140f; col[2] = 60f
            }
            // The pale green glow of the Morgul vale.
            if (morgulW > 0f) {
                val g = valueNoise(x / 9f, y / 9f, 521)
                if (g > 0.68f) {
                    val t = ((g - 0.68f) * 2.4f).coerceIn(0f, 0.6f) * morgulW
                    col[0] += (140 - col[0]) * t; col[1] += (200 - col[1]) * t; col[2] += (160 - col[2]) * t
                }
            }
            // High peaks are always snow, except in Mordor.
            val mordor = (l.fv(MORGUL) + l.fv(ASH) + l.fv(LAVA)).coerceIn(0f, 1f)
            if (z > 0.95f && mordor < 0.5f) {
                val t = ((z - 0.95f) * 4).coerceIn(0f, 1f) * (1 - 2 * mordor)
                Ramps.sample(SNOW, 0.6f + 0.3f * tex, tmp)
                for (ch in 0..2) col[ch] += (tmp[ch] - col[ch]) * t
                snowW = max(snowW, t)
                bestB = SNOW
            }
            best[i] = bestB.toByte()
            val d = y - roadRow[x]
            if (d in -3..3) {
                val o = (x * 7 + d + 3) * 3
                roadShares[o] = underW; roadShares[o + 1] = snowW; roadShares[o + 2] = goldenW
            }
            // Hillshade, light from the north-west; Moria's darkness flattens it.
            val dx = e(x + 1, y) - e(x - 1, y)
            val dy = e(x, y + 1) - e(x, y - 1)
            var sh = (1 - (dx + dy) * 3.2f).coerceIn(0.55f, 1.35f)
            sh += (1 - sh) * 0.6f * underW
            c.set(i, col[0] * sh, col[1] * sh, col[2] * sh, WorldCanvas.GROUND)
        }
    }

    /** Ramp position of biome [k] at (x, y): the plain texture, or the biome's own pattern. */
    private fun brightness(k: Int, x: Int, y: Int, tex: Float): Float {
        val fine = 0.15f * (valueNoise(x / 3f, y / 3f, k) - 0.5f)
        return when (k) {
            // Vertical streaks of the cliff faces.
            CLIFFS -> 0.2f + 0.6f * valueNoise(x / 1.6f, y / 10f, 501) + 0.3f * (tex - 0.5f) + fine
            // A maze of dark crevices between grey slabs.
            GREYROCK -> {
                val ridge = abs(valueNoise(x / 7f, y / 5f, 511) * 2 - 1)
                (0.3f + 0.5f * tex + fine) * (if (ridge < 0.12f) 0.45f + ridge * 3 else 1f)
            }
            ASH -> 0.3f + 0.4f * tex + 0.2f * (valueNoise(x / 13f, y / 4f, 531) - 0.5f) + fine
            else -> 0.25f + 0.5f * tex + fine
        }
    }

    /** The Orodruin: a dark, gullied cone north of the road with a glowing crater and lava flows. */
    private fun cone() {
        if (coneX < 0) return
        val height = (coneBase - coneApex).toFloat()
        for (y in coneApex - 1..coneBase + 3) {
            if (y !in 0 until h) continue
            val t = ((y - coneApex) / height).coerceAtLeast(0f)
            val half = 2.5f + (CONE_HALF - 2.5f) * t.pow(0.85f) + (valueNoise(y / 3f, 0f, 601) - 0.5f) * 2.4f * min(t, 1f)
            for (x in (coneX - half - 1).toInt()..(coneX + half + 1).toInt()) {
                if (x !in 0 until w) continue
                val dx = x - coneX
                if (abs(dx) > half) continue
                val bottom = min(coneBase + (valueNoise(x / 4f, 0f, 602) * 4).toInt(), roadRow[x] - 5)
                if (y > bottom) continue
                val i = y * w + x
                coneTile[i] = 1
                if (y < coneApex) {
                    // The crater rim.
                    if (abs(dx) <= 3) c.set(i, 46f, 30f, 26f, WorldCanvas.GROUND)
                    continue
                }
                val u = dx / max(half, 1f)
                val gully = valueNoise(u * 7f + 50f, t * 2.5f, 611)
                val lit = (0.55f - 0.35f * u + 0.35f * (gully - 0.5f) + 0.12f * (valueNoise(x / 2f, y / 2f, 612) - 0.5f))
                    .coerceIn(0f, 1f)
                // Ash-grey summit fading into basalt at the foot.
                Ramps.sample(ZoneKeys.ASH, 0.15f + lit * 0.85f, c.tmp)
                val ar = c.tmp[0]; val ag = c.tmp[1]; val ab = c.tmp[2]
                Ramps.sample(LAVA, lit, c.tmp)
                val m = (t * 1.6f - 0.3f).coerceIn(0f, 1f)
                var f = 1f
                if (abs(dx) > half - 1) f = 0.7f
                c.set(i, (ar + (c.tmp[0] - ar) * m) * f, (ag + (c.tmp[1] - ag) * m) * f, (ab + (c.tmp[2] - ab) * m) * f, WorldCanvas.GROUND)
                // Lava: the crater, a long flow down the lit face and a short one from a side vent.
                val s = (y - coneApex).toFloat()
                val flowA = coneX + 1 + s * 0.22f + sin(s / 5f) * 1.5f
                val flowB = coneX - 1 - s * 0.14f + sin(s / 4f + 2) * 1.2f
                val wA = 0.6f + 1.1f * t
                val wB = 0.5f + 0.8f * t
                val dA = abs(x - flowA)
                val dB = if (t in 0.3f..0.85f) abs(x - flowB) else Float.MAX_VALUE
                when {
                    s <= 1f && abs(dx) <= 1 -> c.set(i, 255f, 190f, 70f, WorldCanvas.LAVA)
                    dA <= wA || dB <= wB -> {
                        val core = dA <= wA * 0.45f || dB <= wB * 0.45f
                        if (core) c.set(i, 255f, 160f, 44f, WorldCanvas.LAVA) else c.set(i, 214f, 72f, 22f, WorldCanvas.LAVA)
                    }
                }
            }
        }
    }

    /** Glowing cracks in the ash of Gorgoroth and the Orodruin, kept off the road band. */
    private fun lavaCracks() = bands { y0, y1 ->
        val l = Lerp()
        for (y in y0 until y1) for (x in 0 until w) {
            val i = y * w + x
            if (c.kind[i].toInt() != WorldCanvas.GROUND || coneTile[i].toInt() == 1 || abs(y - roadRow[x]) <= 4) continue
            l.locate(warp[i])
            val lv = l.fv(LAVA)
            if (lv < 0.15f) continue
            val r = abs(valueNoise(x / 16f, y / 10f, 701) * 2 - 1)
            val thr = 0.025f + 0.035f * (lv - 0.15f)
            if (r >= thr || valueNoise(x / 9f, y / 9f, 702) < 0.5f) continue
            if (r < thr * 0.45f) c.set(i, 255f, 150f, 40f, WorldCanvas.LAVA) else c.set(i, 200f, 70f, 20f, WorldCanvas.LAVA)
        }
    }

    // ---- water: rivers, the Anduin, lakes and marsh pools
    private fun water() = bands { y0, y1 ->
        val l = Lerp()
        for (y in y0 until y1) for (x in 0 until w) {
            val i = y * w + x
            var depth = -1f
            for (r in riverX.indices) {
                val d = abs(x - riverCx[r * h + y])
                if (d < riverW[r]) depth = max(depth, 1 - d / riverW[r])
            }
            var big = false
            val ad = anduinDepth(x, y)
            if (ad >= 0f) {
                depth = max(depth, ad)
                big = true
            }
            val ld = lakeD(x, y)
            if (ld < 1f) depth = max(depth, 1 - ld)
            l.locate(warp[i])
            val marsh = l.fv(MARSH)
            if (marsh > 0.45f) {
                val pn = fbm(x / 9f, y / 9f, 77, 3)
                if (pn > 0.6f) depth = max(depth, (pn - 0.6f) * 4)
            }
            val dead = l.fv(DEADMARSH)
            var deadPoolHere = false
            if (dead > 0.35f) {
                val pn = fbm(x / 7f, y / 7f, 79, 3)
                val thr = 0.64f - 0.08f * dead
                if (pn > thr) {
                    depth = max(depth, (pn - thr) * 4)
                    deadPoolHere = true
                }
            }
            if (depth < 0f) continue
            if (big) bigWater[i] = 1
            if (deadPoolHere) deadPool[i] = (dead.coerceAtMost(1f) * 255).toInt().toByte()
            val cold = l.fv(SNOW) + l.fv(MOUNT) * 0.5f
            val deep: Int
            val shallow: Int
            when {
                cold > 0.5f -> { deep = 0x223E60; shallow = 0x6E96B4 }
                deadPoolHere -> { deep = 0x1E2A24; shallow = 0x485840 }
                marsh > 0.45f -> { deep = 0x2C423A; shallow = 0x607858 }
                big -> { deep = 0x1A3E6A; shallow = 0x4A82AA }
                else -> { deep = 0x1C4680; shallow = 0x5096C8 }
            }
            val t = (depth * 1.6f).coerceIn(0f, 1f)
            c.set(
                i,
                lerp(shallow shr 16 and 0xFF, deep shr 16 and 0xFF, t),
                lerp(shallow shr 8 and 0xFF, deep shr 8 and 0xFF, t),
                lerp(shallow and 0xFF, deep and 0xFF, t),
                WorldCanvas.WATER,
            )
        }
    }

    private fun lerp(a: Int, b: Int, t: Float): Float = a + (b - a) * t

    /** Wet, sandy edge around the water. */
    private fun shore() = bands { y0, y1 ->
        val kind = c.kind
        for (y in max(1, y0) until min(h - 1, y1)) for (x in 1 until w - 1) {
            val i = y * w + x
            if (kind[i].toInt() != WorldCanvas.GROUND) continue
            val wet = kind[i + 1].toInt() == WorldCanvas.WATER || kind[i - 1].toInt() == WorldCanvas.WATER ||
                kind[i + w].toInt() == WorldCanvas.WATER || kind[i - w].toInt() == WorldCanvas.WATER
            if (wet) c.lerpTo(i, 0x96825A, 0.45f)
            val hot = kind[i + 1].toInt() == WorldCanvas.LAVA || kind[i - 1].toInt() == WorldCanvas.LAVA ||
                kind[i + w].toInt() == WorldCanvas.LAVA || kind[i - w].toInt() == WorldCanvas.LAVA
            if (hot) c.lerpTo(i, 0x7A2A12, 0.4f)
        }
    }

    /** The wall's jag follows the map height, so short maps get the same spread of edge columns. */
    private fun jag(y: Int): Int = ((valueNoise(0f, y / (6f * yScale), 61) - 0.5f) * 7).roundToInt()

    /** The jagged rock wall of the Durin gates. */
    private fun cliff() {
        if (doorX < 0) return
        for (y in 0 until h) {
            val left = doorX - 4 + jag(y)
            for (x in left until left + 8) {
                if (x !in 0 until w) continue
                val t = (x - left) / 8f
                val v = 0.75f + 0.25f * valueNoise(x / 2f, y / 2f, 9)
                c.set(y * w + x, (86 + (46 - 86) * t) * v, (84 + (44 - 84) * t) * v, (98 + (54 - 98) * t) * v, WorldCanvas.CLIFF)
            }
        }
    }

    private fun isFord(x: Int, y: Int): Boolean {
        for (r in riverX.indices) if (riverFord[r] && abs(x - riverCx[r * h + y]) < riverW[r] + 1.5f) return true
        return false
    }

    // ---- road: edges, stones, shadow; bridges and fords over water
    private fun roadPass() {
        for (x in 0 until w) {
            val y0 = roadRow[x]
            for (d in -3..3) {
                val y = y0 + d
                if (y !in 0 until h) continue
                val i = y * w + x
                val k = c.kind[i].toInt()
                val ad = abs(d)
                if (k == WorldCanvas.WATER && bigWater[i].toInt() == 1) {
                    // The Anduin is crossed by boat: a calm wake on the central rows, open water around.
                    if (ad <= 1) {
                        c.lerpTo(i, 0x9CC4DC, if ((x + d) % 4 == 0) 0.45f else 0.22f)
                        c.kind[i] = WorldCanvas.ROAD.toByte()
                    }
                    continue
                }
                if (k == WorldCanvas.WATER) {
                    if (isFord(x, y)) {
                        if (ad <= 1 || (ad == 2 && h01(x, y, 4) < 0.5f)) {
                            val f = 0.85f + 0.2f * h01(x, y, 41)
                            c.set(i, 150 * f, 146 * f, 136 * f, WorldCanvas.ROAD)
                        }
                    } else if (ad <= 2) {
                        val f = (if (x % 3 == 0) 0.75f else 1f) * (if (ad == 2) 0.8f else 1f)
                        c.set(i, 140 * f, 96 * f, 52 * f, WorldCanvas.ROAD)
                    } else {
                        c.set(i, 90f, 60f, 32f, WorldCanvas.ROAD)
                    }
                    continue
                }
                if (k == WorldCanvas.CLIFF) continue
                if (ad == 3) {
                    c.scale(x, y, if (d > 0) 0.72f else 0.9f)
                    continue
                }
                val o = (x * 7 + d + 3) * 3
                val wu = smoothRange(0.2f, 0.55f, roadShares[o])
                val ws = smoothRange(0.2f, 0.55f, roadShares[o + 1])
                val wg = smoothRange(0.2f, 0.55f, roadShares[o + 2])
                val sum = wu + ws + wg
                val norm = if (sum > 1f) 1f / sum else 1f
                val dirt = 1f - min(sum, 1f)
                var r = 196 * dirt + (70 * wu + 206 * ws + 232 * wg) * norm
                var g = 160 * dirt + (64 * wu + 212 * ws + 214 * wg) * norm
                var b = 104 * dirt + (78 * wu + 222 * ws + 160 * wg) * norm
                var f = if (ad == 2) 0.82f else 1f
                if (valueNoise(x / 2f, y / 2f, 31) > 0.78f) f *= 0.8f
                if (h01(x, y, 32) < 0.05f) { r = 140f; g = 134f; b = 124f; f = 1f }
                f *= 0.92f + 0.12f * valueNoise(x / 4f, y.toFloat(), 33)
                c.set(i, r * f, g * f, b * f, WorldCanvas.ROAD)
            }
        }
        boats()
    }

    /** An elven boat with two travellers in the middle of every stretch where the road is on the Anduin. */
    private fun boats() {
        var x = 0
        while (x < w) {
            if (bigWater[roadRow[x] * w + x].toInt() != 1) { x++; continue }
            val start = x
            while (x < w && bigWater[roadRow[x] * w + x].toInt() == 1) x++
            if (x - start < 5) continue
            val m = (start + x - 1) / 2
            val y0 = roadRow[m]
            fun p(dx: Int, dy: Int, rgb: Int) = c.put((m + dx).toFloat(), (y0 + dy).toFloat(), rgb, 1f, WorldCanvas.ROAD)
            for (dx in -3..3) p(dx, 1, 0x3E2A1A)
            for (dx in -4..4) p(dx, 0, if (abs(dx) == 4) 0x5A3E24 else 0x8A6A44)
            for (dx in -3..3) p(dx, -1, 0xC2B696)
            p(-1, -1, 0x55694A); p(-1, -2, 0xE0C098) // cloaked traveller
            p(1, -1, 0x6E5A3E); p(1, -2, 0xD4B48C) // second traveller
            p(3, -2, 0x6A4A2A); p(4, -3, 0x6A4A2A) // paddle
        }
    }

    private fun smoothRange(a: Float, b: Float, v: Float): Float = smooth(((v - a) / (b - a)).coerceIn(0f, 1f))

    /** Left column of the gate, aligned with the wall's jag on the road row. */
    private val doorLeft: Int get() = doorX - 4 + jag(roadRow[doorX])

    /** The gate: a light opening 3 rows high flanked by carved jambs, with an arch above. */
    private fun door() {
        if (doorX < 0) return
        val y0 = roadRow[doorX]
        val left = doorLeft
        val right = left + 7
        fun paint(x: Int, y: Int, rgb: Int, f: Float, k: Int) {
            if (c.inside(x, y)) c.put(x.toFloat(), y.toFloat(), rgb, f, k)
        }
        for (x in left..right) {
            for (y in y0 - 1..y0 + 1) paint(x, y, DOOR_LIGHT, 1f, WorldCanvas.ROAD)
            // Carved jambs above and below the opening, with grooves every other tile.
            val groove = if ((x - left) % 2 == 0) 1f else 0.82f
            paint(x, y0 - 2, 0xB2AEC4, groove, WorldCanvas.CLIFF)
            paint(x, y0 + 2, 0x9894AC, groove, WorldCanvas.CLIFF)
        }
        // Arch: a lintel row one tile narrower on each side, with a dark outline.
        for (x in left + 1..right - 1) paint(x, y0 - 3, 0x9692AA, 1f, WorldCanvas.CLIFF)
        paint(left, y0 - 3, 0x3C3A48, 1f, WorldCanvas.CLIFF)
        paint(right, y0 - 3, 0x3C3A48, 1f, WorldCanvas.CLIFF)
        for (x in left + 2..right - 2) paint(x, y0 - 4, 0x3C3A48, 1f, WorldCanvas.CLIFF)
    }

    private class SpriteResult(val chimneys: List<Tile>, val mallorns: List<Tile>, val anchors: List<Tile>, val types: IntArray)

    /**
     * Tiles no sprite footprint may cover: the road band (its 7 rows), the lakes, the Durin gate
     * surroundings and the stops. The landmarks (Argonath statues, Amon Hen seat) that fit outside
     * them are added to [landmarks] and their footprints, like the Orodruin cone, protected too.
     */
    private fun protectedPrefix(landmarks: MutableList<Long>): IntArray {
        val mask = ByteArray(w * h)
        for (y in 0 until h) for (x in 0 until w) if (lakeD(x, y) < 1.7f) mask[y * w + x] = 1
        for (x in 0 until w) for (y in roadRow[x] - 3..roadRow[x] + 3) if (c.inside(x, y)) mask[y * w + x] = 1
        if (doorX >= 0) {
            val y0 = roadRow[doorX]
            for (y in y0 - 4 - DOOR_CLEAR..y0 + 2 + DOOR_CLEAR) for (x in doorLeft - DOOR_CLEAR..doorLeft + 7 + DOOR_CLEAR) {
                if (c.inside(x, y)) mask[y * w + x] = 1
            }
        }
        for (s in 0 until n) {
            val sx = s * zc + zc / 2
            val sy = roadRow[sx]
            for (y in sy - 8..sy + 4) for (x in sx - 6..sx + 6) if (c.inside(x, y)) mask[y * w + x] = 1
        }
        val ext = IntArray(3)
        for ((type, x, y) in landmarkCandidates()) {
            WorldSprites.extent(type, 1f, ext)
            val x0 = x - ext[0]; val x1 = x + ext[0]; val y0 = y - ext[1]; val y1 = y + ext[2]
            if (x0 < 0 || x1 >= w || y0 < 0 || y1 >= h) continue
            var free = true
            for (yy in y0..y1) for (xx in x0..x1) if (mask[yy * w + xx].toInt() == 1) free = false
            if (!free) continue
            landmarks += (y.toLong() shl 40) or (x.toLong() shl 8) or type.toLong()
            for (yy in y0..y1) for (xx in x0..x1) mask[yy * w + xx] = 1
        }
        if (coneX >= 0) {
            for (y in max(0, coneApex - 2)..min(h - 1, coneBase + 4)) {
                for (x in max(0, coneX - CONE_HALF - 4)..min(w - 1, coneX + CONE_HALF + 4)) mask[y * w + x] = 1
            }
        }
        val sat = IntArray((w + 1) * (h + 1))
        for (y in 0 until h) {
            var rowSum = 0
            for (x in 0 until w) {
                rowSum += mask[y * w + x]
                sat[(y + 1) * (w + 1) + x + 1] = sat[y * (w + 1) + x + 1] + rowSum
            }
        }
        return sat
    }

    private fun protectedIn(sat: IntArray, x0: Int, y0: Int, x1: Int, y1: Int): Boolean {
        val ax = x0.coerceIn(0, w)
        val ay = y0.coerceIn(0, h)
        val bx = (x1 + 1).coerceIn(0, w)
        val by = (y1 + 1).coerceIn(0, h)
        if (ax >= bx || ay >= by) return false
        val s = sat[by * (w + 1) + bx] - sat[ay * (w + 1) + bx] - sat[by * (w + 1) + ax] + sat[ay * (w + 1) + ax]
        return s > 0
    }

    /** (type, x, y) of the hand-placed sprites: the Argonath on both banks and the seat of Amon Hen. */
    private fun landmarkCandidates(): List<Triple<Int, Int, Int>> {
        val out = ArrayList<Triple<Int, Int, Int>>()
        val argonath = zoneIds.indexOf("argonath")
        if (argonath >= 0) {
            // Where the river drops through the gorge, one king on each bank of the same row.
            val x0 = argonath * zc
            // Search the river at the kings' mid-height; their plinths stand 14 rows lower.
            val row = c.round(road[x0 + zc / 2] + 52 * yScale)
            var left = -1
            var right = -1
            if (row in 0 until h) {
                for (x in x0 until x0 + zc) {
                    val wet = bigWater[row * w + x].toInt() == 1
                    if (wet && left < 0) left = x
                    if (wet) right = x
                    if (!wet && left >= 0) break
                }
            }
            if (left >= 0) {
                out += Triple(WorldSprites.STATUE, left - STATUE_GAP, row + 14)
                out += Triple(WorldSprites.STATUE, right + STATUE_GAP, row + 14)
            }
        }
        if (hillX >= 0) out += Triple(WorldSprites.SEAT, hillX, c.round(hillY) + 3)
        return out
    }

    private fun sprites(): SpriteResult {
        val landmarks = ArrayList<Long>()
        val sat = protectedPrefix(landmarks)
        val ext = IntArray(3)
        var items = LongArray(1024 + landmarks.size)
        var count = 0
        for (l in landmarks) items[count++] = l
        val kind = c.kind
        for (gy in 4 until h step GRID) for (gx in 0 until w step GRID) {
            val x = gx + (h01(gx, gy, 201) * GRID).toInt()
            val y = gy + (h01(gx, gy, 202) * GRID).toInt()
            if (x >= w || y >= h) continue
            val i = y * w + x
            if (kind[i].toInt() != WorldCanvas.GROUND) continue
            val b = best[i].toInt()
            if (h01(gx, gy, 203) > WorldSprites.DENSITY[b]) continue
            val type = WorldSprites.pick(b, h01(gx, gy, 204))
            val ry = road[x]
            if (abs(y - ry) < WorldSprites.roadClearance(type) && y < ry + 3) continue
            if (nearWaterOrCliff(x, y)) continue
            WorldSprites.extent(type, WorldSprites.scaleOf(type, x, y), ext)
            if (protectedIn(sat, x - ext[0], y - ext[1], x + ext[0], y + ext[2])) continue
            if (count == items.size) items = items.copyOf(count * 2)
            items[count++] = (y.toLong() shl 40) or (x.toLong() shl 8) or type.toLong()
        }
        items = items.copyOf(count)
        items.sort()
        val painter = WorldSprites(c)
        val chimneys = ArrayList<Tile>()
        val mallorns = ArrayList<Tile>()
        val anchors = ArrayList<Tile>(count)
        val types = IntArray(count)
        for (item in items) {
            val y = (item ushr 40).toInt()
            val x = ((item ushr 8) and 0xFFFFFFFFL).toInt()
            val type = (item and 0xFF).toInt()
            val landmark = type == WorldSprites.STATUE || type == WorldSprites.SEAT
            val chimney = painter.draw(type, x, y, if (landmark) 1f else WorldSprites.scaleOf(type, x, y))
            if (chimney != null && c.inside(chimney.x, chimney.y)) chimneys += chimney
            if (type == WorldSprites.MALLORN) mallorns += Tile(x, max(0, y - 9))
            types[anchors.size] = type
            anchors += Tile(x, y)
        }
        return SpriteResult(chimneys, mallorns, anchors, types)
    }

    private fun nearWaterOrCliff(x: Int, y: Int): Boolean {
        var k = -6
        while (k <= 6) {
            for (j in -3..0) {
                val xx = x + k
                val yy = y + j
                if (!c.inside(xx, yy)) continue
                val kd = c.kind[yy * w + xx].toInt()
                if (kd == WorldCanvas.WATER || kd == WorldCanvas.CLIFF || kd == WorldCanvas.LAVA) return true
            }
            k += 2
        }
        return false
    }

    /** Will-o'-the-wisps over the pools of the Dead Marshes, off the road. */
    private fun wisps(): List<Tile> {
        val out = ArrayList<Tile>()
        for (gy in 0 until h step WISP_GRID) for (gx in 0 until w step WISP_GRID) {
            if (h01(gx, gy, 301) > 0.6f) continue
            val x = gx + (h01(gx, gy, 302) * WISP_GRID).toInt()
            val y = gy + (h01(gx, gy, 303) * WISP_GRID).toInt()
            if (x >= w || y >= h) continue
            val i = y * w + x
            if (c.kind[i].toInt() != WorldCanvas.WATER || (deadPool[i].toInt() and 0xFF) < WISP_MIN_DEAD) continue
            if (abs(y - roadRow[x]) <= 4) continue
            out += Tile(x, max(0, y - 1))
        }
        return out
    }

    private fun torches(): List<Tile> {
        if (moria < 0) return emptyList()
        val start = if (doorX >= 0) doorX + 8 else moria * zc + 12
        val end = min(w, (moria + 1) * zc + 4)
        val out = ArrayList<Tile>()
        var x = start
        while (x < end) {
            out += Tile(x, max(0, roadRow[x] - 6))
            x += 11
        }
        return out
    }

    /** Moria: darkness that grows away from the road, following field(under) without cuts. */
    private fun moriaDarkness() {
        val near = 6 * yScale
        val span = 40 * yScale
        bands { y0, y1 ->
            val l = Lerp()
            for (y in y0 until y1) for (x in 0 until w) {
                val i = y * w + x
                l.locate(warp[i])
                val u = l.fv(UNDER)
                if (u <= 0f) continue
                val d = abs(y - road[x])
                val f = 1 - u * ((d - near) / span).coerceIn(0f, 0.75f)
                if (f < 1f) c.scale(x, y, f)
            }
        }
    }

    /** Ordered 4×4 Bayer dither to 32 levels per channel (step 8). */
    private fun quantise(): IntArray {
        val out = IntArray(w * h)
        val img = c.img
        bands { y0, y1 ->
            for (y in y0 until y1) for (x in 0 until w) {
                val i = y * w + x
                val th = (BAYER[(y and 3) * 4 + (x and 3)] / 16f - 0.5f) * 10f
                var argb = 0xFF shl 24
                for (ch in 0..2) {
                    val v = (floor((img[i * 3 + ch] + th) / 8f + 0.5f).toInt() * 8).coerceIn(0, 255)
                    argb = argb or (v shl (16 - ch * 8))
                }
                out[i] = argb
            }
        }
        return out
    }

    private companion object {
        const val GRID = 7
        const val DOOR_CLEAR = 10
        const val CONE_HALF = 28
        const val STATUE_GAP = 9
        const val RIVER_SMOOTH = 3
        const val RIVER_WIN = 10
        const val WISP_GRID = 7

        /** Dead-marsh share (of 255) a pool needs for a wisp: keeps them inside the marshes. */
        const val WISP_MIN_DEAD = 180
        const val DOOR_LIGHT = 0xECE6C8
        val BAYER = intArrayOf(0, 8, 2, 10, 12, 4, 14, 6, 3, 11, 1, 9, 15, 7, 13, 5)
    }
}
