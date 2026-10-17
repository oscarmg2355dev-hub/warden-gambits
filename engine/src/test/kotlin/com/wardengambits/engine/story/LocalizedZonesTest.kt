package com.wardengambits.engine.story

import com.wardengambits.engine.story.content.StoryContentLoader
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame

/** Zone names come from the loaded content, so an English story shows English zone names. */
class LocalizedZonesTest {
    private val spanish = StoryEngine(StoryContentLoader.loadFromResources())
    private val english = StoryEngine(StoryContentLoader.loadFromResources(language = "en"))

    @Test fun translatingTheParsedSpanishStoryEqualsLoadingTheTranslation() {
        val base = StoryContentLoader.loadFromResources()
        val loaded = StoryContentLoader.loadFromResources(language = "en")
        assertEquals(loaded.zones, StoryContentLoader.translated(base, "en").zones)
        assertSame(base, StoryContentLoader.translated(base, "es"))
    }

    @Test fun spanishZonesAreTheRoute() = assertEquals(Route.zones, spanish.zones())

    @Test fun englishZonesKeepTheRouteIdsAndOrder() {
        val zones = english.zones()
        assertEquals(Route.zones.map { it.id }, zones.map { it.id })
        assertEquals(ZoneInfo("chet", "The Chetwood"), zones[1])
        assertEquals(ZoneInfo("orodruin", "Mount Doom"), english.zone("orodruin"))
    }

    @Test fun zoneProgressUsesTheContentName() {
        val start = english.newGame()
        val chet = NodeId("chet", 2, 1)
        val atChet = start.copy(run = start.run.copy(node = chet, path = listOf(chet)))
        assertEquals(ZoneProgress(ZoneInfo("chet", "The Chetwood"), 2, TreeShape.LEVELS), english.zoneProgress(atChet))
        val final = start.copy(run = start.run.copy(node = NodeId.FINAL, phase = Phase.LAP_COMPLETE))
        assertEquals(ZoneProgress(ZoneInfo("orodruin", "Mount Doom"), 1, 1), english.zoneProgress(final))
        assertEquals(ZoneProgress(Route.zone("chet"), 2, TreeShape.LEVELS), spanish.zoneProgress(atChet))
    }
}
