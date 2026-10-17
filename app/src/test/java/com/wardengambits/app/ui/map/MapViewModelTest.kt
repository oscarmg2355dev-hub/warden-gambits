package com.wardengambits.app.ui.map

import com.wardengambits.app.GameSession
import com.wardengambits.app.InMemoryPreferencesDataStore
import com.wardengambits.app.data.SaveRepository
import com.wardengambits.engine.story.NodeId
import com.wardengambits.engine.story.Phase
import com.wardengambits.engine.story.Route
import com.wardengambits.engine.story.StoryAction
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
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MapViewModelTest {
    private val engine = StoryEngine(StoryContentLoader.loadFromResources())
    private val english = StoryEngine(StoryContentLoader.loadFromResources(language = "en"))
    private val dispatcher = UnconfinedTestDispatcher()

    @Before fun setUp() = Dispatchers.setMain(dispatcher)
    @After fun tearDown() = Dispatchers.resetMain()

    private fun session(repo: SaveRepository = repo()) = GameSession(
        engine,
        repo,
        CoroutineScope(SupervisorJob() + dispatcher),
    ) { 0L }

    private fun repo() = SaveRepository(InMemoryPreferencesDataStore(), engine) { }

    private fun rowsAt(node: NodeId?): List<ZoneRowUi> {
        val repo = repo()
        val session = session(repo)
        if (node != null) runBlocking {
            val p = engine.newGame()
            repo.save(p.copy(run = p.run.copy(node = node, path = listOf(node), phase = if (node == NodeId.FINAL) Phase.LAP_COMPLETE else Phase.CHOOSING)))
            session.load()
        }
        return MapViewModel(session, MutableStateFlow(engine)).rows.value
    }

    @Test fun newGameRows() {
        val rows = rowsAt(null)
        assertEquals(24, rows.size)
        assertEquals(ZoneRowUi("bree", "Bree", ZoneStatus.CURRENT, 1, 7), rows[0])
        assertEquals(List(23) { ZoneStatus.LOCKED }, rows.drop(1).map { it.status })
    }

    @Test fun midRouteRows() {
        val rows = rowsAt(NodeId("chet", 3, 0))
        assertEquals(Route.zones.map { it.name }, rows.map { it.name })
        assertEquals(ZoneStatus.DONE, rows[0].status)
        assertEquals(ZoneRowUi("chet", "Bosque de Chet", ZoneStatus.CURRENT, 3, 7), rows[1])
        assertEquals(List(22) { ZoneStatus.LOCKED }, rows.drop(2).map { it.status })
    }

    @Test fun finalNodeRows() {
        val rows = rowsAt(NodeId.FINAL)
        assertEquals(List(23) { ZoneStatus.DONE }, rows.take(23).map { it.status })
        assertEquals(ZoneRowUi("orodruin", "Monte del Destino", ZoneStatus.CURRENT, 1, 1), rows[23])
    }

    @Test fun rowsFollowTheStoryLanguage() {
        val story = MutableStateFlow(engine)
        val vm = MapViewModel(session(), story)
        assertEquals("Bosque de Chet", vm.rows.value[1].name)
        story.value = english
        assertEquals(english.zones().map { it.name }, vm.rows.value.map { it.name })
        assertEquals("The Chetwood", vm.rows.value[1].name)
        assertEquals("Mount Doom", vm.rows.value[23].name)
    }

    @Test fun rowsFollowSession() {
        val session = session()
        val vm = MapViewModel(session, MutableStateFlow(engine))
        session.dispatch(StoryAction.Choose(0))
        assertEquals(2, vm.rows.value[0].level)
    }
}
