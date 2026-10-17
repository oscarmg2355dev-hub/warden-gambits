package com.wardengambits.app.ui.character

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wardengambits.app.R
import com.wardengambits.app.i18n.trText
import com.wardengambits.app.i18n.trTextResource
import com.wardengambits.app.ui.common.ScreenHeader
import com.wardengambits.app.ui.theme.UncialFamily
import com.wardengambits.app.ui.theme.WardenColors
import com.wardengambits.engine.story.RunResult

/** «Crónica de vueltas»: one parchment entry per finished lap, most recent first. */
@Composable
fun HistoryScreen(history: List<HistoryRowUi>, onBack: () -> Unit) {
    // Transparent: the app-wide RPG pattern shows around the entries.
    Surface(modifier = Modifier.fillMaxSize(), color = Color.Transparent, contentColor = WardenColors.Text) {
        Column(Modifier.fillMaxSize()) {
            ScreenHeader(stringResource(R.string.character_chronicle), onBack)
            // Entries are centred (at most SHEET_MAX_WIDTH wide) and, when few, vertically centred too.
            LazyColumn(
                Modifier.fillMaxWidth().weight(1f).padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterVertically),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                if (history.isEmpty()) {
                    item {
                        Text(
                            trTextResource(R.string.history_empty),
                            modifier = Modifier.widthIn(max = SHEET_MAX_WIDTH).fillMaxWidth().padding(vertical = 12.dp).parchment().padding(24.dp),
                            style = AlmendraBody.copy(fontStyle = FontStyle.Italic),
                            textAlign = TextAlign.Center,
                        )
                    }
                }
                items(history) { HistoryEntry(it) }
                item { Box(Modifier.padding(bottom = 6.dp)) }
            }
        }
    }
}

@Composable
private fun HistoryEntry(row: HistoryRowUi) {
    Box(Modifier.widthIn(max = SHEET_MAX_WIDTH).fillMaxWidth().parchment()) {
        Column(Modifier.fillMaxWidth().padding(start = 18.dp, top = 16.dp, bottom = 16.dp, end = if (row.completed) 64.dp else 18.dp)) {
            val result = when (row.result) {
                RunResult.COMPLETED -> stringResource(R.string.history_result_completed)
                RunResult.DEFEATED -> stringResource(R.string.history_result_defeated)
            }
            Text(trTextResource(R.string.history_head, row.lap, result), style = UncialHeading)
            Text(
                trTextResource(
                    R.string.history_detail,
                    row.place,
                    row.level,
                    pluralStringResource(R.plurals.victories, row.wins, row.wins),
                    pluralStringResource(R.plurals.history_deaths, row.deaths, row.deaths),
                ),
                modifier = Modifier.padding(top = 4.dp),
                style = AlmendraBody.copy(fontSize = 16.sp),
            )
            Text(
                trText(row.date),
                modifier = Modifier.padding(top = 2.dp),
                style = AlmendraBody.copy(fontSize = 14.sp, fontStyle = FontStyle.Italic, color = ParchmentColors.Ink.copy(alpha = 0.8f)),
            )
        }
        if (row.completed) {
            val sealDescription = stringResource(R.string.history_completed_seal)
            WaxSeal(
                SealColors.Green,
                40.dp,
                Modifier.align(Alignment.CenterEnd).padding(end = 16.dp)
                    .clearAndSetSemantics { contentDescription = sealDescription },
            ) {
                Text("✔", style = TextStyle(fontFamily = UncialFamily, fontSize = 18.sp, color = ParchmentColors.Gold))
            }
        }
    }
}
