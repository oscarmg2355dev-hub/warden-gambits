package com.wardengambits.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.wardengambits.app.i18n.ProvideLanguageAndScript
import com.wardengambits.app.navigation.WardenNavHost
import com.wardengambits.app.ui.theme.rpgBackground
import com.wardengambits.app.ui.theme.WardenTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val container = (application as WardenApp).container
        if (!container.session.loaded.value) lifecycleScope.launch { container.session.load() }
        setContent {
            val language by container.language.collectAsStateWithLifecycle()
            val script by container.script.collectAsStateWithLifecycle()
            // Switching language or script recomposes in place: no Activity restart, screens keep their state.
            ProvideLanguageAndScript(language, script) {
                WardenTheme {
                    val loaded by container.session.loaded.collectAsStateWithLifecycle()
                    // The RPG pattern is painted once here, behind every screen (they are transparent).
                    Box(Modifier.fillMaxSize().rpgBackground().safeDrawingPadding()) {
                        if (loaded) WardenNavHost(container) else LoadingScreen()
                    }
                }
            }
        }
    }
}

@Composable
private fun LoadingScreen() {
    Box(Modifier.fillMaxSize())
}
