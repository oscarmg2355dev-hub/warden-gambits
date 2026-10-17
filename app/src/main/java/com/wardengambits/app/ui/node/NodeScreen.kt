package com.wardengambits.app.ui.node

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.dp
import com.wardengambits.app.R
import com.wardengambits.app.i18n.trText
import com.wardengambits.app.i18n.trTextResource
import com.wardengambits.app.ui.common.NodeImage
import com.wardengambits.app.ui.theme.CinzelFamily
import com.wardengambits.app.ui.theme.WardenColors

/** Text styles of the node screen (UI v2 §2). */
object NodeTextStyles {
    val title = TextStyle(fontFamily = CinzelFamily, fontWeight = FontWeight.Bold, fontSize = 24.sp)
    val body = TextStyle(fontFamily = FontFamily.Serif, fontSize = 18.sp, lineHeight = 26.sp)
    val rival = TextStyle(fontFamily = FontFamily.Serif, fontSize = 16.sp, lineHeight = 22.sp)
}

@Composable
fun NodeScreen(
    ui: NodeUi,
    onFight: () -> Unit,
    onChoose: (Int) -> Unit,
    onContinue: () -> Unit,
    onNextLap: () -> Unit,
    onMap: () -> Unit,
) {
    Surface(modifier = Modifier.fillMaxSize(), color = Color.Transparent, contentColor = MaterialTheme.colorScheme.onBackground) {
        Column(Modifier.fillMaxSize()) {
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    trText(ui.zoneName),
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.titleMedium,
                    color = WardenColors.Primary,
                )
                Text(trText("${ui.level}/${ui.total}"), color = WardenColors.Text, modifier = Modifier.padding(end = 8.dp))
                TextButton(onClick = onMap) { Text(trTextResource(R.string.node_map)) }
            }
            Column(Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState())) {
                NodeImage(ui.key, ui.zoneId, ui.zoneName, contentDescription = ui.title)
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(trText(ui.title), style = NodeTextStyles.title, color = WardenColors.Primary)
                    Text(trText(ui.text), style = NodeTextStyles.body, color = WardenColors.Text)
                    val actions = ui.actions
                    if (actions is NodeActions.Fight) {
                        actions.rivals.forEach { rival ->
                            Text(trTextResource(R.string.node_rival, rival.name, rival.level), style = NodeTextStyles.rival, color = WardenColors.Text)
                        }
                    }
                }
            }
            Column(
                Modifier.fillMaxWidth().padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                when (val actions = ui.actions) {
                    is NodeActions.Fight -> ActionButton(stringResource(R.string.node_fight), onFight)
                    is NodeActions.Choices -> actions.options.forEachIndexed { index, choice ->
                        ActionButton(choice.label.orEmpty()) { onChoose(index) }
                    }
                    NodeActions.Continue -> ActionButton(stringResource(R.string.node_continue), onContinue)
                    is NodeActions.NextLap ->
                        ActionButton(stringResource(R.string.node_next_lap, actions.nextLap), onNextLap)
                }
            }
        }
    }
}

@Composable
private fun ActionButton(label: String, onClick: () -> Unit) {
    Button(onClick = onClick, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) { Text(trText(label)) }
}
