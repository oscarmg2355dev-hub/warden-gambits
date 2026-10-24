package com.wardengambits.app.ui.options

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.focus.onFocusChanged
import kotlinx.coroutines.launch
import com.wardengambits.app.i18n.annotateTengwar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wardengambits.app.R
import com.wardengambits.app.i18n.AppLanguage
import com.wardengambits.app.i18n.AppScript
import com.wardengambits.app.i18n.tengwar.Equivalence
import com.wardengambits.app.i18n.tengwar.EquivalenceKind
import com.wardengambits.app.i18n.tengwar.Tengwar
import com.wardengambits.app.i18n.tengwar.TengwarMode
import com.wardengambits.app.i18n.trText
import com.wardengambits.app.i18n.trTextResource
import com.wardengambits.app.ui.common.ScreenHeader
import com.wardengambits.app.ui.theme.AlcarinFamily
import com.wardengambits.app.ui.theme.WardenColors

private val TengwaStyle = TextStyle(fontFamily = AlcarinFamily, fontSize = 26.sp, lineHeight = 40.sp)
private val LatinStyle = TextStyle(fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold, fontSize = 18.sp)

/** Example consonant the vowel tehtar are shown on. */
private const val EXAMPLE_CONSONANT = "t"

/**
 * Options: music, effects, narration, language and script toggles, then the latin → tengwar equivalence table of the chosen
 * language's mode (built from the transcriber's tables; the latin letters always stay in Latin).
 */
