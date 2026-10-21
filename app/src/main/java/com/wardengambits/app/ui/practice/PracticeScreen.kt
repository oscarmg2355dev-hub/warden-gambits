package com.wardengambits.app.ui.practice

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.disabled
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.wardengambits.app.R
import com.wardengambits.app.i18n.trText
import com.wardengambits.app.ui.combat.EliteRibbon
import com.wardengambits.app.ui.common.Portrait
import com.wardengambits.app.ui.common.ScreenHeader
import com.wardengambits.app.ui.theme.WardenColors
import com.wardengambits.engine.story.NodeId

private val PortraitSize = 72.dp
private val LockedText = WardenColors.Text.copy(alpha = 0.55f)

/** Practice grid: every encounter of the story by zone; locked ones are greyed out, the rest start a practice fight. */
@Composable
fun PracticeScreen(zones: List<PracticeZoneUi>, initialZoneIndex: Int, onFight: (NodeId) -> Unit, onBack: () -> Unit) {
    // Flat index of the current zone's header: one header plus the cells of each earlier zone.
    val startIndex = remember(zones, initialZoneIndex) {
        zones.take(initialZoneIndex.coerceIn(0, (zones.size - 1).coerceAtLeast(0))).sumOf { it.cells.size + 1 }
    }
    val state = rememberLazyGridState(initialFirstVisibleItemIndex = startIndex)
    Column(Modifier.fillMaxSize()) {
        ScreenHeader(stringResource(R.string.practice_title), onBack)
        LazyVerticalGrid(
            columns = GridCells.Adaptive(104.dp),
            state = state,
            modifier = Modifier.fillMaxSize().testTag("practice_grid"),
            contentPadding = PaddingValues(12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            zones.forEach { zone ->
                item(key = "zone_${zone.zoneId}", span = { GridItemSpan(maxLineSpan) }) {
                    Text(
                        trText(zone.name),
                        style = MaterialTheme.typography.titleMedium,
                        color = WardenColors.Primary,
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    )
                }
                items(zone.cells, key = { it.node.key }) { cell -> PracticeCell(cell, onFight) }
            }
        }
    }
}

@Composable
private fun PracticeCell(cell: PracticeCellUi, onFight: (NodeId) -> Unit) {
    val shape = RoundedCornerShape(8.dp)
    val border = if (cell.isBoss) Color(0xFFB3261E) else MaterialTheme.colorScheme.outline
    Column(
        Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(Color(0x66000000))
            .border(BorderStroke(1.dp, border), shape)
            .then(if (cell.unlocked) Modifier.clickable { onFight(cell.node) } else Modifier.semantics { disabled() })
            .padding(6.dp)
            .testTag("practice_cell_${cell.node.key}"),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(Modifier.size(PortraitSize).clip(RoundedCornerShape(6.dp)), contentAlignment = Alignment.Center) {
            Portrait(cell.portrait, PortraitSize, dimmed = !cell.unlocked)
            if (cell.isBoss && cell.unlocked) EliteRibbon(PortraitSize)
            if (!cell.unlocked) Text("🔒", style = MaterialTheme.typography.headlineSmall, color = WardenColors.Text)
            if (cell.unlocked && cell.rivalCount > 1) {
                Text(
                    "×${cell.rivalCount}",
                    color = Color.White,
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.align(Alignment.BottomEnd).background(Color(0xAA000000)).padding(horizontal = 4.dp),
                )
            }
        }
        if (cell.unlocked) {
            Text(
                trText(cell.title),
                color = WardenColors.Text,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                trText(stringResource(R.string.practice_level, cell.playerLevel)),
                style = MaterialTheme.typography.labelSmall,
                color = WardenColors.Primary,
            )
        } else {
            Text(trText(stringResource(R.string.practice_locked)), color = LockedText, style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.Center)
        }
    }
}
