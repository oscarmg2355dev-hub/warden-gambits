package com.wardengambits.app.data

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.wardengambits.app.InMemoryPreferencesDataStore
import com.wardengambits.app.i18n.AppLanguage
import com.wardengambits.app.i18n.AppScript
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Locale

class SettingsRepositoryTest {
    private val store = InMemoryPreferencesDataStore()
    private val repo = SettingsRepository(store, deviceLanguage = { AppLanguage.ES })

    @Test fun musicSfxAndNarrationAreEnabledByDefault() = runTest {
        assertTrue(repo.musicEnabled.first())
        assertTrue(repo.sfxEnabled.first())
        assertTrue(repo.narrationEnabled.first())
    }

    @Test fun legacySoundSettingMigratesToEffectsOnly() = runTest {
        store.edit { it[booleanPreferencesKey("sound_enabled")] = false }
        assertFalse(repo.sfxEnabled.first())
        assertTrue(repo.musicEnabled.first())
        assertTrue(repo.narrationEnabled.first())
        repo.toggleSfx()
        assertTrue(repo.sfxEnabled.first())
    }

    @Test fun eachToggleFlipsAndIsSaved() = runTest {
        repo.toggleMusic()
        assertFalse(SettingsRepository(store).musicEnabled.first())
        assertTrue(SettingsRepository(store).sfxEnabled.first())
        repo.toggleSfx()
        assertFalse(SettingsRepository(store).sfxEnabled.first())
        repo.toggleNarration()
        assertFalse(SettingsRepository(store).narrationEnabled.first())
        repo.toggleMusic()
        repo.toggleSfx()
        repo.toggleNarration()
        assertTrue(repo.musicEnabled.first())
        assertTrue(repo.sfxEnabled.first())
        assertTrue(repo.narrationEnabled.first())
    }

    @Test fun languageDefaultsToTheDeviceLanguageAndScriptToLatin() = runTest {
        assertEquals(AppLanguage.EN, SettingsRepository(store, deviceLanguage = { AppLanguage.EN }).language.first())
        assertEquals(AppLanguage.ES, SettingsRepository(store, deviceLanguage = { AppLanguage.ES }).language.first())
        assertEquals(AppScript.LATIN, repo.script.first())
    }

    @Test fun deviceLanguageIsSpanishOnlyForSpanishLocales() {
        assertEquals(AppLanguage.ES, AppLanguage.fromLocale(Locale.forLanguageTag("es-ES")))
        assertEquals(AppLanguage.ES, AppLanguage.fromLocale(Locale.forLanguageTag("es-419")))
        assertEquals(AppLanguage.EN, AppLanguage.fromLocale(Locale.forLanguageTag("en-GB")))
        assertEquals(AppLanguage.EN, AppLanguage.fromLocale(Locale.forLanguageTag("fr-FR")))
    }

    @Test fun languageAndScriptAreSavedAndLoaded() = runTest {
        repo.toggleLanguage()
        repo.toggleScript()
        val reloaded = SettingsRepository(store, deviceLanguage = { AppLanguage.ES })
        assertEquals(AppLanguage.EN, reloaded.language.first())
        assertEquals(AppScript.TENGWAR, reloaded.script.first())
        assertEquals("en", store.data.first()[stringPreferencesKey("language")])
        assertEquals("tengwar", store.data.first()[stringPreferencesKey("script")])
        repo.toggleLanguage()
        repo.toggleScript()
        assertEquals(AppLanguage.ES, reloaded.language.first())
        assertEquals(AppScript.LATIN, reloaded.script.first())
    }

    @Test fun unknownStoredValuesFallBackToTheDefaults() = runTest {
        store.edit {
            it[stringPreferencesKey("language")] = "klingon"
            it[stringPreferencesKey("script")] = "runes"
        }
        assertEquals(AppLanguage.ES, repo.language.first())
        assertEquals(AppScript.LATIN, repo.script.first())
    }

    @Test fun soundSettingsUseTheirOwnKeysAndKeepTheSave() = runTest {
        val saveKey = stringPreferencesKey("save_json")
        store.edit { it[saveKey] = "{}" }
        repo.toggleSfx()
        repo.toggleMusic()
        repo.toggleNarration()
        assertEquals("{}", store.data.first()[saveKey])
    }

    @Test fun tutorialProgressStartsEmptyAndNotDismissed() = runTest {
        assertEquals(emptySet<String>(), repo.tutorialDone.first())
        assertFalse(repo.tutorialDismissed.first())
    }

    @Test fun tutorialDoneAndDismissedAreSaved() = runTest {
        val saveKey = stringPreferencesKey("save_json")
        store.edit { it[saveKey] = "{}" }
        repo.markTutorialDone("attack")
        repo.markTutorialDone("daze")
        repo.markTutorialDone("attack")
        repo.dismissTutorial()
        val reloaded = SettingsRepository(store)
        assertEquals(setOf("attack", "daze"), reloaded.tutorialDone.first())
        assertTrue(reloaded.tutorialDismissed.first())
        assertEquals("{}", store.data.first()[saveKey])
    }
}