@Composable
fun OptionsScreen(
    musicEnabled: Boolean,
    sfxEnabled: Boolean,
    narrationEnabled: Boolean,
    language: AppLanguage,
    script: AppScript,
    onToggleMusic: () -> Unit,
    onToggleSfx: () -> Unit,
    onToggleNarration: () -> Unit,
    onToggleLanguage: () -> Unit,
    onToggleScript: () -> Unit,
    onBack: () -> Unit,
) {
    Column(Modifier.fillMaxSize().imePadding()) {
        ScreenHeader(stringResource(R.string.menu_options), onBack)
        Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 24.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            OptionButton(toggleLabel(R.string.options_music, musicEnabled), onToggleMusic)
            OptionButton(toggleLabel(R.string.options_sfx, sfxEnabled), onToggleSfx)
            OptionButton(toggleLabel(R.string.options_narration, narrationEnabled), onToggleNarration)
            OptionButton(stringResource(R.string.options_language, language.displayName), onToggleLanguage, transcribe = false)
            val scriptName = stringResource(
                when (script) {
                    AppScript.LATIN -> R.string.options_script_latin
                    AppScript.TENGWAR -> R.string.options_script_tengwar
                },
            )
            OptionButton(stringResource(R.string.options_script, scriptName), onToggleScript, transcribe = false)
            Spacer(Modifier.height(24.dp))
            EquivalenceTable(language)
            Spacer(Modifier.height(24.dp))
            Transcriber(language)
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun toggleLabel(name: Int, enabled: Boolean): String =
    stringResource(if (enabled) R.string.options_toggle_on else R.string.options_toggle_off, stringResource(name))

/**
 * [transcribe] = false keeps the label in Latin with the normal font even in tengwar: the language and
 * script toggles must stay readable so the player can always switch back.
 */
@Composable
private fun OptionButton(label: String, onClick: () -> Unit, transcribe: Boolean = true) {
    Button(onClick = onClick, modifier = Modifier.padding(vertical = 6.dp).fillMaxWidth().heightIn(min = 48.dp)) {
        Text(if (transcribe) trText(label) else AnnotatedString(label), style = MaterialTheme.typography.labelLarge)
    }
}

@Composable
private fun EquivalenceTable(language: AppLanguage) {
    val mode = language.tengwarMode
    val rows = remember(mode) { Tengwar.equivalences(mode).groupBy { it.kind } }
    Text(
        trTextResource(R.string.options_equivalences_title),
        modifier = Modifier.fillMaxWidth(),
        style = MaterialTheme.typography.titleMedium,
        color = WardenColors.Primary,
    )
    EquivalenceKind.entries.forEach { kind ->
        val entries = rows[kind].orEmpty()
        if (entries.isEmpty()) return@forEach
        val heading = when (kind) {
            EquivalenceKind.CONSONANT -> R.string.options_equivalences_consonants
            EquivalenceKind.DIGRAPH -> R.string.options_equivalences_digraphs
            EquivalenceKind.VOWEL -> R.string.options_equivalences_vowels
        }
        Text(
            trTextResource(heading),
            modifier = Modifier.fillMaxWidth().padding(top = 16.dp, bottom = 8.dp),
            style = MaterialTheme.typography.bodyLarge,
            color = WardenColors.Text,
        )
        if (kind == EquivalenceKind.DIGRAPH && mode == TengwarMode.SPANISH) {
            Text(
                trTextResource(R.string.options_equivalences_digraphs_note),
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                style = MaterialTheme.typography.bodySmall,
                color = WardenColors.Text,
            )
        }
        FlowRow(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            entries.forEach { EquivalenceCell(it, mode) }
        }
    }
}

@Composable
private fun EquivalenceCell(eq: Equivalence, mode: TengwarMode) {
    Column(
        Modifier.width(if (eq.kind == EquivalenceKind.VOWEL) 96.dp else 64.dp)
            .clip(RoundedCornerShape(6.dp)).background(WardenColors.Surface)
            .padding(vertical = 6.dp)
            .semantics(mergeDescendants = true) {}
            .testTag("equivalence_${eq.latin}"),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // The latin letter is the reference: it is never transcribed.
        Text(eq.latin, style = LatinStyle, color = WardenColors.Primary)
        if (eq.kind == EquivalenceKind.VOWEL) {
            val example = remember(eq, mode) { Tengwar.transcribe(EXAMPLE_CONSONANT + eq.latin, mode) }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(eq.tengwar, style = TengwaStyle, color = WardenColors.Text)
                Text(example, style = TengwaStyle, color = WardenColors.Text)
            }
        } else {
            Text(eq.tengwar, style = TengwaStyle, color = WardenColors.Text)
        }
    }
}

/**
 * Live transcriber: the player types Latin text (always in the normal font) and sees its tengwar
 * transcription below. The mode is local to the form and starts at the app language's mode.
 */
@Composable
private fun Transcriber(language: AppLanguage) {
    var mode by rememberSaveable { mutableStateOf(language.tengwarMode) }
    var input by rememberSaveable { mutableStateOf("") }
    val requester = remember { BringIntoViewRequester() }
    val scope = rememberCoroutineScope()
    Column(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).background(WardenColors.Surface).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            trTextResource(R.string.transcriber_title),
            modifier = Modifier.fillMaxWidth(),
            style = MaterialTheme.typography.titleMedium,
            color = WardenColors.Primary,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            // Chip labels are language names: always Latin.
            FilterChip(
                selected = mode == TengwarMode.SPANISH,
                onClick = { mode = TengwarMode.SPANISH },
                label = { Text("Español") },
                modifier = Modifier.testTag("transcriber_mode_es"),
            )
            FilterChip(
                selected = mode == TengwarMode.ENGLISH,
                onClick = { mode = TengwarMode.ENGLISH },
                label = { Text("English") },
                modifier = Modifier.testTag("transcriber_mode_en"),
            )
        }
        OutlinedTextField(
            value = input,
            onValueChange = { input = it },
            modifier = Modifier.fillMaxWidth().bringIntoViewRequester(requester)
                .onFocusChanged { if (it.isFocused) scope.launch { requester.bringIntoView() } }
                .testTag("transcriber_input"),
            placeholder = { Text(stringResource(R.string.transcriber_placeholder)) },
            maxLines = 4,
        )
        if (input.isNotEmpty()) {
            val written = remember(input, mode) { annotateTengwar(Tengwar.transcribe(input, mode)) }
            SelectionContainer {
                Text(
                    written,
                    modifier = Modifier.fillMaxWidth().testTag("transcriber_output"),
                    style = TextStyle(fontSize = 24.sp, lineHeight = 38.sp),
                    color = WardenColors.Text,
                )
            }
        }
    }
}
