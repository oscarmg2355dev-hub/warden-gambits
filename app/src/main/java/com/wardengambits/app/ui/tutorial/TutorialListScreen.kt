package com.wardengambits.app.ui.tutorial

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wardengambits.app.R
import com.wardengambits.app.i18n.trText
import com.wardengambits.app.ui.common.ScreenHeader
import com.wardengambits.app.ui.theme.WardenColors

/** The training ground: the five lessons in order, a ✔ on the completed ones; any of them can be played. */
@Composable
fun TutorialListScreen(lessons: List<TutorialLessonUi>, onLesson: (String) -> Unit, onBack: () -> Unit) {
    Column(Modifier.fillMaxSize()) {
        ScreenHeader(stringResource(R.string.tutorial_title), onBack)
        Column(
            Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            lessons.forEachIndexed { index, lesson -> LessonRow(index + 1, lesson, onLesson) }
        }
    }
}

@Composable
private fun LessonRow(number: Int, lesson: TutorialLessonUi, onLesson: (String) -> Unit) {
    val shape = RoundedCornerShape(8.dp)
    val name = TutorialTexts.of(lesson.id)?.let { stringResource(it.name) } ?: lesson.id
    Row(
        Modifier.widthIn(max = 520.dp).fillMaxWidth().heightIn(min = 56.dp).clip(shape).background(Color(0x99000000))
            .border(BorderStroke(1.dp, if (lesson.done) WardenColors.Primary else MaterialTheme.colorScheme.outline), shape)
            .clickable(role = Role.Button) { onLesson(lesson.id) }
            .padding(horizontal = 16.dp, vertical = 10.dp)
            .testTag("tutorial_lesson_${lesson.id}"),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text("$number", color = WardenColors.Primary, fontSize = 20.sp, modifier = Modifier.padding(end = 16.dp))
        Text(trText(name), color = WardenColors.Text, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
        if (lesson.done) {
            Text("✔", color = Color(0xFF81C784), fontSize = 20.sp, modifier = Modifier.testTag("tutorial_done_${lesson.id}"))
        }
    }
}
