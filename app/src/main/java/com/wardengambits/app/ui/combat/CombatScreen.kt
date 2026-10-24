package com.wardengambits.app.ui.combat

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wardengambits.app.R
import com.wardengambits.app.ui.help.ComboDescriber
import com.wardengambits.app.i18n.trText
import com.wardengambits.app.i18n.trTextKeepingNumber
import com.wardengambits.app.i18n.trTextResource
import com.wardengambits.app.ui.common.ComboKeys
import com.wardengambits.app.ui.common.DisabledAlpha
import com.wardengambits.app.ui.theme.WardenColors
import com.wardengambits.app.ui.tutorial.TutorialPanel
import com.wardengambits.engine.combo.Input
import com.wardengambits.engine.model.FighterId
import com.wardengambits.engine.model.Outcome

private const val RIVAL_SLOTS = 4
private const val RIVAL_COLUMNS = 2
private val ButtonHeight = 56.dp
private val ZoneGap = 6.dp
private val HeaderHeight = 40.dp
private val ComboRowHeight = 48.dp
private val CardShape = RoundedCornerShape(8.dp)
private val NameSize = 14.sp
private val StatSize = 11.sp

@Composable
fun CombatScreen(
    state: CombatUiState,
    onPress: (Input) -> Unit,
    onExecute: () -> Unit,
    onClear: () -> Unit,
    onSelectTarget: (FighterId) -> Unit,
    onTogglePause: () -> Unit,
    onFinish: () -> Unit,
    onAbandon: () -> Unit,
    modifier: Modifier = Modifier,
    message: String? = null,
    floatingNumbers: FloatingNumbersState = FloatingNumbersState(),
    numbersClock: () -> Long = { 0L },
    running: Boolean = !state.paused,
    isPractice: Boolean = false,
    onRetry: () -> Unit = {},
    onUnderstood: () -> Unit = {},
    onNextLesson: (String) -> Unit = {},
) {
    var confirmAbandon by remember { mutableStateOf(false) }
    var pausedByDialog by remember { mutableStateOf(false) }
    val tutorial = state.tutorial
    // A lesson never ends by death: it is over once its last step is cast.
    val finished = state.outcome != null || tutorial?.completed == true

    fun openAbandonDialog() {
        confirmAbandon = true
        if (!state.paused) {
            pausedByDialog = true
            onTogglePause()
        }
    }

    fun closeAbandonDialog() {
        confirmAbandon = false
        if (pausedByDialog) {
            pausedByDialog = false
            onTogglePause()
        }
    }

    BackHandler {
        if (finished) onFinish() else openAbandonDialog()
    }

    Surface(modifier = modifier.fillMaxSize(), color = Color.Transparent, contentColor = MaterialTheme.colorScheme.onBackground) {
        Box(Modifier.fillMaxSize()) {
            // Fixed zones (spec §4): header, rivals (weight 4), player (weight 2), combo row, buttons.
            Column(
                Modifier.fillMaxSize().padding(horizontal = 8.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(ZoneGap),
            ) {
                Header(state, onTogglePause)
                RivalGrid(state.rivals, floatingNumbers, numbersClock, running, onSelectTarget, Modifier.weight(4f).fillMaxWidth())
                PlayerSlot(state.player, floatingNumbers, numbersClock, running, message, Modifier.weight(2f).fillMaxWidth())
                if (tutorial != null && !tutorial.completed) TutorialPanel(tutorial, onUnderstood)
                ComboRow(state, onClear)
                ActionButtons(onPress, onExecute, state.inputsEnabled, ready = state.bufferMatch != null, running = running && !finished)
            }
            if (state.paused && !finished && tutorial?.reading != true) PausedOverlay(onTogglePause)
            if (tutorial?.completed == true) {
                TutorialResult(tutorial.nextLessonId, onNextLesson, onFinish)
            } else {
                state.outcome?.let { ResultOverlay(state, it, isPractice, onRetry, onFinish) }
            }
        }
    }

    if (confirmAbandon && !finished) {
        AlertDialog(
            onDismissRequest = ::closeAbandonDialog,
            title = { Text(trTextResource(R.string.combat_abandon_title)) },
            text = {
                Text(
                    trTextResource(
                        when {
                            tutorial != null -> R.string.tutorial_abandon_message
                            isPractice -> R.string.combat_practice_abandon_message
                            else -> R.string.combat_abandon_message
                        },
                    ),
                )
            },
            confirmButton = {
                TextButton(onClick = { confirmAbandon = false; pausedByDialog = false; onAbandon() }) {
                    Text(trTextResource(R.string.combat_abandon_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = ::closeAbandonDialog) { Text(trTextResource(R.string.cancel)) }
            },
        )
    }
}

@Composable
private fun Header(state: CombatUiState, onTogglePause: () -> Unit) {
    Row(Modifier.fillMaxWidth().height(HeaderHeight), verticalAlignment = Alignment.CenterVertically) {
        Text(
            trText(state.zoneName),
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.titleMedium,
            color = WardenColors.Primary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            trTextKeepingNumber("${state.levelLabel}  ·  ${stringResource(R.string.combat_tick, state.tick)}", state.tick),
            color = WardenColors.Text,
            fontSize = 13.sp,
            maxLines = 1,
        )
        val tutorial = state.tutorial
        TextButton(
            onClick = onTogglePause,
            enabled = state.outcome == null && tutorial?.reading != true && tutorial?.completed != true,
            modifier = Modifier.testTag("btn_pause"),
        ) {
            Text(trTextResource(R.string.combat_pause))
        }
    }
}

/** Always a 2×2 grid of equal cells; missing rivals leave an empty placeholder. */
@Composable
private fun RivalGrid(
    rivals: List<FighterUi>,
    floatingNumbers: FloatingNumbersState,
    numbersClock: () -> Long,
    running: Boolean,
    onSelectTarget: (FighterId) -> Unit,
    modifier: Modifier,
) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(ZoneGap)) {
        for (row in 0 until RIVAL_SLOTS / RIVAL_COLUMNS) {
            Row(Modifier.weight(1f).fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(ZoneGap)) {
                for (col in 0 until RIVAL_COLUMNS) {
                    val index = row * RIVAL_COLUMNS + col
                    Box(Modifier.weight(1f).fillMaxHeight().testTag("rival_cell_$index")) {
                        val rival = rivals.getOrNull(index)
                        if (rival == null) {
                            Box(
                                Modifier.fillMaxSize().testTag("rival_empty_$index")
                                    .border(BorderStroke(1.dp, WardenColors.Surface), CardShape),
                            )
                        } else {
                            RivalCard(
                                rival,
                                floatingNumbers.forTarget(rival.id),
                                numbersClock,
                                running,
                                onSelect = { onSelectTarget(rival.id) },
                                modifier = Modifier.fillMaxSize().testTag("rival_slot_$index"),
                            )
                        }
                    }
                }
            }
        }
    }
}

/** The Warden's card, with the transient rejection text drawn over it (it never changes the zone sizes). */
@Composable
private fun PlayerSlot(
    player: FighterUi,
    floatingNumbers: FloatingNumbersState,
    numbersClock: () -> Long,
    running: Boolean,
    message: String?,
    modifier: Modifier,
) {
    Box(modifier.testTag("player_card")) {
        PlayerCard(player, floatingNumbers.forTarget(player.id), numbersClock, running, Modifier.fillMaxSize())
        if (message != null) {
            Text(
                trText(message),
                modifier = Modifier.align(Alignment.BottomCenter).padding(4.dp)
                    .clip(RoundedCornerShape(4.dp)).background(Color(0xE0000000))
                    .padding(horizontal = 8.dp, vertical = 2.dp).testTag("combat_message"),
                color = Color(0xFFFF8A80),
                fontSize = 13.sp,
                maxLines = 1,
            )
        }
    }
}

/** Left: the combo being formed (or the queued one); right: its name, chain mark and cost; then ⌫. */
@Composable
private fun ComboRow(state: CombatUiState, onClear: () -> Unit) {
    val queued = state.player.queued
    val match = state.bufferMatch
    Row(
        Modifier.fillMaxWidth().height(ComboRowHeight).clip(CardShape).background(WardenColors.Surface)
            .padding(start = 8.dp).testTag("combo_row"),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
            when {
                state.buffer.isNotEmpty() -> ComboKeys(state.buffer, size = 24.dp)
                queued != null -> Row(
                    Modifier.testTag("combat_queued"),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text(trTextResource(R.string.combat_queued), color = WardenColors.Primary, fontSize = 12.sp, maxLines = 1)
                    ComboKeys(queued.sequence, size = 24.dp)
                }
            }
        }
        val name: String
        val cost: Int?
        when {
            state.buffer.isNotEmpty() -> {
                name = if (match == null) {
                    stringResource(R.string.combat_combo_empty)
                } else {
                    stringResource(ComboDescriber.nameRes(match.sequence)) + if (state.chainContinues) " ⛓" else ""
                }
                cost = match?.cost
            }
            queued != null -> {
                name = stringResource(ComboDescriber.nameRes(queued.sequence))
                cost = queued.cost
            }
            else -> {
                name = ""
                cost = null
            }
        }
        Column(Modifier.weight(1f)) {
            Text(trText(name), color = WardenColors.Text, fontSize = NameSize, maxLines = 1, overflow = TextOverflow.Ellipsis)
            if (cost != null) {
                Text(trTextResource(R.string.combat_combo_cost, cost), color = WardenColors.Text, fontSize = StatSize, maxLines = 1)
            }
        }
        val clearDescription = stringResource(R.string.combat_clear_description)
        Box(
            Modifier.size(ComboRowHeight).alpha(if (state.inputsEnabled) 1f else DisabledAlpha)
                .clickable(enabled = state.inputsEnabled, role = Role.Button, onClick = onClear)
                .semantics { contentDescription = clearDescription }.testTag("btn_clear"),
            contentAlignment = Alignment.Center,
        ) {
            Text(trTextResource(R.string.combat_clear), color = WardenColors.Text, fontSize = 20.sp)
        }
    }
}

