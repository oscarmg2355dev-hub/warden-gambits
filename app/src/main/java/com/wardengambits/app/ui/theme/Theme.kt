package com.wardengambits.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val WardenColorScheme = darkColorScheme(
    primary = WardenColors.Primary,
    onPrimary = WardenColors.Background,
    background = WardenColors.Background,
    onBackground = WardenColors.Text,
    surface = WardenColors.Surface,
    onSurface = WardenColors.Text,
    surfaceVariant = WardenColors.Surface,
    onSurfaceVariant = WardenColors.Text,
    error = WardenColors.Health,
)

@Composable
fun WardenTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = WardenColorScheme, typography = WardenTypography, content = content)
}
