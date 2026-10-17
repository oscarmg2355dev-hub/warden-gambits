package com.wardengambits.app.ui.combat

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wardengambits.app.R
import com.wardengambits.app.audio.CombatSound
import com.wardengambits.app.audio.SoundPlayer
import com.wardengambits.engine.combo.Input
import com.wardengambits.engine.event.CombatEvent
import com.wardengambits.engine.event.RejectReason
import com.wardengambits.engine.model.FighterId
import com.wardengambits.engine.model.PLAYER_ID
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val MESSAGE_MILLIS = 1500L

/** Spanish toast text for a rejected combo; null when the player needs no feedback. */
private fun rejectionText(context: Context, reason: RejectReason): String? = when (reason) {
    RejectReason.BUFFER_FULL -> context.getString(R.string.combat_rejected_buffer)
    RejectReason.UNKNOWN_SEQUENCE -> context.getString(R.string.combat_rejected_unknown)
    RejectReason.DAZED -> context.getString(R.string.combat_rejected_dazed)
    RejectReason.NOT_ENOUGH_POWER -> context.getString(R.string.combat_rejected_power)
    RejectReason.INVALID_TARGET -> context.getString(R.string.combat_rejected_target)
    RejectReason.FIGHTER_DEFEATED, RejectReason.COMBAT_OVER -> null
}

/** Max power (for a [CombatTextTone.POWER] line) or max HP of fighter [id]; 0 when it is not in the fight. */
internal fun maxVitalOf(state: CombatUiState, id: FighterId, tone: CombatTextTone): Int {
    val fighter = (state.rivals + state.player).firstOrNull { it.id == id } ?: return 0
    return when (tone) {
        CombatTextTone.POWER -> fighter.maxPower
        CombatTextTone.DAMAGE, CombatTextTone.HEAL -> fighter.maxHp
    }
}

/**
 * Binds [CombatScreen] to a [CombatViewModel]: lifecycle (active only while STARTED), event feedback
 * (haptics, sounds, toast text, the floating numbers fed by [CombatEvent.EffectResolved] and [CombatEvent.PotionUsed])
 * and navigation callbacks.
 */
@Composable
fun CombatRoute(
    viewModel: CombatViewModel,
    soundPlayer: SoundPlayer,
    onBackToNode: () -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val haptic = LocalHapticFeedback.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val sound = remember(soundPlayer) { CombatSound(soundPlayer, PLAYER_ID) }
    var screenActive by remember { mutableStateOf(false) }

    var message by remember { mutableStateOf<String?>(null) }
    var messageToken by remember { mutableLongStateOf(0L) }

    if (viewModel.invalid) {
        LaunchedEffect(Unit) { onBackToNode() }
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner, viewModel) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> {
                    viewModel.setActive(true)
                    screenActive = true
                }
                Lifecycle.Event.ON_STOP -> {
                    viewModel.setActive(false)
                    screenActive = false
                    // The frame clock is paused from here on, so the LaunchedEffect below would not run: pause now.
                    sound.update(active = false, paused = true, outcome = viewModel.state.value.outcome)
                }
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            viewModel.setActive(false)
            screenActive = false
        }
    }

    // Ambience: plays while the screen is started and the fight runs; pauses with the game; stops at the end.
    LaunchedEffect(sound, screenActive, state.paused, state.outcome) {
        sound.update(active = screenActive && !viewModel.invalid, paused = state.paused, outcome = state.outcome)
    }
    DisposableEffect(sound) {
        onDispose { sound.dispose() }
    }

    val running = !state.paused && screenActive
    val numbers = rememberFloatingNumbers(running)

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            sound.onEvent(event)
            combatTextFor(event)?.let { line ->
                // Sized against the target's max power for a potion, its max HP otherwise.
                numbers.add(listOf(line), { id -> maxVitalOf(viewModel.state.value, id, line.tone) })
            }
            when (event) {
                is CombatEvent.ComboRejected -> if (event.fighter == PLAYER_ID) {
                    haptic.performHapticFeedback(HapticFeedbackType.Reject)
                    rejectionText(context, event.reason)?.let { text ->
                        message = text
                        val token = ++messageToken
                        scope.launch {
                            delay(MESSAGE_MILLIS)
                            if (messageToken == token) message = null
                        }
                    }
                }
                is CombatEvent.ComboResolved -> if (event.fighter == PLAYER_ID) {
                    haptic.performHapticFeedback(
                        if (event.chained) HapticFeedbackType.LongPress else HapticFeedbackType.Confirm,
                    )
                }
                is CombatEvent.ComboQueued,
                is CombatEvent.EffectResolved,
                is CombatEvent.EffectApplied,
                is CombatEvent.EffectExpired,
                is CombatEvent.PotionUsed,
                is CombatEvent.FighterDefeated,
                is CombatEvent.CombatEnded,
                -> Unit
            }
        }
    }

    // Double taps or back during the exit transition must navigate only once.
    var handled by remember { mutableStateOf(false) }

    CombatScreen(
        state = state,
        onPress = { input: Input ->
            haptic.performHapticFeedback(HapticFeedbackType.VirtualKey)
            sound.onKey(input)
            viewModel.press(input)
        },
        onExecute = {
            sound.onAction()
            viewModel.execute()
        },
        onClear = viewModel::clearBuffer,
        onSelectTarget = viewModel::selectTarget,
        onTogglePause = viewModel::togglePause,
        onFinish = {
            if (handled) return@CombatScreen
            handled = true
            viewModel.finish()
            // Victory and defeat both land on the node screen: after a defeat the story is on the previous node.
            onBackToNode()
        },
        onAbandon = {
            if (handled) return@CombatScreen
            handled = true
            viewModel.abandon()
            onBackToNode()
        },
        message = message,
        floatingNumbers = numbers.state,
        numbersClock = numbers::now,
        running = running,
    )
}
