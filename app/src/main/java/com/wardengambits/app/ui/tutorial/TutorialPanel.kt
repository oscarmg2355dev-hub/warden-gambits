package com.wardengambits.app.ui.tutorial

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wardengambits.app.R
import com.wardengambits.app.i18n.trText
import com.wardengambits.app.i18n.trTextResource
import com.wardengambits.app.ui.combat.TutorialUi
import com.wardengambits.app.ui.common.ComboKeys
import com.wardengambits.app.ui.common.KeyColors
import com.wardengambits.app.ui.theme.WardenColors
import kotlinx.coroutines.delay

private const val CHECK_MILLIS = 900L
private val KeySize = 26.dp
private val PanelShape = RoundedCornerShape(8.dp)

/** Keys of [sequence] separated by spaces, as the hint names them ("1 2"). */
internal fun spacedKeys(sequence: String): String = sequence.toList().joinToString(" ")

/**
 * The lesson panel over the combat keys: lesson name, «Paso n de m», the intro (first time) and the step text, the
 * requested combo as key chips plus ⚔, the hint after a wrong combo and «Entendido» while the fight waits for it.
 */
@Composable
fun TutorialPanel(tutorial: TutorialUi, onUnderstood: () -> Unit, modifier: Modifier = Modifier) {
    val texts = TutorialTexts.of(tutorial.lessonId)
    var showCheck by remember { mutableStateOf(false) }
    LaunchedEffect(tutorial.passed) {
        if (tutorial.passed > 0) {
            showCheck = true
            delay(CHECK_MILLIS)
            showCheck = false
        }
    }
    Column(
        modifier.fillMaxWidth().clip(PanelShape).background(Color(0xE6101418))
            .border(1.dp, WardenColors.Primary, PanelShape).padding(horizontal = 10.dp, vertical = 6.dp)
            .heightIn(max = 260.dp).testTag("tutorial_panel"),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                trText(texts?.let { stringResource(it.name) }.orEmpty()),
                color = WardenColors.Primary,
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.weight(1f),
                maxLines = 1,
            )
            if (showCheck) {
                Text("✔", color = Color(0xFF81C784), fontSize = 18.sp, modifier = Modifier.testTag("tutorial_check"))
                Spacer(Modifier.width(8.dp))
            }
            Text(
                trTextResource(R.string.tutorial_step_of, (tutorial.step + 1).coerceAtMost(tutorial.stepCount), tutorial.stepCount),
                color = WardenColors.Text,
                fontSize = 12.sp,
                maxLines = 1,
            )
        }
        Column(
            Modifier.weight(1f, fill = false).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            if (tutorial.showIntro && texts != null) {
                Text(trText(stringResource(texts.intro)), color = WardenColors.Text, fontSize = 13.sp)
            }
            val stepText = texts?.steps?.getOrNull(tutorial.step)
            if (stepText != null && tutorial.requested != null) {
                Text(trText(stringResource(stepText)), color = WardenColors.Text, fontSize = 14.sp)
            }
        }
        val requested = tutorial.requested
        if (requested != null) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                ComboKeys(requested, size = KeySize, modifier = Modifier.testTag("tutorial_keys"))
                Box(
                    Modifier.size(KeySize).clip(RoundedCornerShape(KeySize / 4)).background(KeyColors.actionBackground),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        painterResource(R.drawable.ic_sword),
                        contentDescription = stringResource(R.string.key_action),
                        tint = KeyColors.actionContent,
                        modifier = Modifier.size(18.dp),
                    )
                }
                Spacer(Modifier.weight(1f))
                if (tutorial.reading) {
                    Button(onClick = onUnderstood, modifier = Modifier.testTag("btn_tutorial_understood")) {
                        Text(trTextResource(R.string.tutorial_understood))
                    }
                }
            }
            if (tutorial.hint && !tutorial.reading) {
                Text(
                    trTextResource(R.string.tutorial_hint, spacedKeys(requested)),
                    color = Color(0xFFFF8A80),
                    fontSize = 13.sp,
                    modifier = Modifier.testTag("tutorial_hint"),
                )
            }
        }
    }
}
