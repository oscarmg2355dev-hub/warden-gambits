package com.wardengambits.app.ui.tutorial

import android.app.Application
import android.os.Looper
import androidx.activity.ComponentActivity
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavHostController
import androidx.navigation.compose.ComposeNavigator
import androidx.navigation.testing.TestNavHostController
import androidx.test.core.app.ApplicationProvider
import com.wardengambits.app.AppContainer
import com.wardengambits.app.R
import com.wardengambits.app.audio.FakeSoundPlayer
import com.wardengambits.app.i18n.AppLanguage
import com.wardengambits.app.i18n.AppScript
import com.wardengambits.app.i18n.ProvideLanguageAndScript
import com.wardengambits.app.i18n.tengwar.Tengwar
import com.wardengambits.app.i18n.tengwar.TengwarMode
import com.wardengambits.app.navigation.Routes
import com.wardengambits.app.navigation.WardenNavHost
import com.wardengambits.app.navigation.navigateSingle
import com.wardengambits.app.ui.combat.CombatScreen
import com.wardengambits.app.ui.combat.CombatViewModel
import com.wardengambits.app.ui.combat.TutorialCombatSource
import com.wardengambits.app.ui.theme.WardenTheme
import com.wardengambits.engine.combo.Input
import com.wardengambits.engine.rules.CombatEngine
import com.wardengambits.engine.tutorial.TutorialLessons
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import java.time.Duration

