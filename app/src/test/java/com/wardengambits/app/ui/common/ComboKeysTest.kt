package com.wardengambits.app.ui.common

import androidx.activity.ComponentActivity
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertContentDescriptionEquals
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.onRoot
import com.wardengambits.app.ui.theme.WardenTheme
import androidx.compose.ui.semantics.getOrNull
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ComboKeysTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun comboKeysShowsDigitsInOrder() {
        composeRule.setContent { WardenTheme { ComboKeys("121") } }
        composeRule.onAllNodesWithText("1").assertCountEquals(2)
        composeRule.onAllNodesWithText("2").assertCountEquals(1)
        val texts = composeRule.onRoot(useUnmergedTree = true).fetchSemanticsNode()
        val order = mutableListOf<String>()
        fun walk(n: androidx.compose.ui.semantics.SemanticsNode) {
            n.config.getOrNull(androidx.compose.ui.semantics.SemanticsProperties.Text)?.let { order.add(it.joinToString("") { s -> s.text }) }
            n.children.forEach(::walk)
        }
        walk(texts)
        assertEquals(listOf("1", "2", "1"), order)
    }

    @Test
    fun keyColorsMatchSpec() {
        assertEquals(Color(0xFFC62828), KeyColors.background('1'))
        assertEquals(Color(0xFF0097A7), KeyColors.background('2'))
        assertEquals(Color(0xFFF9A825), KeyColors.background('3'))
        assertEquals(Color(0xFF2E7D32), KeyColors.actionBackground)
        assertEquals(Color.White, KeyColors.content('1'))
        assertEquals(Color.White, KeyColors.content('2'))
        assertEquals(Color.Black, KeyColors.content('3'))
    }

    @Test
    fun actionKeyHasContentDescriptionAndClicks() {
        var clicks = 0
        composeRule.setContent {
            WardenTheme { KeyButton(null, { clicks++ }, Modifier.testTag("k")) }
        }
        composeRule.onNodeWithTag("k").assertContentDescriptionEquals("Acción")
        composeRule.onNodeWithTag("k").performClick()
        assertEquals(1, clicks)
    }
}
