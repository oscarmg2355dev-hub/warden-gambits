package com.wardengambits.app.ui.character

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.unit.dp
import com.wardengambits.app.ui.theme.WardenTheme
import com.wardengambits.engine.model.Stats
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.math.abs

@RunWith(RobolectricTestRunner::class)
@Config(qualifiers = "es-w800dp-h1280dp")
class CharacterSheetLayoutTest {
    @get:Rule
    val composeRule = createComposeRule()

    private val ui = CharacterUi(
        level = 7, lap = 1, wins = 3, deaths = 0, zoneName = "Bree", progress = "1/7",
        stats = Stats(maxHp = 10_000, maxPower = 300, powerRegen = 5, attack = 1_000, heal = 900, defense = 500),
        history = emptyList(),
        completedLaps = 0,
    )

    @Test fun sheetIsCentredWithAMaxWidthOnAWideScreen() {
        composeRule.setContent {
            WardenTheme {
                Box(Modifier.fillMaxSize().testTag("root")) { CharacterScreen(ui = ui, onBack = {}, onHistory = {}) }
            }
        }
        val root = composeRule.onNodeWithTag("root").getUnclippedBoundsInRoot()
        val sheet = composeRule.onNodeWithTag(CHARACTER_SHEET_TAG).getUnclippedBoundsInRoot()
        val left = sheet.left - root.left
        val right = root.right - sheet.right
        assertTrue("left $left vs right $right", abs((left - right).value) <= 1f)
        assertTrue("width ${sheet.right - sheet.left}", (sheet.right - sheet.left) <= SHEET_MAX_WIDTH + 0.5.dp)
        // Short sheet on a tall screen: vertically centred in the space below the header.
        val top = sheet.top - root.top
        val bottom = root.bottom - sheet.bottom
        assertTrue("sheet should sit away from the top (top $top, bottom $bottom)", top > 100.dp && bottom > 100.dp)
    }
}
