package com.wardengambits.app.data

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import com.wardengambits.app.i18n.AppLanguage
import com.wardengambits.app.i18n.AppScript
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import java.util.Locale

/** App settings stored in the save's DataStore, under their own keys (never touching the save). */
class SettingsRepository(
    private val dataStore: DataStore<Preferences>,
    private val warn: (String) -> Unit = { Log.w(TAG, it) },
    private val deviceLanguage: () -> AppLanguage = { AppLanguage.fromLocale(Locale.getDefault()) },
) {
    private val prefs: Flow<Preferences> = dataStore.data
        .catch { e -> if (e is IOException) emit(emptyPreferences()) else throw e }

    /** Background music on or off; on by default and when the store cannot be read. */
    val musicEnabled: Flow<Boolean> = prefs.map { it[MUSIC_ENABLED] ?: true }

    /** Sound effects on or off; follows the legacy `sound_enabled` value until the player flips it, else on. */
    val sfxEnabled: Flow<Boolean> = prefs.map { it[SFX_ENABLED] ?: it[LEGACY_SOUND_ENABLED] ?: true }

    /** Narration on or off; on by default. */
    val narrationEnabled: Flow<Boolean> = prefs.map { it[NARRATION_ENABLED] ?: true }

    /** Language of the app; the device's language (Spanish or English) until the player picks one. */
    val language: Flow<AppLanguage> = prefs.map { AppLanguage.fromCode(it[LANGUAGE]) ?: deviceLanguage() }

    /** Script of the visible text; Latin until the player picks tengwar. */
    val script: Flow<AppScript> = prefs.map { AppScript.fromCode(it[SCRIPT]) ?: AppScript.LATIN }

    /** Ids of the tutorial lessons the player has completed (kept in the settings, never in the save). */
    val tutorialDone: Flow<Set<String>> = prefs.map { it[TUTORIAL_DONE] ?: emptySet() }

    /** True once the player skipped the tutorial card of a new game. */
    val tutorialDismissed: Flow<Boolean> = prefs.map { it[TUTORIAL_DISMISSED] ?: false }

    suspend fun markTutorialDone(id: String) = edit("tutorial") { it[TUTORIAL_DONE] = (it[TUTORIAL_DONE] ?: emptySet()) + id }

    suspend fun dismissTutorial() = edit("tutorial") { it[TUTORIAL_DISMISSED] = true }

    /** Flips the setting atomically inside the store's edit, so quick toggles never read a stale value. */
    suspend fun toggleMusic() = edit("music") { it[MUSIC_ENABLED] = !(it[MUSIC_ENABLED] ?: true) }

    suspend fun toggleSfx() = edit("sfx") {
        it[SFX_ENABLED] = !(it[SFX_ENABLED] ?: it[LEGACY_SOUND_ENABLED] ?: true)
    }

    suspend fun toggleNarration() = edit("narration") { it[NARRATION_ENABLED] = !(it[NARRATION_ENABLED] ?: true) }

    /** Switches between Spanish and English (atomically, like [toggleMusic]). */
    suspend fun toggleLanguage() = edit("language") {
        it[LANGUAGE] = (AppLanguage.fromCode(it[LANGUAGE]) ?: deviceLanguage()).other().code
    }

    /** Switches between Latin and tengwar (atomically, like [toggleMusic]). */
    suspend fun toggleScript() = edit("script") {
        it[SCRIPT] = (AppScript.fromCode(it[SCRIPT]) ?: AppScript.LATIN).other().code
    }

    private suspend fun edit(what: String, change: (MutablePreferences) -> Unit) {
        try {
            dataStore.edit(change)
        } catch (e: IOException) {
            warn("Could not save $what setting: ${e.message}")
        }
    }

    private companion object {
        const val TAG = "SettingsRepository"
        val MUSIC_ENABLED = booleanPreferencesKey("music_enabled")
        val SFX_ENABLED = booleanPreferencesKey("sfx_enabled")
        val NARRATION_ENABLED = booleanPreferencesKey("narration_enabled")
        val LEGACY_SOUND_ENABLED = booleanPreferencesKey("sound_enabled")
        val LANGUAGE = stringPreferencesKey("language")
        val SCRIPT = stringPreferencesKey("script")
        val TUTORIAL_DONE = stringSetPreferencesKey("tutorial_done")
        val TUTORIAL_DISMISSED = booleanPreferencesKey("tutorial_dismissed")
    }
}
