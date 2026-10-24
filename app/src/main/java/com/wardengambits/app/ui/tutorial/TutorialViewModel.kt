package com.wardengambits.app.ui.tutorial

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wardengambits.engine.tutorial.TutorialLesson
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

data class TutorialLessonUi(val id: String, val done: Boolean)

/** The lesson list of the training ground, with the completed ones from [done] (the settings). */
class TutorialViewModel(private val lessonList: List<TutorialLesson>, done: StateFlow<Set<String>?>) : ViewModel() {
    val lessons: StateFlow<List<TutorialLessonUi>> = done.map(::lessonsFor)
        .stateIn(viewModelScope, SharingStarted.Eagerly, lessonsFor(done.value))

    private fun lessonsFor(done: Set<String>?): List<TutorialLessonUi> =
        lessonList.map { TutorialLessonUi(it.id, done != null && it.id in done) }
}