/** Result scrim at 65% so the defeated rivals at 0 stay visible behind it. */
private val ResultScrim = Color(0xA6000000)

/** Full-screen translucent layer that swallows touches meant for the fight underneath. */
@Composable
private fun Overlay(scrim: Color = Color(0xCC000000), content: @Composable () -> Unit) {
    Box(
        Modifier.fillMaxSize().background(scrim).clickable(enabled = true, onClick = {}),
        contentAlignment = Alignment.Center,
    ) { content() }
}

@Composable
private fun PausedOverlay(onTogglePause: () -> Unit) {
    Overlay {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text(
                trTextResource(R.string.combat_paused),
                style = MaterialTheme.typography.headlineSmall,
                color = WardenColors.Primary,
            )
            Button(onClick = onTogglePause, modifier = Modifier.heightIn(min = ButtonHeight)) {
                Text(trTextResource(R.string.combat_resume))
            }
        }
    }
}

/** End of a tutorial lesson: «Siguiente lección» (or «¡Listo para Bree!» after the last one) and «Volver al mapa». */
@Composable
private fun TutorialResult(nextLessonId: String?, onNextLesson: (String) -> Unit, onBackToMap: () -> Unit) {
    Overlay(ResultScrim) {
        Column(
            Modifier.padding(24.dp).testTag("tutorial_result"),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                trTextResource(R.string.tutorial_completed),
                style = MaterialTheme.typography.headlineSmall,
                color = WardenColors.Primary,
            )
            if (nextLessonId != null) {
                Button(onClick = { onNextLesson(nextLessonId) }, modifier = Modifier.heightIn(min = ButtonHeight)) {
                    Text(trTextResource(R.string.tutorial_next))
                }
            } else {
                Text(trTextResource(R.string.tutorial_ready), style = MaterialTheme.typography.titleMedium, color = WardenColors.Text)
            }
            Button(onClick = onBackToMap, modifier = Modifier.heightIn(min = ButtonHeight)) {
                Text(trTextResource(R.string.tutorial_back_to_map))
            }
        }
    }
}

