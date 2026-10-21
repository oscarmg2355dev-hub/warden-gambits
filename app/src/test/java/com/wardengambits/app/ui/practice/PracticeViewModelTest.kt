package com.wardengambits.app.ui.practice

import com.wardengambits.app.GameSession
import com.wardengambits.app.InMemoryPreferencesDataStore
import com.wardengambits.app.data.SaveRepository
import com.wardengambits.engine.story.NodeId
import com.wardengambits.engine.story.Phase
import com.wardengambits.engine.story.RunState
import com.wardengambits.engine.story.StoryProfile
import com.wardengambits.engine.story.StoryEngine
import com.wardengambits.engine.story.content.StoryContentLoader
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PracticeViewModelTest {
    private val engine = StoryEngine(StoryContentLoader.loadFromResources())
    private val english = StoryEngine(StoryContentLoader.loadFromResources(language = "en"))
    private val dispatcher = UnconfinedTestDispatcher()

    @Before fun setUp() = Dispatchers.setMain(dispatcher)
    @After fun tearDown() = Dispatchers.resetMain()

    private fun session() = GameSession(
        engine,
        SaveRepository(InMemoryPreferencesDataStore(), engine) { },
        CoroutineScope(SupervisorJob() + dispatcher),
    ) { 0L }

    @Test fun mapsEntriesToZonesWithTranslatedNames() {
        val vm = PracticeViewModel(session(), MutableStateFlow(engine))
        val zones = vm.zones.value
        val entries = engine.practiceEntries(engine.newGame())
        assertEquals(entries.map { it.zoneId }.distinct(), zones.map { it.zoneId })
        assertEquals(zones.map { engine.zone(it.zoneId).name }, zones.map { it.name })
        assertEquals(engine.practiceEntries(engine.newGame()).size, zones.sumOf { it.cells.size })
        assertEquals(0, vm.currentZoneIndex)
    }

    @Test fun titleIsTheNodesAndPortraitTheFirstRivals() {
        val vm = PracticeViewModel(session(), MutableStateFlow(engine))
        val entry = engine.practiceEntries(engine.newGame()).first()
        val cell = vm.zones.value.first().cells.first()
        assertEquals(entry.node, cell.node)
        assertEquals(engine.node(entry.node).title, cell.title)
        assertEquals(entry.rivals.first().portrait, cell.portrait)
        assertEquals(entry.rivals.size, cell.rivalCount)
        assertEquals(entry.isBoss, cell.isBoss)
        assertEquals(entry.unlocked, cell.unlocked)
    }

    @Test fun gridFollowsLanguage() {
        val story = MutableStateFlow(engine)
        val vm = PracticeViewModel(session(), story)
        val before = vm.zones.value
        story.value = english
        val after = vm.zones.value
        assertEquals(after.map { english.zone(it.zoneId).name }, after.map { it.name })
        assertEquals(english.node(after.first().cells.first().node).title, after.first().cells.first().title)
        assertTrue(before.first().name != after.first().name || before.first().cells.first().title != after.first().cells.first().title)
    }

    private fun indexWhenAt(node: NodeId, phase: Phase): Int {
        val repo = SaveRepository(InMemoryPreferencesDataStore(), engine) { }
        runBlocking { repo.save(StoryProfile(RunState(5, 1, 1, 4, node, listOf(node), phase), emptyList())) }
        val session = GameSession(engine, repo, CoroutineScope(SupervisorJob() + dispatcher)) { 0L }
        runBlocking { session.load() }
        val vm = PracticeViewModel(session, MutableStateFlow(engine))
        assertEquals(node, session.profile.value.run.node)
        return vm.currentZoneIndex
    }

    private fun indexOfZone(id: String) = PracticeViewModel(session(), MutableStateFlow(engine)).zones.value.indexOfFirst { it.zoneId == id }

    @Test fun opensAtTheCurrentZone() = assertEquals(indexOfZone("moria"), indexWhenAt(NodeId("moria", 4, 1), Phase.COMBAT_PENDING))

    @Test fun restZoneOpensAtTheNextZoneWithEncounters() {
        assertEquals(indexOfZone("eregion"), indexWhenAt(NodeId("rivendel", 1, 0), Phase.CHOOSING))
        assertEquals(indexOfZone("anduin"), indexWhenAt(NodeId("lothlorien", 1, 0), Phase.CHOOSING))
    }

    @Test fun finalOpensAtTheLastZone() {
        val last = PracticeViewModel(session(), MutableStateFlow(engine)).zones.value.lastIndex
        assertEquals(last, indexWhenAt(NodeId.FINAL, Phase.LAP_COMPLETE))
    }
}
