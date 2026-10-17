package com.wardengambits.app

import androidx.compose.material3.Text
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.navigation.NavHostController
import androidx.navigation.compose.ComposeNavigator
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.testing.TestNavHostController
import com.wardengambits.app.navigation.navigateSingle
import com.wardengambits.app.navigation.safeBack
import androidx.compose.ui.platform.LocalContext
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class NavigationTest {
    @get:Rule
    val composeRule = createComposeRule()

    private fun setUp(): NavHostController {
        lateinit var nav: NavHostController
        composeRule.setContent {
            nav = TestNavHostController(LocalContext.current).apply {
                navigatorProvider.addNavigator(ComposeNavigator())
            }
            NavHost(nav, startDestination = "menu") {
                composable("menu") { Text("menu") }
                composable("map") { Text("map") }
            }
        }
        return nav
    }

    @Test fun doubleForwardAndDoubleBackKeepStartDestination() {
        val nav = setUp()
        composeRule.runOnUiThread {
            nav.navigateSingle("map")
            nav.navigateSingle("map")
        }
        composeRule.waitForIdle()
        assertEquals(1, nav.currentBackStack.value.count { it.destination.route == "map" })
        composeRule.runOnUiThread { nav.safeBack(); nav.safeBack() }
        composeRule.waitForIdle()
        assertEquals("menu", nav.currentBackStackEntry?.destination?.route)
    }
}
