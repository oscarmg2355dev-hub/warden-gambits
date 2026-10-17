package com.wardengambits.app.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.wardengambits.engine.story.NodeId
import com.wardengambits.engine.story.Phase
import com.wardengambits.engine.story.RunRecord
import com.wardengambits.engine.story.RunResult
import com.wardengambits.engine.story.SaveCodec
import com.wardengambits.engine.story.StoryAction
import com.wardengambits.engine.story.StoryEngine
import com.wardengambits.engine.story.content.StoryContentLoader
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class SaveRepositoryTest {
    @get:Rule val tmp = TemporaryFolder()

    private val engine = StoryEngine(StoryContentLoader.loadFromResources())
    private val key = stringPreferencesKey("save_json")
    private val warnings = mutableListOf<String>()

    private fun TestScope.repo(): Pair<SaveRepository, DataStore<Preferences>> {
        val ds = PreferenceDataStoreFactory.create(scope = backgroundScope) { File(tmp.root, "t.preferences_pb") }
        return SaveRepository(ds, engine) { warnings += it } to ds
    }

    @Test fun emptyLoadsNewGame() = runTest {
        val (repo, _) = repo()
        assertEquals(engine.newGame(), repo.load())
    }

    @Test fun saveThenLoadRoundTrip() = runTest {
        val (repo, _) = repo()
        val profile = engine.reduce(engine.newGame(), StoryAction.Choose(0)).profile
        repo.save(profile)
        assertEquals(profile, repo.load())
    }

    @Test fun corruptJsonLoadsNewGame() = runTest {
        val (repo, ds) = repo()
        ds.edit { it[key] = "{oops" }
        assertEquals(engine.newGame(), repo.load())
        assertEquals(1, warnings.size)
    }

    @Test fun invalidProfileLoadsNewGame() = runTest {
        val (repo, ds) = repo()
        val fresh = engine.newGame()
        val badNode = NodeId("bree", 9, 0)
        val bad = fresh.copy(run = fresh.run.copy(node = badNode, path = listOf(badNode)))
        ds.edit { it[key] = SaveCodec.encode(bad) }
        assertEquals(fresh, repo.load())
        assertEquals(1, warnings.size)
    }

    @Test fun wrongVersionLoadsNewGame() = runTest {
        val (repo, ds) = repo()
        val valid = engine.reduce(engine.newGame(), StoryAction.Choose(0)).profile
        val json = SaveCodec.encode(valid).replace("\"version\":1", "\"version\":2")
        check("\"version\":2" in json)
        ds.edit { it[key] = json }
        assertEquals(engine.newGame(), repo.load())
        assertEquals(1, warnings.size)
    }

    /** Road to Mordor §4: a save at the old final node (Lothlórien 1/0, lap complete) resumes there, choosing. */
    @Test fun oldFinalSaveContinuesInLothlorien() = runTest {
        val (repo, ds) = repo()
        val node = NodeId("lothlorien", 1, 0)
        val fresh = engine.newGame()
        val history = listOf(RunRecord(1, node, 30, 28, RunResult.COMPLETED, 5L, deaths = 3))
        val old = fresh.copy(
            run = fresh.run.copy(level = 40, lap = 2, lapStartLevel = 30, wins = 12, deaths = 4, node = node, path = listOf(node), phase = Phase.LAP_COMPLETE),
            history = history,
        )
        ds.edit { it[key] = SaveCodec.encode(old) }
        val loaded = repo.load()
        assertEquals(old.copy(run = old.run.copy(phase = Phase.CHOOSING)), loaded)
        assertEquals("lothlorien_1_0", loaded.run.node.key)
        assertEquals(emptyList<String>(), warnings)
    }

    @Test fun readFailureLoadsNewGame() = runTest {
        val repo = SaveRepository(com.wardengambits.app.FailingPreferencesDataStore(), engine) { warnings += it }
        assertEquals(engine.newGame(), repo.load())
        assertEquals(1, warnings.size)
    }
}
