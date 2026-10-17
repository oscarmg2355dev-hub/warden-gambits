package com.wardengambits.app

import android.app.Application
import androidx.activity.ComponentActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavHostController
import androidx.navigation.compose.ComposeNavigator
import androidx.navigation.testing.TestNavHostController
import androidx.test.core.app.ApplicationProvider
import com.wardengambits.app.audio.FakeSoundPlayer
import com.wardengambits.app.i18n.AppLanguage
import com.wardengambits.app.i18n.AppScript
import com.wardengambits.app.i18n.ProvideLanguageAndScript
import com.wardengambits.app.navigation.Routes
import com.wardengambits.app.navigation.WardenNavHost
import com.wardengambits.app.navigation.navigateSingle
import com.wardengambits.app.ui.map.MapViewModel
import com.wardengambits.app.ui.theme.WardenTheme
import com.wardengambits.engine.story.Phase
import com.wardengambits.engine.story.StoryEngine
import com.wardengambits.engine.story.TreeShape
import com.wardengambits.engine.story.content.StoryContentLoader
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** The story (node texts, rival names, zone names) follows the language chosen in the app. */
@RunWith(RobolectricTestRunner::class)
@Config(qualifiers = "es-w411dp-h1600dp")
class StoryLanguageRoutingTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private val container =
        AppContainer(ApplicationProvider.getApplicationContext<Application>(), audio = { FakeSoundPlayer() })
    private val spanish = container.storyEngine
    private val english = StoryEngine(StoryContentLoader.loadFromResources(language = "en"))

    private var language by mutableStateOf(AppLanguage.ES)

    /** A Chetwood fight: its zone, title and rivals have different names in English. */
    private val node = TreeShape.nodesOf("chet").first { spanish.node(it).encounter != null }

    private fun show(): NavHostController {
        val start = spanish.newGame()
        val seeded = start.copy(run = start.run.copy(node = node, path = listOf(node), phase = Phase.COMBAT_PENDING, level = 5))
        assertTrue(spanish.isValid(seeded))
        runBlocking {
            container.saveRepository.save(seeded)
            container.session.load()
        }
        lateinit var nav: NavHostController
        composeRule.setContent {
            val context = LocalContext.current
            nav = remember { TestNavHostController(context).apply { navigatorProvider.addNavigator(ComposeNavigator()) } }
            ProvideLanguageAndScript(language, AppScript.LATIN) { WardenTheme { WardenNavHost(container, nav) } }
        }
        composeRule.runOnUiThread {
            nav.navigateSingle(Routes.MAP)
            nav.navigateSingle(Routes.NODE)
        }
        composeRule.waitForIdle()
        return nav
    }

    private inline fun <reified T : ViewModel> viewModelAt(nav: NavHostController, route: String): T {
        val entry = nav.getBackStackEntry(route)
        val existingOnly = object : ViewModelProvider.Factory {
            override fun <V : ViewModel> create(modelClass: Class<V>): V = error("${T::class.simpleName} was not created by the route")
        }
        return ViewModelProvider(entry.viewModelStore, existingOnly)[T::class.java]
    }

    private fun rivalNames(engine: StoryEngine) = engine.node(node).encounter!!.rivals.map { it.name }

    @Test fun switchingToEnglishTranslatesNodeRivalsZonesAndCombat() {
        val nav = show()
        composeRule.onNodeWithText(spanish.node(node).title).assertExists()
        composeRule.onNodeWithText("Bosque de Chet").assertExists()

        language = AppLanguage.EN
        composeRule.waitForIdle()
        // The translation loads in the background; the screens switch when it is ready.
        composeRule.waitUntil(20_000) { container.storyTexts.value !== container.storyEngine }
        composeRule.waitForIdle()
        val title = english.node(node).title
        assertTrue(title != spanish.node(node).title)
        composeRule.onNodeWithText(title).assertExists()
        composeRule.onNodeWithText("The Chetwood").assertExists()
        rivalNames(english).forEach { name -> composeRule.onAllNodesWithText(name, substring = true).onFirst().assertExists() }
        composeRule.onNodeWithText("Fight!").assertExists()

        composeRule.runOnUiThread { nav.navigateSingle(Routes.COMBAT) }
        composeRule.waitForIdle()
        rivalNames(english).distinct().forEach { composeRule.onAllNodesWithText(it).onFirst().assertExists() }
        composeRule.onNodeWithText("The Chetwood").assertExists()

        language = AppLanguage.ES
        composeRule.waitForIdle()
        rivalNames(spanish).distinct().forEach { composeRule.onAllNodesWithText(it).onFirst().assertExists() }
        composeRule.onNodeWithText("Bosque de Chet").assertExists()

        language = AppLanguage.EN
        composeRule.runOnUiThread {
            nav.popBackStack()
            nav.popBackStack()
        }
        composeRule.waitForIdle()
        assertEquals(Routes.MAP, nav.currentBackStackEntry?.destination?.route)
        val map: MapViewModel = viewModelAt(nav, Routes.MAP)
        assertEquals(english.zones().map { it.name }, map.rows.value.map { it.name })
        assertEquals("Mount Doom", map.rows.value.last().name)
    }
}
