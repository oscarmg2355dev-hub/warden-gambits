package com.wardengambits.app.audio

import com.wardengambits.app.i18n.AppLanguage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class FakeNarrationPlayer(var audio: Set<String> = emptySet(), var failOnPlay: Boolean = false) : NarrationPlayer {
    val calls = mutableListOf<String>()
    private val _state = MutableStateFlow(NarrationState.IDLE)
    override val state: StateFlow<NarrationState> = _state
    override fun hasAudio(key: String, language: AppLanguage) = "${language.code}/$key" in audio
    override fun play(key: String, language: AppLanguage) {
        calls += "play ${language.code}/$key"
        if (failOnPlay) throw IllegalStateException("broken")
        _state.value = NarrationState.PLAYING
    }
    override fun stop() { calls += "stop"; _state.value = NarrationState.IDLE }
    override fun release() { calls += "release" }
    fun finish() { _state.value = NarrationState.IDLE }
}
