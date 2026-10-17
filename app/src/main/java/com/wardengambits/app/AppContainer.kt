package com.wardengambits.app

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.wardengambits.app.audio.AndroidSoundPlayer
import com.wardengambits.app.audio.GatedSoundPlayer
import com.wardengambits.app.audio.SoundPlayer
import com.wardengambits.app.data.SaveRepository
import com.wardengambits.app.data.SettingsRepository
import com.wardengambits.app.i18n.AppLanguage
import com.wardengambits.app.i18n.AppScript
import com.wardengambits.engine.rules.CombatEngine
import com.wardengambits.engine.story.StoryEngine
import com.wardengambits.engine.story.content.StoryContent
import com.wardengambits.engine.story.content.StoryContentLoader
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Locale
import java.util.concurrent.ConcurrentHashMap

private val Context.saveDataStore: DataStore<Preferences> by preferencesDataStore("warden_save")

/** Builds the story in another language from the parsed Spanish one (overlays only, no second parse of the base). */
fun interface StoryLoader {
    fun load(language: AppLanguage, spanish: StoryContent): StoryEngine
}

private val translatedStory = StoryLoader { language, spanish ->
    StoryEngine(StoryContentLoader.translated(spanish, language.code))
}

/**
 * Hand-built dependency container (no DI framework). Translations of the story are loaded on [storyLoadDispatcher],
 * never on the main thread.
 */
class AppContainer(
    context: Context,
    val clock: () -> Long = System::currentTimeMillis,
    audio: (Context) -> SoundPlayer = ::AndroidSoundPlayer,
    private val storyLoadDispatcher: CoroutineDispatcher = Dispatchers.Default,
    private val storyLoader: StoryLoader = translatedStory,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    /** The Spanish story: it holds the mechanics, so the session and the save always use it. */
    val content: StoryContent = StoryContentLoader.loadFromResources()
    val storyEngine: StoryEngine = StoryEngine(content)

    /** Story engines by language, filled as their background loads finish. */
    private val loadedStories = ConcurrentHashMap(mapOf(AppLanguage.ES to storyEngine))

    /** Background load of each translation; touched on the main thread only. */
    private val storyLoads = mutableMapOf<AppLanguage, Deferred<StoryEngine>>()

    /** Language last asked for with [showStoryIn]; a load that finishes late only applies if it is still this one. */
    private var shownLanguage = AppLanguage.ES

    private val _storyTexts = MutableStateFlow(storyEngine)

    /**
     * The story in the language set with [showStoryIn], for what the screens show (node texts, rival and zone
     * names). Same tree, encounters and rules as [storyEngine]; only the texts change.
     */
    val storyTexts: StateFlow<StoryEngine> = _storyTexts.asStateFlow()

    val combatEngine: CombatEngine = CombatEngine()
    val saveRepository = SaveRepository(context.applicationContext.saveDataStore, storyEngine)
    val settingsRepository = SettingsRepository(context.applicationContext.saveDataStore)
    val session = GameSession(storyEngine, saveRepository, scope, clock)

    /** Plays nothing while the "Sonido" option is off. */
    val soundPlayer: SoundPlayer = GatedSoundPlayer(audio(context.applicationContext))

    val soundEnabled: StateFlow<Boolean> = settingsRepository.soundEnabled.stateIn(scope, SharingStarted.Eagerly, true)

    /** Language chosen in Options (the device's until then). */
    val language: StateFlow<AppLanguage> = settingsRepository.language
        .stateIn(scope, SharingStarted.Eagerly, AppLanguage.fromLocale(Locale.getDefault()))

    /** Script chosen in Options (Latin until then). */
    val script: StateFlow<AppScript> = settingsRepository.script.stateIn(scope, SharingStarted.Eagerly, AppScript.LATIN)

    init {
        scope.launch { soundEnabled.collect(soundPlayer::setEnabled) }
        // Preload every translation in the background, so switching language (or starting in English) is instant.
        AppLanguage.entries.filter { it != AppLanguage.ES }.forEach(::loadStory)
    }

    /**
     * Switches [storyTexts] to [language]: at once when its translation is loaded, otherwise when its background load
     * finishes (the screens keep the previous language meanwhile). Main thread; never parses on it.
     */
    fun showStoryIn(language: AppLanguage) {
        shownLanguage = language
        loadedStories[language]?.let {
            _storyTexts.value = it
            return
        }
        val pending = loadStory(language)
        scope.launch {
            val story = try {
                pending.await()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                // A broken translation must not kill the app: keep the Spanish story and retry on the next switch.
                Log.w(TAG, "Could not load the story in ${language.code}", e)
                storyLoads.remove(language, pending)
                storyEngine
            }
            if (shownLanguage == language) _storyTexts.value = story
        }
    }

    private fun loadStory(language: AppLanguage): Deferred<StoryEngine> = storyLoads.getOrPut(language) {
        scope.async(storyLoadDispatcher) { storyLoader.load(language, content).also { loadedStories[language] = it } }
    }

    fun toggleSound() {
        scope.launch { settingsRepository.toggleSound() }
    }

    fun toggleLanguage() {
        scope.launch { settingsRepository.toggleLanguage() }
    }

    fun toggleScript() {
        scope.launch { settingsRepository.toggleScript() }
    }

    private companion object {
        const val TAG = "AppContainer"
    }
}
