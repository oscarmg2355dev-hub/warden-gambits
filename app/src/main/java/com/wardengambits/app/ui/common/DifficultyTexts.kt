package com.wardengambits.app.ui.common

import com.wardengambits.app.R
import com.wardengambits.engine.model.Difficulty

/** String resource of the display name of [this] difficulty. */
fun Difficulty.nameRes(): Int = when (this) {
    Difficulty.SHIRE -> R.string.difficulty_shire_name
    Difficulty.RANGER -> R.string.difficulty_ranger_name
    Difficulty.MARK -> R.string.difficulty_mark_name
    Difficulty.EYE -> R.string.difficulty_eye_name
}

/** String resource of the one-line hint of [this] difficulty. */
fun Difficulty.hintRes(): Int = when (this) {
    Difficulty.SHIRE -> R.string.difficulty_shire_hint
    Difficulty.RANGER -> R.string.difficulty_ranger_hint
    Difficulty.MARK -> R.string.difficulty_mark_hint
    Difficulty.EYE -> R.string.difficulty_eye_hint
}