/** Real [WardenNavHost] and [AppContainer]: a guided lesson from its intro to «¡Lección completada!». */
@RunWith(RobolectricTestRunner::class)
@Config(qualifiers = "es-w360dp-h640dp")
class TutorialFlowTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private val container = AppContainer(ApplicationProvider.getApplicationContext<Application>(), audio = { FakeSoundPlayer() })
    private val attack = TutorialLessons.load().first { it.id == "attack" }

    /** Lesson texts hold a literal «%», so only strings with arguments go through formatting. */
    private fun str(id: Int, vararg args: Any): String =
        if (args.isEmpty()) composeRule.activity.getString(id) else composeRule.activity.getString(id, *args)

    private fun open(lesson: String = "attack"): NavHostController {
        lateinit var nav: NavHostController
        composeRule.setContent {
            nav = TestNavHostController(LocalContext.current).apply { navigatorProvider.addNavigator(ComposeNavigator()) }
            WardenNavHost(container, nav)
        }
        composeRule.runOnUiThread {
            nav.navigateSingle(Routes.TUTORIAL)
            nav.navigateSingle(Routes.tutorialCombat(lesson))
        }
        composeRule.waitForIdle()
        assertEquals(Routes.TUTORIAL_COMBAT, nav.currentBackStackEntry?.destination?.route)
        return nav
    }

    private fun vm(nav: NavHostController): CombatViewModel {
        val entry = checkNotNull(nav.currentBackStackEntry)
        val existingOnly = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T = error("not created by the route")
        }
        return ViewModelProvider(entry.viewModelStore, existingOnly)[CombatViewModel::class.java]
    }

    private fun understood() {
        composeRule.onNodeWithText(str(R.string.tutorial_understood)).performClick()
        composeRule.waitForIdle()
    }

    /** Marks [sequence], casts it and lets the next tick resolve it. */
    private fun cast(vm: CombatViewModel, sequence: String) {
        composeRule.runOnUiThread {
            sequence.forEach { key -> vm.press(Input.entries.first { it.code == key }) }
            vm.execute()
        }
        repeat(2) {
            shadowOf(Looper.getMainLooper()).idleFor(Duration.ofSeconds(1))
            composeRule.waitForIdle()
        }
    }

    @Test fun stepWaitsForUnderstood() {
        val vm = vm(open())
        composeRule.onNodeWithText(str(R.string.tutorial_attack_intro)).assertIsDisplayed()
        composeRule.onNodeWithText(str(R.string.tutorial_step_of, 1, 5)).assertIsDisplayed()
        assertTrue(vm.state.value.paused)
        assertFalse(vm.state.value.inputsEnabled)
        // The plain pause overlay does not cover the lesson text.
        composeRule.onNodeWithText(str(R.string.combat_resume)).assertDoesNotExist()
        shadowOf(Looper.getMainLooper()).idleFor(Duration.ofSeconds(3))
        assertEquals(0, vm.state.value.tick)
        understood()
        assertFalse(vm.state.value.paused)
        assertTrue(vm.state.value.inputsEnabled)
        composeRule.onNodeWithTag("btn_spear").performClick()
        composeRule.waitForIdle()
        assertEquals("1", vm.state.value.buffer)
    }

    @Test fun correctComboAdvances() {
        val vm = vm(open())
        understood()
        cast(vm, "11")
        composeRule.onNodeWithText(str(R.string.tutorial_step_of, 2, 5)).assertIsDisplayed()
        composeRule.onNodeWithText(str(R.string.tutorial_attack_step_1)).assertIsDisplayed()
        assertTrue("a new text pauses the fight", vm.state.value.paused)
        assertEquals(1, vm.state.value.tutorial?.step)
    }

    @Test fun wrongComboShowsHint() {
        val vm = vm(open())
        understood()
        cast(vm, "12")
        composeRule.onNodeWithText(str(R.string.tutorial_hint, "1 1")).assertIsDisplayed()
        assertEquals(0, vm.state.value.tutorial?.step)
    }

    @Test fun lessonCompletes() {
        val vm = vm(open())
        attack.steps.forEach { step ->
            understood()
            cast(vm, step)
        }
        composeRule.onNodeWithText(str(R.string.tutorial_completed)).assertIsDisplayed()
        composeRule.onNodeWithText(str(R.string.tutorial_next)).assertIsDisplayed()
        composeRule.onNodeWithText(str(R.string.tutorial_back_to_map)).assertIsDisplayed()
        assertNull("nobody dies in the tutorial", vm.state.value.outcome)
        // Saved in the settings' DataStore (on its own thread) and shown by the container's flow on the main looper.
        repeat(250) {
            if (container.tutorialDone.value?.contains("attack") == true) return@repeat
            shadowOf(Looper.getMainLooper()).idle()
            Thread.sleep(20)
        }
        assertTrue(container.tutorialDone.value?.contains("attack") == true)
        // The settings store is shared by the tests of this process, so other lessons may be marked too.
        assertTrue("attack" in runBlocking { container.settingsRepository.tutorialDone.first() })
    }

    @Test fun nextLessonOpensTheFollowingOne() {
        val nav = open()
        val vm = vm(nav)
        attack.steps.forEach { step ->
            understood()
            cast(vm, step)
        }
        composeRule.onNodeWithText(str(R.string.tutorial_next)).performClick()
        composeRule.waitForIdle()
        assertEquals("defense", nav.currentBackStackEntry?.arguments?.getString("lesson"))
        composeRule.onNodeWithText(str(R.string.tutorial_defense_intro)).assertIsDisplayed()
    }

    @Test fun lastLessonIsReadyForBree() {
        val finisher = TutorialLessons.load().last()
        val vm = vm(open(finisher.id))
        finisher.steps.forEach { step ->
            understood()
            cast(vm, step)
        }
        composeRule.onNodeWithText(str(R.string.tutorial_ready)).assertIsDisplayed()
        composeRule.onNodeWithText(str(R.string.tutorial_next)).assertDoesNotExist()
    }

    @Test fun dummiesShowTheirTranslatedName() {
        val vm = vm(open())
        assertEquals(listOf(str(R.string.tutorial_dummy)), vm.state.value.rivals.map { it.name })
    }

    @Test fun tutorialNeverTouchesProfile() {
        val before = container.session.profile.value
        val vm = vm(open())
        attack.steps.forEach { step ->
            understood()
            cast(vm, step)
        }
        composeRule.onNodeWithText(str(R.string.tutorial_back_to_map)).performClick()
        composeRule.waitForIdle()
        assertEquals(before, container.session.profile.value)
    }

    private fun showPanel(language: AppLanguage, script: AppScript) {
        val vm = CombatViewModel(
            TutorialCombatSource("attack", TutorialLessons.load(), "Attack", "Training dummy") {},
            MutableStateFlow(container.storyEngine),
            CombatEngine(),
            { 0L },
        )
        composeRule.setContent {
            ProvideLanguageAndScript(language, script) {
                WardenTheme { CombatScreen(vm.state.value, {}, {}, {}, {}, {}, {}, {}) }
            }
        }
    }

    @Test fun panelInEnglish() {
        showPanel(AppLanguage.EN, AppScript.LATIN)
        composeRule.onNodeWithText("Step 1 of 5").assertIsDisplayed()
        composeRule.onNodeWithText("Got it").assertIsDisplayed()
    }

    @Test fun panelInTengwar() {
        showPanel(AppLanguage.ES, AppScript.TENGWAR)
        val understood = str(R.string.tutorial_understood)
        composeRule.onNodeWithText(understood).assertDoesNotExist()
        composeRule.onNodeWithText(Tengwar.transcribe(understood, TengwarMode.SPANISH)).assertIsDisplayed()
    }
}
