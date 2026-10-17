package com.wardengambits.app.ui.map

import com.wardengambits.app.ui.map.world.WorldGen
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotSame
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

@RunWith(RobolectricTestRunner::class)
class MapWorldCacheTest {
    private val ids = listOf("bree", "chet")

    @Test
    fun cacheReturnsSameInstance() {
        val first = WorldCache.get(ids, 200)
        assertSame(first, WorldCache.get(ids, 200))
        assertSame(first, WorldCache.cached(ids, 200))
    }

    @Test
    fun cacheKeepsOnlyTheTwoMostRecentWorlds() {
        val a = WorldCache.get(ids, 40)
        WorldCache.get(ids, 41)
        WorldCache.get(ids, 42)
        assertNull(WorldCache.cached(ids, 40))
        assertNotSame(a, WorldCache.get(ids, 40))
    }

    @Test
    fun concurrentCallsForTheSameWorldGenerateOnce() {
        val generations = AtomicInteger()
        val started = CountDownLatch(1)
        val release = CountDownLatch(1)
        val lru = WorldLru(capacity = 2) { zones, rows ->
            generations.incrementAndGet()
            started.countDown()
            release.await(5, TimeUnit.SECONDS)
            WorldGen.generate(zones, rows)
        }
        val pool = Executors.newFixedThreadPool(2)
        try {
            val first = pool.submit<Any> { lru.get(ids, 40) }
            assertTrue(started.await(5, TimeUnit.SECONDS))
            val second = pool.submit<Any> { lru.get(ids, 40) }
            // Give the second caller time to reach the cache while the first is still generating.
            Thread.sleep(200)
            release.countDown()
            val a = first.get(10, TimeUnit.SECONDS)
            val b = second.get(10, TimeUnit.SECONDS)
            assertSame(a, b)
            assertEquals(1, generations.get())
        } finally {
            pool.shutdownNow()
        }
    }
}
