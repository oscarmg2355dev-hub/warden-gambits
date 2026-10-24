package com.wardengambits.app

import android.app.Application
import android.os.Looper
import androidx.activity.ComponentActivity
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavHostController
import androidx.navigation.compose.ComposeNavigator
import androidx.navigation.testing.TestNavHostController
import androidx.test.core.app.ApplicationProvider
import com.wardengambits.app.data.SaveRepository
import com.wardengambits.app.data.SettingsRepository
import com.wardengambits.app.navigation.Routes
import com.wardengambits.app.navigation.WardenNavHost
import com.wardengambits.app.navigation.navigateSingle
import com.wardengambits.app.ui.combat.CombatViewModel
import com.wardengambits.app.ui.combat.PracticeCombatSource
import com.wardengambits.app.ui.combat.StoryCombatSource
import com.wardengambits.app.ui.combat.TutorialCombatSource
import com.wardengambits.engine.combo.Input
import com.wardengambits.engine.model.CombatSetup
import com.wardengambits.engine.model.Difficulty
import com.wardengambits.engine.model.Outcome
import com.wardengambits.engine.rules.CombatEngine
import com.wardengambits.engine.story.NodeId
import com.wardengambits.engine.story.Phase
import com.wardengambits.engine.story.StoryEngine
import com.wardengambits.engine.story.StoryProfile
import com.wardengambits.engine.story.TreeShape
import com.wardengambits.engine.story.content.StoryContentLoader
import com.wardengambits.engine.tutorial.TutorialLessons
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf

