package com.wardengambits.app.ui.ending

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import com.wardengambits.app.audio.NarrationController
import com.wardengambits.app.ui.common.NarrationButton
import com.wardengambits.app.ui.common.NarrationButtonView
import com.wardengambits.app.ui.common.rememberNarration
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.wardengambits.app.R
import com.wardengambits.app.i18n.trText
import com.wardengambits.app.i18n.trTextResource
import com.wardengambits.app.ui.common.NodeImage
import com.wardengambits.app.ui.node.NodeTextStyles
import com.wardengambits.app.ui.theme.WardenColors

/** "¡Has completado la aventura!": the final node, its epilogue and the summary of the completed lap. */
@Composable
fun NarratedEndingScreen(
    ui: EndingUi,
    narration: NarrationController,
    narrationEnabled: Boolean,
    onNextLap: () -> Unit,
    onMenu: () -> Unit,
) {
    val button = rememberNarration(ui.key, narration, narrationEnabled)
    EndingScreen(
        ui = ui,
        onNextLap = { narration.onLeave(); onNextLap() },
        onMenu = { narration.onLeave(); onMenu() },
        narration = button,
    )
}

@Composable
fun EndingScreen(ui: EndingUi, onNextLap: () -> Unit, onMenu: () -> Unit, narration: NarrationButton? = null) {
    Surface(modifier = Modifier.fillMaxSize(), color = Color.Transparent, contentColor = MaterialTheme.colorScheme.onBackground) {
        Column(Modifier.fillMaxSize()) {
            Text(
                trTextResource(R.string.ending_title),
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                style = NodeTextStyles.title,
                color = WardenColors.Primary,
                textAlign = TextAlign.Center,
            )
            Column(Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState())) {
                NodeImage(ui.key, ui.zoneId, ui.zoneName, contentDescription = ui.title)
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(trText(ui.title), style = MaterialTheme.typography.titleLarge, color = WardenColors.Primary)
                    if (narration != null) NarrationButtonView(narration, Modifier.align(Alignment.End))
                    Text(trText(ui.text), style = NodeTextStyles.body, color = WardenColors.Text)
                    Summary(ui)
                }
            }
            Column(
                Modifier.fillMaxWidth().padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Button(onClick = onNextLap, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) {
                    Text(trTextResource(R.string.node_next_lap, ui.nextLap))
                }
                OutlinedButton(onClick = onMenu, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) {
                    Text(trTextResource(R.string.ending_menu))
                }
            }
        }
    }
}

@Composable
private fun Summary(ui: EndingUi) {
    Column(
        Modifier.fillMaxWidth().background(WardenColors.Surface, RoundedCornerShape(8.dp)).padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            trTextResource(R.string.ending_lap_completed, ui.lap),
            style = MaterialTheme.typography.titleMedium,
            color = WardenColors.Primary,
        )
        Text(trTextResource(R.string.ending_level, ui.level, ui.levelsGained), color = WardenColors.Text)
        Text(trTextResource(R.string.ending_wins, ui.wins), color = WardenColors.Text)
        Text(trTextResource(R.string.character_deaths, ui.deaths), color = WardenColors.Text)
    }
}
