package com.wardengambits.app.ui.common

import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wardengambits.app.R
import com.wardengambits.app.audio.NarrationController
import com.wardengambits.app.audio.NarrationState
import com.wardengambits.app.i18n.LocalAppLanguage
import com.wardengambits.app.i18n.trTextResource

/** The stop/read button of a narrated screen; null on screens without recorded audio. */
data class NarrationButton(val playing: Boolean, val onToggle: () -> Unit)

/**
 * Starts the narration of [key] when it shows (or the option changes), stops it when the screen goes away, to the
 * background or when only the language changes (it never restarts in the new language), and returns the button
 * state (null without audio).
 */
@Composable
fun rememberNarration(key: String, controller: NarrationController, enabled: Boolean): NarrationButton? {
    val language = LocalAppLanguage.current
    val shown = remember { arrayOfNulls<Any>(2) } // last (key, language) the effect saw
    LaunchedEffect(key, language, enabled) {
        val languageOnly = shown[0] == key && shown[1] != language
        shown[0] = key
        shown[1] = language
        if (languageOnly) controller.onLeave() else controller.onNodeShown(key, language, enabled)
    }
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    DisposableEffect(controller, lifecycle) {
        val observer = LifecycleEventObserver { _, event -> if (event == Lifecycle.Event.ON_STOP) controller.onLeave() }
        lifecycle.addObserver(observer)
        onDispose {
            lifecycle.removeObserver(observer)
            controller.onLeave()
        }
    }
    val state by controller.state.collectAsStateWithLifecycle()
    if (!controller.hasAudio(key, language)) return null
    return NarrationButton(state == NarrationState.PLAYING) { controller.toggle(key, language) }
}

@Composable
fun NarrationButtonView(button: NarrationButton, modifier: Modifier = Modifier) {
    OutlinedButton(onClick = button.onToggle, modifier = modifier.heightIn(min = 40.dp)) {
        Text(trTextResource(if (button.playing) R.string.node_stop_reading else R.string.node_read))
    }
}
