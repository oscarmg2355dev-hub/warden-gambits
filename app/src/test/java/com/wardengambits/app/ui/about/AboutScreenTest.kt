package com.wardengambits.app.ui.about

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.UriHandler
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.wardengambits.app.i18n.AppLanguage
import com.wardengambits.app.i18n.AppScript
import com.wardengambits.app.i18n.ProvideLanguageAndScript
import com.wardengambits.app.i18n.tengwar.Tengwar
import com.wardengambits.app.i18n.tengwar.TengwarMode
import com.wardengambits.app.ui.theme.WardenTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(qualifiers = "es-w411dp-h2400dp")
class AboutScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    private val opened = mutableListOf<String>()

    private fun show(language: AppLanguage = AppLanguage.ES, script: AppScript = AppScript.LATIN, onBack: () -> Unit = {}) {
        val uriHandler = object : UriHandler {
            override fun openUri(uri: String) { opened += uri }
        }
        composeRule.setContent {
            CompositionLocalProvider(LocalUriHandler provides uriHandler) {
                ProvideLanguageAndScript(language, script) { WardenTheme { AboutScreen(onBack = onBack) } }
            }
        }
    }

    @Test fun linksThePublicSourceCodeOnGitHub() {
        show()
        assertEquals("https://github.com/oscarmg2355dev-hub/warden-gambits", SOURCE_CODE_URL)
        composeRule.onNodeWithText("Código fuente").assertIsDisplayed()
        composeRule.onNodeWithText("GitHub", substring = true).assertIsDisplayed()
        composeRule.onNodeWithText(SOURCE_CODE_URL).assertIsDisplayed().performClick()
        assertEquals(listOf(SOURCE_CODE_URL), opened)
    }

    @Test fun sourceCodeLinkInEnglish() {
        show(AppLanguage.EN)
        composeRule.onNodeWithText("Source code").assertIsDisplayed()
        composeRule.onNodeWithText(SOURCE_CODE_URL).assertIsDisplayed()
    }

    @Test fun sourceCodeUrlStaysLatinInTengwar() {
        show(AppLanguage.ES, AppScript.TENGWAR)
        composeRule.onNodeWithText(Tengwar.transcribe("Código fuente", TengwarMode.SPANISH)).assertIsDisplayed()
        composeRule.onNodeWithText(SOURCE_CODE_URL).assertIsDisplayed()
    }

    @Test fun showsHowItWasMadeTheAuthorAndTheCredits() {
        var back = 0
        show(onBack = { back++ })
        composeRule.onNodeWithText("Acerca de").assertIsDisplayed()
        composeRule.onNodeWithText("Cómo se ha hecho").assertIsDisplayed()
        composeRule.onNodeWithText("El autor").assertIsDisplayed()
        composeRule.onNodeWithText("Oscar Martínez Gómez", substring = true).assertIsDisplayed()
        composeRule.onNodeWithText("LOTRO", substring = true).assertIsDisplayed()
        composeRule.onNodeWithText("Créditos").assertIsDisplayed()
        composeRule.onNodeWithText("Cinzel, Uncial Antiqua, Almendra y Alcarin Tengwar", substring = true).assertIsDisplayed()
        composeRule.onNodeWithText("J. R. R. Tolkien", substring = true).assertIsDisplayed()
        composeRule.onNodeWithText("sin relación", substring = true).assertIsDisplayed()
        composeRule.onNodeWithContentDescription("Volver").performClick()
        assertEquals(1, back)
    }

    @Test fun inEnglish() {
        show(AppLanguage.EN)
        composeRule.onNodeWithText("About").assertIsDisplayed()
        composeRule.onNodeWithText("How it was made").assertIsDisplayed()
        composeRule.onNodeWithText("The author").assertIsDisplayed()
        composeRule.onNodeWithText("Oscar Martínez Gómez", substring = true).assertIsDisplayed()
        composeRule.onNodeWithText("Credits").assertIsDisplayed()
        composeRule.onNodeWithText("Cinzel, Uncial Antiqua, Almendra and Alcarin Tengwar", substring = true).assertIsDisplayed()
        composeRule.onNodeWithText("not affiliated", substring = true).assertIsDisplayed()
    }

    @Test fun inTengwar() {
        show(AppLanguage.ES, AppScript.TENGWAR)
        composeRule.onNodeWithText("El autor").assertDoesNotExist()
        composeRule.onNodeWithText(Tengwar.transcribe("El autor", TengwarMode.SPANISH)).assertIsDisplayed()
        composeRule.onNodeWithText(Tengwar.transcribe("Créditos", TengwarMode.SPANISH)).assertIsDisplayed()
        composeRule.onNodeWithText("Oscar Martínez Gómez", substring = true).assertDoesNotExist()
    }

    @Test fun inEnglishTengwar() {
        show(AppLanguage.EN, AppScript.TENGWAR)
        composeRule.onNodeWithText(Tengwar.transcribe("The author", TengwarMode.ENGLISH)).assertIsDisplayed()
    }
}
