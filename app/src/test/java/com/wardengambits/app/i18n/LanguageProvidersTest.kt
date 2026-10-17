package com.wardengambits.app.i18n

import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.wardengambits.app.i18n.tengwar.Tengwar
import com.wardengambits.app.i18n.tengwar.TengwarMode
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class LanguageProvidersTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test fun stringResourcesAndLocalsFollowTheChosenLanguageAndScript() {
        var language by mutableStateOf(AppLanguage.ES)
        var script by mutableStateOf(AppScript.LATIN)
        composeRule.setContent {
            ProvideLanguageAndScript(language, script) {
                // A framework string translated in both languages proves stringResource reads the chosen locale.
                Text(stringResource(android.R.string.cancel))
                Text("ctx:" + LocalContext.current.resources.configuration.locales[0].language)
                Text("cfg:" + LocalConfiguration.current.locales[0].language)
                Text("lang:" + LocalAppLanguage.current.code + " script:" + LocalScript.current.code)
                Text(trText("Moria"))
            }
        }
        composeRule.onNodeWithText("Cancelar").assertIsDisplayed()
        composeRule.onNodeWithText("ctx:es").assertIsDisplayed()
        composeRule.onNodeWithText("cfg:es").assertIsDisplayed()
        composeRule.onNodeWithText("lang:es script:latin").assertIsDisplayed()
        composeRule.onNodeWithText("Moria").assertIsDisplayed()

        language = AppLanguage.EN
        script = AppScript.TENGWAR
        composeRule.waitForIdle()
        composeRule.onNodeWithText("Cancel").assertIsDisplayed()
        composeRule.onNodeWithText("ctx:en").assertIsDisplayed()
        composeRule.onNodeWithText("cfg:en").assertIsDisplayed()
        composeRule.onNodeWithText("lang:en script:tengwar").assertIsDisplayed()
        composeRule.onNodeWithText(Tengwar.transcribe("Moria", TengwarMode.ENGLISH)).assertIsDisplayed()
    }

    @Test fun aChangingNumberIsKeptOutOfTheTranscription() {
        composeRule.setContent {
            ProvideLanguageAndScript(AppLanguage.ES, AppScript.TENGWAR) {
                Text(trTextKeepingNumber("3/7  ·  Tick 42", 42))
            }
        }
        composeRule.onNodeWithText(Tengwar.transcribe("3/7  ·  Tick ", TengwarMode.SPANISH) + "42").assertIsDisplayed()
    }
}
