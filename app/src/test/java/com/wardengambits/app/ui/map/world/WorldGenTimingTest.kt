package com.wardengambits.app.ui.map.world

import com.wardengambits.engine.story.Route
import org.junit.Test

class WorldGenTimingTest {
    /** Profiling aid: TIME_WORLD=1 ./gradlew :app:testDebugUnitTest --tests "*WorldGenTimingTest*" -i */
    @Test fun timeGenerationWhenAsked() {
        if (System.getenv("TIME_WORLD") == null) return
        val ids = Route.zones.map { it.id }
        for ((zs, r) in listOf(ids to 197, ids to 200, ids to 60, listOf("bree", "chet") to 40, listOf("durin", "moria") to 120)) {
            val m = WorldGen.generate(zs, r)
            println("HASH ${zs.size}x$r ${m.pixels.contentHashCode()} ${m.water.contentHashCode()} ${m.chimneys.hashCode()} ${m.torches.hashCode()} ${m.mallorns.hashCode()} ${m.stops.hashCode()}")
        }
        for (parallel in listOf(false, true)) repeat(6) {
            val start = System.nanoTime()
            WorldGen.generate(ids, 197, parallel)
            println("TIMING parallel=$parallel run $it: ${(System.nanoTime() - start) / 1_000_000} ms")
        }
    }
}
