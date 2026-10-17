package com.wardengambits.app.ui.common

import androidx.compose.ui.test.assertHeightIsAtLeast
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertWidthIsAtLeast
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import com.wardengambits.app.ui.theme.WardenTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(qualifiers = "es-w411dp-h800dp")
class ScreenHeaderTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test fun backIsAnArrowButtonDescribedAsVolverThatCallsOnBack() {
        var back = 0
        composeRule.setContent { WardenTheme { ScreenHeader("Personaje", onBack = { back++ }) } }
        composeRule.onNodeWithText("Volver").assertDoesNotExist()
        val button = composeRule.onNode(hasContentDescription("Volver") and hasClickAction())
        button.assertIsDisplayed().assertWidthIsAtLeast(48.dp).assertHeightIsAtLeast(48.dp)
        button.performClick()
        assertEquals(1, back)
        composeRule.onNodeWithText("Personaje").assertIsDisplayed()
    }
}
