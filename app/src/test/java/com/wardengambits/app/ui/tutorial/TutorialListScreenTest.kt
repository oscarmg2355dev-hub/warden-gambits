package com.wardengambits.app.ui.tutorial

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.wardengambits.app.i18n.AppLanguage
import com.wardengambits.app.i18n.AppScript
import com.wardengambits.app.i18n.ProvideLanguageAndScript
import com.wardengambits.app.i18n.tengwar.Tengwar
import com.wardengambits.app.i18n.tengwar.TengwarMode
import com.wardengambits.app.ui.theme.WardenTheme
import com.wardengambits.engine.tutorial.TutorialLessons
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class TutorialListScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    private val lessons = TutorialLessons.load()

    private fun show(
        done: Set<String> = emptySet(),
        opened: MutableList<String> = mutableListOf(),
        language: AppLanguage = AppLanguage.ES,
        script: AppScript = AppScript.LATIN,
    ) {
        val vm = TutorialViewModel(lessons, MutableStateFlow(done))
        composeRule.setContent {
            ProvideLanguageAndScript(language, script) {
                WardenTheme { TutorialListScreen(vm.lessons.value, onLesson = { opened += it }, onBack = {}) }
            }
        }
    }

    @Test fun viewModelListsTheFiveLessonsWithTheirDoneMark() {
        val done = MutableStateFlow(setOf("daze"))
        val vm = TutorialViewModel(lessons, done)
        assertEquals(listOf("attack", "defense", "daze", "drain", "finisher"), vm.lessons.value.map { it.id })
        assertEquals(listOf(false, false, true, false, false), vm.lessons.value.map { it.done })
    }

    @Test fun everyLessonHasANameAnIntroAndOneTextPerStep() {
        lessons.forEach { lesson ->
            val texts = requireNotNull(TutorialTexts.of(lesson.id)) { lesson.id }
            assertEquals(lesson.id, lesson.steps.size, texts.steps.size)
        }
    }

    @Test fun showsFiveLessonsWithCheckOnTheCompletedOnes() {
        show(done = setOf("attack", "drain"))
        composeRule.onNodeWithText("Campo de entrenamiento").assertIsDisplayed()
        listOf("Ataque", "Defensa", "Aturdimiento", "Robo de vida", "Remate").forEach {
            composeRule.onNodeWithText(it).assertIsDisplayed()
        }
        composeRule.onAllNodesWithText("✔").assertCountEquals(2)
        composeRule.onNodeWithTag("tutorial_done_attack", useUnmergedTree = true).assertExists()
        composeRule.onNodeWithTag("tutorial_done_drain", useUnmergedTree = true).assertExists()
        composeRule.onNodeWithTag("tutorial_done_defense", useUnmergedTree = true).assertDoesNotExist()
    }

    @Test fun tappingALessonOpensIt() {
        val opened = mutableListOf<String>()
        show(opened = opened)
        composeRule.onNodeWithTag("tutorial_lesson_daze").performClick()
        assertEquals(listOf("daze"), opened)
    }

    @Test fun worksInEnglish() {
        show(language = AppLanguage.EN)
        composeRule.onNodeWithText("Training ground").assertIsDisplayed()
        composeRule.onNodeWithText("Attack").assertIsDisplayed()
    }

    @Test fun worksInTengwar() {
        show(script = AppScript.TENGWAR)
        composeRule.onNodeWithText("Ataque").assertDoesNotExist()
        composeRule.onNodeWithText(Tengwar.transcribe("Ataque", TengwarMode.SPANISH)).assertIsDisplayed()
    }
}
