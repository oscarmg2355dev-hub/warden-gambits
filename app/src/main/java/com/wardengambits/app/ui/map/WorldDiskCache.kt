package com.wardengambits.app.ui.map

import com.wardengambits.app.ui.map.world.Tile
import com.wardengambits.app.ui.map.world.WorldGen
import com.wardengambits.app.ui.map.world.WorldMap
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.File
import java.io.IOException
import java.nio.ByteBuffer

/**
 * Generated worlds saved in [dir] (the app's cache dir), so a new launch loads the map instead of
 * generating it. Files are `<version>-<zones hash>-<rows>.bin`; a new [version] (bump
 * [WorldGen.WORLD_GEN_VERSION] when the generator changes) misses every old file and removes them.
 */
class WorldDiskCache(private val dir: File, private val version: Int = WorldGen.WORLD_GEN_VERSION) {

    fun load(zoneIds: List<String>, rows: Int): WorldMap? {
        val file = fileFor(zoneIds, rows)
        if (!file.isFile) return null
        return try {
            DataInputStream(BufferedInputStream(file.inputStream())).use { read(it, zoneIds, rows) }
        } catch (e: IOException) {
            file.delete()
            null
        }
    }

    fun save(zoneIds: List<String>, rows: Int, world: WorldMap) {
        try {
            dir.mkdirs()
            val file = fileFor(zoneIds, rows)
            val tmp = File(dir, file.name + ".tmp")
            DataOutputStream(BufferedOutputStream(tmp.outputStream())).use { write(it, zoneIds, rows, world) }
            if (!tmp.renameTo(file)) {
                tmp.delete()
                return
            }
            prune(keep = file)
        } catch (_: IOException) {
            // A cache that cannot be written only costs a regeneration on the next launch.
        }
    }

    private fun fileFor(zoneIds: List<String>, rows: Int): File {
        val hash = Integer.toHexString(zoneIds.joinToString(",").hashCode())
        return File(dir, "$version-$hash-$rows.bin")
    }

    /** Keeps [keep] and the most recent other world of this version (e.g. the other orientation). */
    private fun prune(keep: File) {
        val files = dir.listFiles() ?: return
        val current = files.filter { it != keep && it.name.startsWith("$version-") && it.name.endsWith(".bin") }
            .sortedByDescending { it.lastModified() }
        files.filter { it != keep && it !in current.take(MAX_FILES - 1) }.forEach { it.delete() }
    }

    private fun write(out: DataOutputStream, zoneIds: List<String>, rows: Int, world: WorldMap) {
        out.writeInt(MAGIC)
        out.writeInt(version)
        out.writeUTF(zoneIds.joinToString(","))
        out.writeInt(rows)
        out.writeInt(world.width)
        out.writeInt(world.height)
        writeInts(out, world.pixels)
        writeInts(out, world.water)
        writeInts(out, world.lava)
        for (tiles in listOf(world.chimneys, world.torches, world.mallorns, world.stops, world.spriteAnchors, world.wisps, world.vents)) {
            writeInts(out, IntArray(tiles.size * 2) { if (it % 2 == 0) tiles[it / 2].x else tiles[it / 2].y })
        }
    }

    private fun read(input: DataInputStream, zoneIds: List<String>, rows: Int): WorldMap? {
        if (input.readInt() != MAGIC || input.readInt() != version) return null
        if (input.readUTF() != zoneIds.joinToString(",") || input.readInt() != rows) return null
        val width = input.readInt()
        val height = input.readInt()
        if (width <= 0 || height <= 0 || width.toLong() * height > MAX_INTS) throw IOException("Corrupt world: ${width}x$height")
        val size = width * height
        val pixels = readInts(input)
        if (pixels.size != size) throw IOException("Corrupt world: ${pixels.size} pixels")
        val water = readInts(input)
        if (water.any { it !in 0 until size }) throw IOException("Corrupt world: water index out of range")
        val lava = readInts(input)
        if (lava.any { it !in 0 until size }) throw IOException("Corrupt world: lava index out of range")
        val tiles = List(TILE_LISTS) {
            val a = readInts(input)
            if (a.size % 2 != 0) throw IOException("Corrupt world: odd tile list")
            List(a.size / 2) { Tile(a[2 * it], a[2 * it + 1]) }
        }
        for (list in tiles) for (t in list) {
            if (t.x !in 0 until width || t.y !in 0 until height) throw IOException("Corrupt world: tile $t outside the map")
        }
        return WorldMap(width, height, pixels, water, tiles[0], tiles[1], tiles[2], tiles[3], tiles[4], lava, tiles[5], tiles[6])
    }

    private fun writeInts(out: DataOutputStream, values: IntArray) {
        out.writeInt(values.size)
        val bytes = ByteBuffer.allocate(values.size * 4)
        bytes.asIntBuffer().put(values)
        out.write(bytes.array())
    }

    private fun readInts(input: DataInputStream): IntArray {
        val size = input.readInt()
        if (size < 0 || size > MAX_INTS) throw IOException("Corrupt world: $size ints")
        val bytes = ByteArray(size * 4)
        input.readFully(bytes)
        return IntArray(size).also { ByteBuffer.wrap(bytes).asIntBuffer().get(it) }
    }

    private companion object {
        const val MAGIC = 0x57524C44 // "WRLD"
        const val MAX_FILES = 2

        /** chimneys, torches, mallorns, stops, sprite anchors, wisps, vents. */
        const val TILE_LISTS = 7
        const val MAX_INTS = 16_000_000
    }
}
