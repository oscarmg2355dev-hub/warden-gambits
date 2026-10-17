package com.wardengambits.app.ui.common

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.wardengambits.app.R
import com.wardengambits.app.i18n.trText
import com.wardengambits.app.ui.theme.WardenColors

/** Shared screen header: a round gold back-arrow button plus the screen title. */
@Composable
fun ScreenHeader(title: String, onBack: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        BackButton(onBack)
        Spacer(Modifier.width(8.dp))
        Text(
            trText(title),
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.headlineSmall,
            color = WardenColors.Primary,
        )
    }
}

/** 48 dp circular button with a gold outline and a hand-drawn ← arrow; described by the «Volver» string. */
@Composable
private fun BackButton(onBack: () -> Unit) {
    val description = stringResource(R.string.back)
    IconButton(
        onClick = onBack,
        modifier = Modifier.size(48.dp).semantics { contentDescription = description },
        colors = IconButtonDefaults.iconButtonColors(containerColor = WardenColors.Surface),
    ) {
        Canvas(
            Modifier.size(40.dp).border(BorderStroke(1.5.dp, WardenColors.Primary.copy(alpha = 0.7f)), CircleShape),
        ) {
            val w = size.width
            val h = size.height
            val stroke = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
            val arrow = Path().apply {
                moveTo(w * 0.70f, h * 0.5f)
                lineTo(w * 0.30f, h * 0.5f)
                moveTo(w * 0.47f, h * 0.33f)
                lineTo(w * 0.30f, h * 0.5f)
                lineTo(w * 0.47f, h * 0.67f)
            }
            drawPath(arrow, WardenColors.Primary, style = stroke)
        }
    }
}
