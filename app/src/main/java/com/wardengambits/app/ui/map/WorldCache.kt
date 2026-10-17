package com.wardengambits.app.ui.map

import android.util.Log
import com.wardengambits.app.BuildConfig
import com.wardengambits.app.ui.map.world.WorldGen
import com.wardengambits.app.ui.map.world.WorldMap
import java.util.concurrent.ExecutionException
import java.util.concurrent.FutureTask

/** Where the map screen gets its world from; tests inject a slow one to see the loading state. */
interface WorldSource {
    /** The world if it is ready right now (no generation), so returning to the map does not flash. */
    fun cached(zoneIds: List<String>, rows: Int): WorldMap? = null

    /** The world, generating it if needed. May be slow: call it off the main thread. */
    fun load(zoneIds: List<String>, rows: Int): WorldMap
}

/**
 * Process-wide LRU of the last 2 generated worlds, keyed by (zones, rows), on top of an optional
 * [WorldDiskCache] so a new launch loads the saved world instead of generating it.
 */
object WorldCache : WorldSource {
    private val store = WorldLru(capacity = 2, generate = ::timedGenerate)

    /** Saves and loads generated worlds through [cache] (set once at app start; null turns it off). */
    fun useDisk(cache: WorldDiskCache?) = store.useDisk(cache)

    fun get(zoneIds: List<String>, rows: Int): WorldMap = store.get(zoneIds, rows)

    override fun cached(zoneIds: List<String>, rows: Int): WorldMap? = store.cached(zoneIds, rows)

    override fun load(zoneIds: List<String>, rows: Int): WorldMap = store.get(zoneIds, rows)

    private fun timedGenerate(zoneIds: List<String>, rows: Int): WorldMap {
        val start = System.nanoTime()
        val world = WorldGen.generate(zoneIds, rows)
        if (BuildConfig.DEBUG) {
            val ms = (System.nanoTime() - start) / 1_000_000
            Log.d("WorldGen", "generate $ms ms (${world.width}x${world.height} tiles, ${zoneIds.size} zones)")
        }
        return world
    }
}

/** LRU of the last [capacity] worlds, loading from the disk cache or calling [generate] on a miss. */
internal class WorldLru(
    private val capacity: Int,
    private val generate: (zoneIds: List<String>, rows: Int) -> WorldMap,
) {
    private data class Key(val zoneIds: List<String>, val rows: Int)

    private val lock = Any()

    @Volatile
    private var disk: WorldDiskCache? = null

    fun useDisk(cache: WorldDiskCache?) {
        disk = cache
    }

    private val entries = object : LinkedHashMap<Key, WorldMap>(4, 0.75f, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<Key, WorldMap>?): Boolean = size > capacity
    }

    /** One task per world being loaded or generated, so concurrent callers share it. */
    private val inFlight = HashMap<Key, FutureTask<WorldMap>>()

    fun get(zoneIds: List<String>, rows: Int): WorldMap {
        val key = Key(zoneIds.toList(), rows)
        val task: FutureTask<WorldMap>
        val owner: Boolean
        synchronized(lock) {
            entries[key]?.let { return it }
            val running = inFlight[key]
            owner = running == null
            task = running ?: FutureTask { produce(key) }.also { inFlight[key] = it }
        }
        if (owner) {
            // Load or generate outside the lock so a main-thread cached() never waits for it.
            task.run()
            synchronized(lock) {
                inFlight.remove(key)
                if (!task.isCancelled) runCatching { task.get() }.onSuccess { entries[key] = it }
            }
        }
        return try {
            task.get()
        } catch (e: ExecutionException) {
            throw e.cause ?: e
        }
    }

    private fun produce(key: Key): WorldMap =
        timedLoad(key) ?: generate(key.zoneIds, key.rows).also { disk?.save(key.zoneIds, key.rows, it) }

    fun cached(zoneIds: List<String>, rows: Int): WorldMap? =
        synchronized(lock) { entries[Key(zoneIds.toList(), rows)] }

    private fun timedLoad(key: Key): WorldMap? {
        val store = disk ?: return null
        val start = System.nanoTime()
        val world = store.load(key.zoneIds, key.rows) ?: return null
        if (BuildConfig.DEBUG) {
            val ms = (System.nanoTime() - start) / 1_000_000
            Log.d("WorldGen", "load from disk $ms ms (${world.width}x${world.height} tiles)")
        }
        return world
    }
}
