package com.wardengambits.app.data

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
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

    /** Sound on or off; on by default and when the store cannot be read. */
    val soundEnabled: Flow<Boolean> = prefs.map { it[SOUND_ENABLED] ?: true }

    /** Language of the app; the device's language (Spanish or English) until the player picks one. */
    val language: Flow<AppLanguage> = prefs.map { AppLanguage.fromCode(it[LANGUAGE]) ?: deviceLanguage() }

    /** Script of the visible text; Latin until the player picks tengwar. */
    val script: Flow<AppScript> = prefs.map { AppScript.fromCode(it[SCRIPT]) ?: AppScript.LATIN }

    suspend fun setSoundEnabled(enabled: Boolean) = edit("sound") { it[SOUND_ENABLED] = enabled }

    /** Flips the setting atomically inside the store's edit, so quick toggles never read a stale value. */
    suspend fun toggleSound() = edit("sound") { it[SOUND_ENABLED] = !(it[SOUND_ENABLED] ?: true) }

    /** Switches between Spanish and English (atomically, like [toggleSound]). */
    suspend fun toggleLanguage() = edit("language") {
        it[LANGUAGE] = (AppLanguage.fromCode(it[LANGUAGE]) ?: deviceLanguage()).other().code
    }

    /** Switches between Latin and tengwar (atomically, like [toggleSound]). */
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
        val SOUND_ENABLED = booleanPreferencesKey("sound_enabled")
        val LANGUAGE = stringPreferencesKey("language")
        val SCRIPT = stringPreferencesKey("script")
    }
}
