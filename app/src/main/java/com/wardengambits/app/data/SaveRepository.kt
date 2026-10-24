package com.wardengambits.app.data

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.wardengambits.engine.story.SaveCodec
import com.wardengambits.engine.story.SaveData
import com.wardengambits.engine.story.SaveMigration
import com.wardengambits.engine.story.StoryEngine
import com.wardengambits.engine.story.StoryProfile
import kotlinx.coroutines.flow.first
import java.io.IOException

/** Persists the [StoryProfile] as a SaveData JSON string; never fails a load (spec section 4). */
class SaveRepository(
    private val dataStore: DataStore<Preferences>,
    private val engine: StoryEngine,
    private val warn: (String) -> Unit = { Log.w(TAG, it) },
) {
    suspend fun load(): StoryProfile {
        val json = try {
            dataStore.data.first()[KEY]
        } catch (e: IOException) {
            return fallback("Save store unreadable: ${e.message}")
        } ?: return engine.newGame()
        val data = SaveCodec.decode(json) ?: return fallback("Save JSON is corrupt")
        if (data.version != SaveData.CURRENT_VERSION) return fallback("Unknown save version ${data.version}")
        val profile = SaveMigration.migrate(data.profile)
        if (!engine.isValid(profile)) return fallback("Saved profile is not valid")
        return profile
    }

    /** Whether a save has been written (a corrupt one still counts: the player had played). */
    suspend fun hasSave(): Boolean = try {
        dataStore.data.first()[KEY] != null
    } catch (e: IOException) {
        false
    }

    suspend fun save(profile: StoryProfile) {
        try {
            try {
            dataStore.edit { it[KEY] = SaveCodec.encode(profile) }
        } catch (e: IOException) {
            warn("Could not save profile: ${e.message}")
        }
        } catch (e: IOException) {
            warn("Could not save profile: ${e.message}")
        }
    }

    private fun fallback(reason: String): StoryProfile {
        warn("$reason; starting a new game")
        return engine.newGame()
    }

    private companion object {
        const val TAG = "SaveRepository"
        val KEY = stringPreferencesKey("save_json")
    }
}