/** Practice result: no level or wins to show, just retry or back to the grid. */
@Composable
private fun PracticeResult(outcome: Outcome, onRetry: () -> Unit, onBack: () -> Unit) {
    Column(
        Modifier.padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        when (outcome) {
            Outcome.VICTORY -> Text(
                trTextResource(R.string.combat_victory),
                style = MaterialTheme.typography.headlineSmall,
                color = WardenColors.Primary,
            )
            Outcome.DEFEAT -> Text(
                trTextResource(R.string.combat_defeat),
                style = MaterialTheme.typography.headlineSmall,
                color = WardenColors.Health,
            )
        }
        Button(onClick = onRetry, modifier = Modifier.heightIn(min = ButtonHeight).testTag("btn_practice_retry")) {
            Text(trTextResource(R.string.combat_practice_retry))
        }
        Button(onClick = onBack, modifier = Modifier.heightIn(min = ButtonHeight).testTag("btn_practice_back")) {
            Text(trTextResource(R.string.combat_practice_back))
        }
    }
}

@Composable
private fun ResultOverlay(
    state: CombatUiState,
    outcome: Outcome,
    isPractice: Boolean,
    onRetry: () -> Unit,
    onFinish: () -> Unit,
) {
    Overlay(ResultScrim) {
        if (isPractice) {
            PracticeResult(outcome, onRetry, onFinish)
            return@Overlay
        }
        Column(
            Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            when (outcome) {
                Outcome.VICTORY -> {
                    Text(
                        trTextResource(R.string.combat_victory),
                        style = MaterialTheme.typography.headlineSmall,
                        color = WardenColors.Primary,
                    )
                    Text(
                        trTextResource(R.string.combat_level_up, state.levelBefore, state.levelBefore + 1),
                        color = WardenColors.Text,
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Button(onClick = onFinish, modifier = Modifier.heightIn(min = ButtonHeight)) {
                        Text(trTextResource(R.string.combat_continue))
                    }
                }
                Outcome.DEFEAT -> {
                    Text(
                        trTextResource(R.string.combat_defeat),
                        style = MaterialTheme.typography.headlineSmall,
                        color = WardenColors.Health,
                    )
                    Text(
                        trTextResource(
                            R.string.combat_defeat_summary,
                            state.zoneName,
                            state.levelBefore,
                            pluralStringResource(R.plurals.victories, state.winsBefore, state.winsBefore),
                        ),
                        color = WardenColors.Text,
                    )
                    Button(onClick = onFinish, modifier = Modifier.heightIn(min = ButtonHeight)) {
                        Text(trTextResource(R.string.combat_back_to_previous_node))
                    }
                }
            }
        }
    }
}
