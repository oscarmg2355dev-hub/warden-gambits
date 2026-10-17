package com.wardengambits.app.ui.node

import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.unit.sp
import com.wardengambits.app.ui.theme.CinzelFamily
import com.wardengambits.app.ui.theme.WardenTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class NodeTextSizeTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test fun styleConstants() {
        assertEquals(24.sp, NodeTextStyles.title.fontSize)
        assertEquals(CinzelFamily, NodeTextStyles.title.fontFamily)
        assertEquals(18.sp, NodeTextStyles.body.fontSize)
        assertEquals(26.sp, NodeTextStyles.body.lineHeight)
        assertEquals(16.sp, NodeTextStyles.rival.fontSize)
    }

    private fun sizeOf(text: String) = composeRule.onNodeWithText(text).fetchSemanticsNode().config
        .getOrNull(SemanticsActions.GetTextLayoutResult)!!.action!!.let {
            val out = mutableListOf<TextLayoutResult>()
            it(out)
            out.single().layoutInput.style
        }

    @Test fun screenUsesStyles() {
        composeRule.setContent {
            WardenTheme {
                NodeScreen(
                    NodeUi("k", "bree", "Bree", 1, 7, "Titulo X", "Cuerpo", NodeActions.Fight(listOf(RivalUi("Orco", 3)))),
                    {}, {}, {}, {}, {},
                )
            }
        }
        assertEquals(24.sp, sizeOf("Titulo X").fontSize)
        assertEquals(18.sp, sizeOf("Cuerpo").fontSize)
        assertEquals(26.sp, sizeOf("Cuerpo").lineHeight)
        assertEquals(16.sp, sizeOf("Orco · Nv 3").fontSize)
    }
}
