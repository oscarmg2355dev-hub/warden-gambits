package com.wardengambits.app.audio

import com.wardengambits.app.i18n.AppLanguage
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class NarrationControllerTest {
    private val player = FakeNarrationPlayer(setOf("es/bree_1_0", "en/bree_1_0", "es/bree_2_0", "en/bree_2_0"))
    private val controller = NarrationController(player)

    private fun plays() = player.calls.filter { it.startsWith("play") }

    @Test fun autoPlaysWhenAudioExists() {
        controller.onNodeShown("bree_1_0", AppLanguage.ES, true)
        assertEquals(listOf("play es/bree_1_0"), plays())
        assertEquals(NarrationState.PLAYING, controller.state.value)
    }

    @Test fun noAudioNoPlay() {
        controller.onNodeShown("bree_3_3", AppLanguage.ES, true)
        assertTrue(plays().isEmpty())
    }

    @Test fun disabledNoPlay() {
        controller.onNodeShown("bree_1_0", AppLanguage.ES, false)
        assertTrue(plays().isEmpty())
    }

    @Test fun newNodeStopsPreviousNarration() {
        controller.onNodeShown("bree_1_0", AppLanguage.ES, true)
        controller.onNodeShown("bree_2_0", AppLanguage.ES, true)
        assertEquals(listOf("stop", "play es/bree_1_0", "stop", "play es/bree_2_0"), player.calls)
    }

    @Test fun fightStopsNarration() {
        controller.onNodeShown("bree_1_0", AppLanguage.ES, true)
        controller.onLeave()
        assertEquals(NarrationState.IDLE, controller.state.value)
    }

    @Test fun languageChangeStops() {
        controller.onNodeShown("bree_2_0", AppLanguage.ES, true)
        controller.onLeave() // what the host does when only the language changes
        assertEquals(NarrationState.IDLE, controller.state.value)
        assertEquals(1, plays().size)
    }

    @Test fun brokenAudioIsIgnored() {
        player.failOnPlay = true
        controller.onNodeShown("bree_1_0", AppLanguage.ES, true)
        assertEquals(NarrationState.IDLE, controller.state.value)
    }

    @Test fun englishUsesEnFolder() {
        controller.onNodeShown("bree_1_0", AppLanguage.EN, true)
        assertEquals(listOf("play en/bree_1_0"), plays())
    }

    @Test fun backFromFightDoesNotReadAgain() {
        controller.onNodeShown("bree_1_0", AppLanguage.ES, true)
        controller.onLeave() // the fight
        controller.onNodeShown("bree_1_0", AppLanguage.ES, true) // back after winning
        assertEquals(listOf("play es/bree_1_0"), plays())
        assertEquals(NarrationState.IDLE, controller.state.value)
    }

    @Test fun readButtonStillReadsAfterFight() {
        controller.onNodeShown("bree_1_0", AppLanguage.ES, true)
        controller.onLeave()
        controller.onNodeShown("bree_1_0", AppLanguage.ES, true)
        controller.toggle("bree_1_0", AppLanguage.ES)
        assertEquals(listOf("play es/bree_1_0", "play es/bree_1_0"), plays())
    }

    @Test fun nextNodeReadsAndThenTheFirstOneAgain() {
        controller.onNodeShown("bree_1_0", AppLanguage.ES, true)
        controller.onNodeShown("bree_2_0", AppLanguage.ES, true)
        controller.onNodeShown("bree_1_0", AppLanguage.ES, true)
        assertEquals(listOf("play es/bree_1_0", "play es/bree_2_0", "play es/bree_1_0"), plays())
    }

    @Test fun enablingTheOptionReadsTheNodeOnScreen() {
        controller.onNodeShown("bree_1_0", AppLanguage.ES, false)
        controller.onNodeShown("bree_1_0", AppLanguage.ES, true)
        assertEquals(listOf("play es/bree_1_0"), plays())
    }

    @Test fun otherLanguageOfTheSameNodeReads() {
        controller.onNodeShown("bree_1_0", AppLanguage.ES, true)
        controller.onLeave()
        controller.onNodeShown("bree_1_0", AppLanguage.EN, true)
        assertEquals(listOf("play es/bree_1_0", "play en/bree_1_0"), plays())
    }

    @Test fun toggleStopsThenReads() {
        controller.onNodeShown("bree_1_0", AppLanguage.ES, true)
        controller.toggle("bree_1_0", AppLanguage.ES)
        assertEquals(NarrationState.IDLE, controller.state.value)
        controller.toggle("bree_1_0", AppLanguage.ES)
        assertEquals(NarrationState.PLAYING, controller.state.value)
    }
}
