package com.wardengambits.app

import android.app.Application
import androidx.activity.ComponentActivity
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
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

/** Real [WardenNavHost]: «Acerca de» on the menu opens the about screen, and back returns to the menu. */
@RunWith(RobolectricTestRunner::class)
@Config(qualifiers = "es-w411dp-h1600dp")
class AboutRoutingTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private val container =
        AppContainer(ApplicationProvider.getApplicationContext<Application>(), audio = { FakeSoundPlayer() })

    private fun string(id: Int) = composeRule.activity.getString(id)

    @Test fun menuOpensAboutAndBackReturns() {
        lateinit var nav: NavHostController
        composeRule.setContent {
            nav = TestNavHostController(LocalContext.current).apply {
                navigatorProvider.addNavigator(ComposeNavigator())
            }
            WardenNavHost(container, nav)
        }
        composeRule.waitForIdle()
        composeRule.onNodeWithText(string(R.string.menu_about)).performClick()
        composeRule.waitForIdle()
        assertEquals(Routes.ABOUT, nav.currentBackStackEntry?.destination?.route)
        composeRule.onNodeWithText(string(R.string.about_author_heading)).assertExists()

        composeRule.onNodeWithContentDescription(string(R.string.back)).performClick()
        composeRule.waitForIdle()
        assertEquals(Routes.MENU, nav.currentBackStackEntry?.destination?.route)
    }
}
