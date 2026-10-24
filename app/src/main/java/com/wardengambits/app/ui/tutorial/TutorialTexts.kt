package com.wardengambits.app.ui.tutorial

import androidx.annotation.StringRes
import com.wardengambits.app.R

/** String resources of one tutorial lesson: its name, intro and one explanation per step. */
data class LessonTexts(@StringRes val name: Int, @StringRes val intro: Int, val steps: List<Int>)

/** The texts of each lesson of `tutorial/lessons.json`, by lesson id (the JSON holds no texts). */
object TutorialTexts {
    private val byId: Map<String, LessonTexts> = mapOf(
        "attack" to LessonTexts(
            R.string.tutorial_lesson_attack,
            R.string.tutorial_attack_intro,
            listOf(
                R.string.tutorial_attack_step_0,
                R.string.tutorial_attack_step_1,
                R.string.tutorial_attack_step_2,
                R.string.tutorial_attack_step_3,
                R.string.tutorial_attack_step_4,
            ),
        ),
        "defense" to LessonTexts(
            R.string.tutorial_lesson_defense,
            R.string.tutorial_defense_intro,
            listOf(
                R.string.tutorial_defense_step_0,
                R.string.tutorial_defense_step_1,
                R.string.tutorial_defense_step_2,
                R.string.tutorial_defense_step_3,
                R.string.tutorial_defense_step_4,
                R.string.tutorial_defense_step_5,
            ),
        ),
        "daze" to LessonTexts(
            R.string.tutorial_lesson_daze,
            R.string.tutorial_daze_intro,
            listOf(
                R.string.tutorial_daze_step_0,
                R.string.tutorial_daze_step_1,
                R.string.tutorial_daze_step_2,
                R.string.tutorial_daze_step_3,
                R.string.tutorial_daze_step_4,
                R.string.tutorial_daze_step_5,
            ),
        ),
        "drain" to LessonTexts(
            R.string.tutorial_lesson_drain,
            R.string.tutorial_drain_intro,
            listOf(
                R.string.tutorial_drain_step_0,
                R.string.tutorial_drain_step_1,
                R.string.tutorial_drain_step_2,
            ),
        ),
        "finisher" to LessonTexts(
            R.string.tutorial_lesson_finisher,
            R.string.tutorial_finisher_intro,
            listOf(
                R.string.tutorial_finisher_step_0,
                R.string.tutorial_finisher_step_1,
                R.string.tutorial_finisher_step_2,
                R.string.tutorial_finisher_step_3,
                R.string.tutorial_finisher_step_4,
                R.string.tutorial_finisher_step_5,
            ),
        ),
    )

    /** The texts of lesson [id]; null for an unknown lesson. */
    fun of(id: String): LessonTexts? = byId[id]
}
