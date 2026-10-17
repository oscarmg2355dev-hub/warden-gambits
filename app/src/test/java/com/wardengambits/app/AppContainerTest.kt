package com.wardengambits.app

import android.app.Application
import android.os.Looper
import androidx.test.core.app.ApplicationProvider
import com.wardengambits.app.i18n.AppLanguage
import com.wardengambits.engine.story.StoryEngine
import com.wardengambits.engine.story.content.ContentValidator
import com.wardengambits.engine.story.content.StoryContentLoader
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotSame
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import java.util.Collections

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class AppContainerTest {
    private val loads = Collections.synchronizedList(mutableListOf<Pair<AppLanguage, Thread>>())

    private fun container(dispatcher: CoroutineDispatcher) = AppContainer(
        ApplicationProvider.getApplicationContext<Application>(),
        storyLoadDispatcher = dispatcher,
        storyLoader = { language, spanish ->
            loads += language to Thread.currentThread()
            StoryEngine(StoryContentLoader.translated(spanish, language.code))
        },
    )

    private fun idleMain() = shadowOf(Looper.getMainLooper()).idle()

    @Test
    fun appContainerLoadsContent() {
        val container = AppContainer(ApplicationProvider.getApplicationContext<Application>())
        assertTrue(ContentValidator.validate(container.content).isEmpty())
    }

    @Test
    fun showStoryInNeverLoadsOnTheCallingThread() {
        val background = StandardTestDispatcher()
        val container = container(background)
        container.showStoryIn(AppLanguage.EN) // cold start in English: nothing loaded yet
        assertEquals(emptyList<Pair<AppLanguage, Thread>>(), loads.toList())
        assertSame(container.storyEngine, container.storyTexts.value)

        background.scheduler.advanceUntilIdle()
        idleMain()
        assertEquals(listOf(AppLanguage.EN), loads.map { it.first }) // the preload and the switch share one load
        assertEquals("The Chetwood", container.storyTexts.value.zone("chet").name)

        container.showStoryIn(AppLanguage.ES)
        assertSame(container.storyEngine, container.storyTexts.value)
        container.showStoryIn(AppLanguage.EN)
        assertEquals("The Chetwood", container.storyTexts.value.zone("chet").name) // cached: switches at once
        assertEquals(1, loads.size)
    }

    @Test
    fun preloadingFillsTheCache() {
        val background = StandardTestDispatcher()
        val container = container(background)
        background.scheduler.advanceUntilIdle()
        assertEquals(listOf(AppLanguage.EN), loads.map { it.first })
        container.showStoryIn(AppLanguage.EN)
        assertNotSame(container.storyEngine, container.storyTexts.value)
        assertEquals("Mount Doom", container.storyTexts.value.zone("orodruin").name)
    }

    @Test
    fun aLateLoadDoesNotOverrideALaterChoice() {
        val background = StandardTestDispatcher()
        val container = container(background)
        container.showStoryIn(AppLanguage.EN)
        container.showStoryIn(AppLanguage.ES)
        background.scheduler.advanceUntilIdle()
        idleMain()
        assertSame(container.storyEngine, container.storyTexts.value)
    }

    @Test
    fun aFailingTranslationKeepsTheSpanishStoryAndRetriesLater() {
        val crashes = Collections.synchronizedList(mutableListOf<Throwable>())
        val main = Looper.getMainLooper().thread
        val previousHandler = main.uncaughtExceptionHandler
        main.uncaughtExceptionHandler = Thread.UncaughtExceptionHandler { _, e -> crashes += e }
        try {
            showingAFailingTranslation()
        } finally {
            main.uncaughtExceptionHandler = previousHandler
        }
        assertEquals(emptyList<Throwable>(), crashes.toList()) // on a device an uncaught exception kills the app
    }

    private fun showingAFailingTranslation() {
        val background = StandardTestDispatcher()
        var failures = 2 // the preload and the first retry fail
        val attempts = mutableListOf<AppLanguage>()
        val container = AppContainer(
            ApplicationProvider.getApplicationContext<Application>(),
            storyLoadDispatcher = background,
            storyLoader = { language, spanish ->
                attempts += language
                if (failures-- > 0) error("broken translation")
                StoryEngine(StoryContentLoader.translated(spanish, language.code))
            },
        )
        background.scheduler.advanceUntilIdle() // the preload fails
        container.showStoryIn(AppLanguage.EN) // awaits the failed preload: must not crash
        background.scheduler.advanceUntilIdle()
        idleMain()
        assertSame(container.storyEngine, container.storyTexts.value)

        container.showStoryIn(AppLanguage.EN) // the failed load is not cached: this retries (and fails again)
        background.scheduler.advanceUntilIdle()
        idleMain()
        assertSame(container.storyEngine, container.storyTexts.value)
        assertEquals(2, attempts.size)

        container.showStoryIn(AppLanguage.EN) // third try works
        background.scheduler.advanceUntilIdle()
        idleMain()
        assertEquals(3, attempts.size)
        assertEquals("The Chetwood", container.storyTexts.value.zone("chet").name)
    }

    @Test
    fun coldStartInEnglishLoadsInTheBackgroundAndEndsInEnglish() {
        val container = container(Dispatchers.Default)
        container.showStoryIn(AppLanguage.EN)
        val deadline = System.currentTimeMillis() + 20_000
        while (container.storyTexts.value === container.storyEngine && System.currentTimeMillis() < deadline) {
            Thread.sleep(10)
            idleMain()
        }
        assertEquals("The Chetwood", container.storyTexts.value.zone("chet").name)
        assertTrue(loads.none { it.second === Looper.getMainLooper().thread })
    }
}