/** The chosen difficulty: first-run default, persistence, and its effect on story and practice fights (not the tutorial). */
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class DifficultyFlowTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private val engine = StoryEngine(StoryContentLoader.loadFromResources())
    private val node: NodeId = TreeShape.nodesOf("bree").first { engine.node(it).encounter?.rivals?.size == 1 }
    private val app get() = ApplicationProvider.getApplicationContext<Application>()

    @After fun tearDown() = Dispatchers.resetMain()

    private fun container(store: DataStore<Preferences>) = AppContainer(app, dataStore = store)

    private fun idleMain() = shadowOf(Looper.getMainLooper()).idle()

    private fun storeWithSave(): InMemoryPreferencesDataStore {
        val store = InMemoryPreferencesDataStore()
        runBlocking { SaveRepository(store, engine) { }.save(engine.newGame()) }
        return store
    }

    @Test fun difficultyDefaultsToEyeWithSave() {
        val store = storeWithSave()
        val container = container(store)
        idleMain()
        assertEquals(Difficulty.EYE, container.difficulty.value)
        assertEquals(Difficulty.EYE, runBlocking { SettingsRepository(store).difficulty.first() })
    }

    @Test fun difficultyDefaultsToRangerWithoutSave() {
        val store = InMemoryPreferencesDataStore()
        val container = container(store)
        idleMain()
        assertEquals(Difficulty.RANGER, container.difficulty.value)
        assertEquals(Difficulty.RANGER, runBlocking { SettingsRepository(store).difficulty.first() })
    }

    @Test fun storedDifficultyIsNotReResolved() {
        val withSave = storeWithSave()
        runBlocking { SettingsRepository(withSave).setDifficulty(Difficulty.MARK) }
        val first = container(withSave)
        idleMain()
        assertEquals(Difficulty.MARK, first.difficulty.value)

        val withoutSave = InMemoryPreferencesDataStore()
        runBlocking { SettingsRepository(withoutSave).setDifficulty(Difficulty.MARK) }
        val second = container(withoutSave)
        idleMain()
        assertEquals(Difficulty.MARK, second.difficulty.value)
    }

    @Test fun setDifficultyPersists() {
        val store = InMemoryPreferencesDataStore()
        val container = container(store)
        idleMain()
        container.setDifficulty(Difficulty.SHIRE)
        idleMain()
        assertEquals(Difficulty.SHIRE, container.difficulty.value)
        assertEquals(Difficulty.SHIRE, runBlocking { SettingsRepository(store).difficulty.first() })
    }

    /** A store whose data cannot be read and whose writes fail (corrupt file, full disk). */
    private class BrokenStore : DataStore<Preferences> {
        override val data: Flow<Preferences> = flow { throw java.io.IOException("unreadable") }
        override suspend fun updateData(transform: suspend (t: Preferences) -> Preferences): Preferences =
            throw java.io.IOException("unwritable")
    }

    @Test fun difficultyResolvesInMemoryWhenTheStoreIsBroken() {
        val container = container(BrokenStore())
        idleMain()
        assertEquals(Difficulty.RANGER, container.difficulty.value)
        container.setDifficulty(Difficulty.MARK)
        idleMain()
        assertEquals(Difficulty.MARK, container.difficulty.value)
    }

    @Test fun unknownStoredValueCountsAsMissing() {
        val store = InMemoryPreferencesDataStore()
        runBlocking { store.edit { it[stringPreferencesKey("difficulty")] = "BOGUS" } }
        assertNull(runBlocking { SettingsRepository(store).difficulty.first() })
    }

    private suspend fun TestScope.session(): GameSession {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val repo = SaveRepository(InMemoryPreferencesDataStore(), engine) { }
        val session = GameSession(engine, repo, backgroundScope) { 0L }
        val p = engine.newGame()
        val parent = TreeShape.nodesOf(node.zoneId).firstOrNull { node in TreeShape.destinations(it) }
        val seeded: StoryProfile = p.copy(
            run = p.run.copy(node = node, path = listOfNotNull(parent, node), phase = Phase.COMBAT_PENDING, level = 60),
        )
        assertTrue(engine.isValid(seeded))
        repo.save(seeded)
        session.load()
        return session
    }

    private fun rivalAttack(setup: CombatSetup?) =
        CombatEngine().start(checkNotNull(setup), 1L).fighters.last().stats.attack

    @Test fun storyCombatUsesDifficulty() = runTest {
        val s = session()
        val shire = StoryCombatSource(s, engine, Difficulty.SHIRE)
        val eye = StoryCombatSource(s, engine, Difficulty.EYE)
        assertEquals(50, shire.setup?.rivalAttackPct)
        assertEquals(rivalAttack(eye.setup) / 2, rivalAttack(shire.setup))

        val vm = CombatViewModel(shire, MutableStateFlow(engine), CombatEngine(), { 1234L })
        vm.setActive(true)
        repeat(800) {
            if (vm.state.value.outcome != null) return@repeat
            vm.press(Input.SPEAR)
            vm.press(Input.SHIELD)
            vm.execute()
            advanceTimeBy(1000L)
            runCurrent()
        }
        assertEquals(Outcome.VICTORY, vm.state.value.outcome)
        vm.finish()
        assertEquals(Difficulty.SHIRE, s.profile.value.run.easiestDifficulty)
    }

    @Test fun practiceCombatUsesDifficulty() = runTest {
        session()
        val shire = PracticeCombatSource(node, engine, Difficulty.SHIRE)
        val eye = PracticeCombatSource(node, engine, Difficulty.EYE)
        assertEquals(50, shire.setup?.rivalAttackPct)
        assertEquals(rivalAttack(eye.setup) / 2, rivalAttack(shire.setup))
    }

    @Test fun tutorialIgnoresDifficulty() {
        val source = TutorialCombatSource("attack", TutorialLessons.load(), "Attack", "Dummy") { }
        assertEquals(100, source.setup?.rivalAttackPct)
    }

    /** A store that never delivers anything: the difficulty stays unresolved (null). */
    private class SilentStore : DataStore<Preferences> {
        override val data: Flow<Preferences> = flow { awaitCancellation() }
        override suspend fun updateData(transform: suspend (t: Preferences) -> Preferences): Preferences =
            awaitCancellation()
    }

    @Test fun combatWaitsForDifficulty() {
        val container = container(SilentStore())
        lateinit var nav: NavHostController
        composeRule.setContent {
            nav = TestNavHostController(LocalContext.current).apply {
                navigatorProvider.addNavigator(ComposeNavigator())
            }
            WardenNavHost(container, nav)
        }
        composeRule.runOnUiThread {
            nav.navigateSingle(Routes.MAP)
            nav.navigateSingle(Routes.COMBAT)
        }
        composeRule.waitForIdle()
        assertNull(container.difficulty.value)
        assertEquals(Routes.COMBAT, nav.currentBackStackEntry?.destination?.route)
        val entry = checkNotNull(nav.currentBackStackEntry)
        val none = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T = error("no view model")
        }
        val failure = runCatching { ViewModelProvider(entry.viewModelStore, none)[CombatViewModel::class.java] }
        assertTrue("the combat source must not exist before the difficulty is known", failure.isFailure)
    }
}
