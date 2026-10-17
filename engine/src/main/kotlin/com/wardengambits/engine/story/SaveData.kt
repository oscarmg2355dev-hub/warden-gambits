package com.wardengambits.engine.story

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class SaveData(val version: Int = CURRENT_VERSION, val profile: StoryProfile) {
    companion object {
        const val CURRENT_VERSION = 1
    }
}

object SaveCodec {
    private val json = Json { encodeDefaults = true }

    fun encode(profile: StoryProfile): String = json.encodeToString(SaveData.serializer(), SaveData(profile = profile))

    /** Null on any parse error. */
    fun decode(json: String): SaveData? =
        try {
            this.json.decodeFromString(SaveData.serializer(), json)
        } catch (_: Exception) {
            null
        }
}
