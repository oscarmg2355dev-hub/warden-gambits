package com.wardengambits.engine.tutorial

import com.wardengambits.engine.ai.PatternBrain
import com.wardengambits.engine.model.CombatSetup
import com.wardengambits.engine.model.FighterSpec
import com.wardengambits.engine.model.RivalSpec
import com.wardengambits.engine.story.content.RivalContent
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json

/** One tutorial lesson: the combos the player must cast in order, against training dummies. */
@Serializable
data class TutorialLesson(val id: String, val steps: List<String>, val rivals: List<RivalContent>)

object TutorialLessons {
    /** No fighter drops below this percentage of its max HP in the tutorial. */
    const val HP_FLOOR_PCT = 10

    private const val RESOURCE = "/tutorial/lessons.json"
    private val json = Json { ignoreUnknownKeys = false }

    fun load(): List<TutorialLesson> {
        val text = requireNotNull(TutorialLessons::class.java.getResourceAsStream(RESOURCE)) { "Missing resource $RESOURCE" }
            .use { it.readBytes().toString(Charsets.UTF_8) }
        return try {
            json.decodeFromString<List<TutorialLesson>>(text.removePrefix("﻿"))
        } catch (e: SerializationException) {
            throw IllegalStateException("Invalid tutorial file $RESOURCE: ${e.message}", e)
        } catch (e: IllegalArgumentException) {
            throw IllegalStateException("Invalid tutorial file $RESOURCE: ${e.message}", e)
        }
    }

    /** The Warden at level 1 against level-1 non-boss dummies, with an HP floor so nobody dies. */
    fun setup(lesson: TutorialLesson): CombatSetup = CombatSetup(
        FighterSpec("Warden", 1),
        lesson.rivals.map {
            RivalSpec(it.name, 1, PatternBrain(it.pattern, it.everyNTicks), isBoss = false, portrait = it.portrait)
        },
        hpFloorPct = HP_FLOOR_PCT,
    )
}
