package com.wardengambits.app

import android.app.Application
import androidx.activity.ComponentActivity
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.navigation.NavHostController
import androidx.navigation.compose.ComposeNavigator
import androidx.navigation.testing.TestNavHostController
import androidx.test.core.app.ApplicationProvider
import com.wardengambits.app.audio.FakeSoundPlayer
import com.wardengambits.app.navigation.Routes
import com.wardengambits.app.navigation.WardenNavHost
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** Real [WardenNavHost]: the character sheet opens the lap chronicle, and back returns to the sheet. */
@RunWith(RobolectricTestRunner::class)
@Config(qualifiers = "es-w411dp-h1600dp")
class CharacterRoutingTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private val container =
        AppContainer(ApplicationProvider.getApplicationContext<Application>(), audio = { FakeSoundPlayer() })

    private fun string(id: Int) = composeRule.activity.getString(id)

    @Test fun chronicleOpensTheHistoryAndBackReturnsToTheSheet() {
        lateinit var nav: NavHostController
        composeRule.setContent {
            nav = TestNavHostController(LocalContext.current).apply {
                navigatorProvider.addNavigator(ComposeNavigator())
            }
            WardenNavHost(container, nav)
        }
        composeRule.waitForIdle()
        composeRule.onNodeWithText(string(R.string.menu_character)).performClick()
        composeRule.waitForIdle()
        assertEquals(Routes.CHARACTER, nav.currentBackStackEntry?.destination?.route)

        composeRule.onNode(hasText(string(R.string.character_chronicle)) and hasClickAction()).performScrollTo().performClick()
        composeRule.waitForIdle()
        assertEquals(Routes.HISTORY, nav.currentBackStackEntry?.destination?.route)
        composeRule.onNodeWithText(string(R.string.history_empty)).assertExists()

        composeRule.onNodeWithContentDescription(string(R.string.back)).performClick()
        composeRule.waitForIdle()
        assertEquals(Routes.CHARACTER, nav.currentBackStackEntry?.destination?.route)
    }
}
