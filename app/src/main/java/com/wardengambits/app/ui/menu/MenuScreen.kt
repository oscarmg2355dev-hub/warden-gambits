package com.wardengambits.app.ui.menu

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.TextButton
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wardengambits.app.R
import com.wardengambits.app.i18n.trText
import com.wardengambits.app.i18n.trTextResource

@Composable
fun MenuScreen(
    onStory: () -> Unit,
    onCharacter: () -> Unit,
    onHelp: () -> Unit,
    onNewAdventure: () -> Unit,
    onOptions: () -> Unit = {},
    onAbout: () -> Unit = {},
    onPractice: () -> Unit = {},
) {
    var confirmNew by rememberSaveable { mutableStateOf(false) }
    if (confirmNew) {
        AlertDialog(
            onDismissRequest = { confirmNew = false },
            title = { Text(trTextResource(R.string.new_adventure_title)) },
            text = { Text(trTextResource(R.string.new_adventure_message)) },
            confirmButton = {
                TextButton(onClick = { confirmNew = false; onNewAdventure() }) {
                    Text(trTextResource(R.string.new_adventure_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmNew = false }) { Text(trTextResource(R.string.cancel)) }
            },
        )
    }
    Surface(modifier = Modifier.fillMaxSize(), color = Color.Transparent, contentColor = MaterialTheme.colorScheme.onBackground) {
        // Scrollable so the six buttons are reachable on short or landscape screens; centred when they fit.
        BoxWithConstraints(Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxWidth().heightIn(min = maxHeight).verticalScroll(rememberScrollState()).padding(32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = trTextResource(R.string.menu_title),
                // 30 sp (displayMedium is 36): "Warden Gambits" fits one line on most phones and wraps whole words otherwise.
                style = MaterialTheme.typography.displayMedium.copy(fontSize = 30.sp),
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(48.dp))
            MenuButton(stringResource(R.string.menu_story), onStory)
            MenuButton(stringResource(R.string.menu_practice), onPractice)
            MenuButton(stringResource(R.string.menu_character), onCharacter)
            MenuButton(stringResource(R.string.menu_help), onHelp)
            MenuButton(stringResource(R.string.menu_new_adventure), onClick = { confirmNew = true })
            MenuButton(stringResource(R.string.menu_options), transcribe = false, onClick = onOptions)
            MenuButton(stringResource(R.string.menu_about), onAbout)
        }
        }
    }
}

/** [transcribe] = false keeps the label in Latin even in tengwar («Opciones» must stay findable to switch back). */
@Composable
private fun MenuButton(label: String, onClick: () -> Unit, transcribe: Boolean = true) {
    Button(onClick = onClick, modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        Text(if (transcribe) trText(label) else AnnotatedString(label), style = MaterialTheme.typography.labelLarge)
    }
}
