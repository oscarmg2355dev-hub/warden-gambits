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
import androidx.compose.ui.test.performClick
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
import com.wardengambits.app.i18n.tengwar.Tengwar
import com.wardengambits.app.i18n.tengwar.TengwarMode
import com.wardengambits.app.navigation.Routes
import com.wardengambits.app.navigation.WardenNavHost
import com.wardengambits.app.navigation.navigateSingle
import com.wardengambits.app.ui.node.NodeViewModel
import com.wardengambits.app.ui.theme.WardenTheme
import com.wardengambits.engine.story.NodeId
import com.wardengambits.engine.story.Phase
import com.wardengambits.engine.story.content.StoryContentLoader
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** Real [WardenNavHost] under the language/script providers: switching them keeps the screen and its state. */
@RunWith(RobolectricTestRunner::class)
@Config(qualifiers = "es-w411dp-h1600dp")
class LanguageSwitchRoutingTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private val container =
        AppContainer(ApplicationProvider.getApplicationContext<Application>(), audio = { FakeSoundPlayer() })

    private var language by mutableStateOf(AppLanguage.ES)
    private var script by mutableStateOf(AppScript.LATIN)

    private fun show(): NavHostController {
        lateinit var nav: NavHostController
        composeRule.setContent {
            val context = LocalContext.current
            nav = remember {
                TestNavHostController(context).apply { navigatorProvider.addNavigator(ComposeNavigator()) }
            }
            ProvideLanguageAndScript(language, script) {
                WardenTheme { WardenNavHost(container, nav) }
            }
        }
        composeRule.waitForIdle()
        return nav
    }

    private fun nodeViewModel(nav: NavHostController): NodeViewModel {
        val entry = checkNotNull(nav.currentBackStackEntry)
        val existingOnly = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T = error("NodeViewModel was not created by the route")
        }
        return ViewModelProvider(entry.viewModelStore, existingOnly)[NodeViewModel::class.java]
    }

    @Test fun switchingLanguageAndScriptInTheMiddleOfANodeKeepsItsState() {
        val engine = container.storyEngine
        val start = engine.newGame()
        val node = NodeId("bree", 1, 0)
        val seeded = start.copy(run = start.run.copy(node = node, path = listOf(node), phase = Phase.CHOOSING))
        assertTrue(engine.isValid(seeded))
        runBlocking {
            container.saveRepository.save(seeded)
            container.session.load()
        }
        val nav = show()
        composeRule.runOnUiThread {
            nav.navigateSingle(Routes.MAP)
            nav.navigateSingle(Routes.NODE)
        }
        composeRule.waitForIdle()
        val vm = nodeViewModel(nav)
        val title = vm.node.value.title
        val stack = nav.currentBackStack.value.map { it.destination.route }
        composeRule.onNodeWithText(title).assertExists()

        language = AppLanguage.EN
        script = AppScript.TENGWAR
        composeRule.waitForIdle()
        // The translation loads in the background; the screens switch when it is ready.
        composeRule.waitUntil(20_000) { container.storyTexts.value !== container.storyEngine }
        composeRule.waitForIdle()
        assertEquals(Routes.NODE, nav.currentBackStackEntry?.destination?.route)
        assertEquals(stack, nav.currentBackStack.value.map { it.destination.route })
        assertSame(vm, nodeViewModel(nav))
        assertEquals(seeded, container.session.profile.value)
        // The story follows the language too: the English title, transcribed.
        val englishTitle = vm.node.value.title
        assertEquals(StoryContentLoader.loadZone("story", "bree", "en").nodes.first { it.level == 1 && it.index == 0 }.title, englishTitle)
        composeRule.onNodeWithText(title).assertDoesNotExist()
        composeRule.onNodeWithText(Tengwar.transcribe(englishTitle, TengwarMode.ENGLISH)).assertExists()
        // Numbers stay in Latin digits inside tengwar screens.
        composeRule.onNodeWithText("${vm.node.value.level}/${vm.node.value.total}").assertExists()

        language = AppLanguage.ES
        script = AppScript.LATIN
        composeRule.waitForIdle()
        assertSame(vm, nodeViewModel(nav))
        composeRule.onNodeWithText(title).assertExists()
    }

    @Test fun theMenuOpensTheOptionsScreen() {
        val nav = show()
        composeRule.onNodeWithText(composeRule.activity.getString(R.string.menu_options)).performClick()
        composeRule.waitForIdle()
        assertEquals(Routes.OPTIONS, nav.currentBackStackEntry?.destination?.route)
        composeRule.onNodeWithText("Idioma: Español").assertExists()
    }
}
