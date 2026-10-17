package com.wardengambits.app.ui.common

import androidx.compose.animation.core.withInfiniteAnimationFrameMillis
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.withFrameMillis

/**
 * Calls [advance] with the frame time elapsed while [active]; stops (freezing the clock) when it is not. An
 * [infinite] clock (one that never runs out by itself) asks for its frames as an infinite animation, which UI tests
 * that auto-advance their clock skip instead of waiting forever to be idle.
 */
@Composable
internal fun PausableClock(key: Any, active: Boolean, infinite: Boolean = false, advance: (Long) -> Unit) {
    LaunchedEffect(key, active) {
        if (!active) return@LaunchedEffect
        suspend fun <R> frame(onFrame: (Long) -> R): R =
            if (infinite) withInfiniteAnimationFrameMillis(onFrame) else withFrameMillis(onFrame)
        var last = frame { it }
        while (true) {
            frame { now ->
                advance(now - last)
                last = now
            }
        }
    }
}
