package com.wardengambits.app.ui.common

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.wardengambits.app.i18n.trText
import com.wardengambits.app.ui.theme.CinzelFamily
import com.wardengambits.app.ui.theme.WardenColors
import com.wardengambits.engine.story.Route

/** One muted color per zone, in route order (`ZoneColorsTest` keeps it in step with [Route.zones]). */
internal val ZoneColors = listOf(
    Color(0xFF5E6B3F), Color(0xFF2F5A3A), Color(0xFF3F5B5E), Color(0xFF6B7480), Color(0xFF6E5B45),
    Color(0xFF5A4F4A), Color(0xFF3F6585), Color(0xFF7A6A3A), Color(0xFF6B4A4A), Color(0xFF8A9096),
    Color(0xFF4A4A56), Color(0xFF3A3340), Color(0xFF34503F), Color(0xFF3F7066), Color(0xFF8F8A4A),
    Color(0xFF3F5F75), Color(0xFF6A6458), Color(0xFF4F6A45), Color(0xFF6B6E70), Color(0xFF3E4A3A),
    Color(0xFF4F7048), Color(0xFF3A4A40), Color(0xFF5A5250), Color(0xFF7A3E2E),
)

/**
 * Node picture (4:3): the drawable named [key] when it exists, otherwise a zone-colored gradient with the zone name.
 * [contentDescription] is the accessibility text of the picture.
 */
@Composable
fun NodeImage(
    key: String,
    zoneId: String,
    zoneName: String,
    modifier: Modifier = Modifier,
    contentDescription: String = zoneName,
) {
    val context = LocalContext.current
    val resId = remember(key) { context.resources.getIdentifier(key, "drawable", context.packageName) }
    val frame = modifier.fillMaxWidth().aspectRatio(4f / 3f)
    if (resId != 0) {
        Image(
            painter = painterResource(resId),
            contentDescription = contentDescription,
            modifier = frame,
            contentScale = ContentScale.Crop,
        )
    } else {
        val zoneColor = ZoneColors[Route.indexOf(zoneId)]
        Box(
            modifier = frame
                .background(Brush.verticalGradient(listOf(zoneColor, WardenColors.Background)))
                .semantics { this.contentDescription = contentDescription },
            contentAlignment = Alignment.Center,
        ) {
            Text(
                trText(zoneName),
                color = WardenColors.Text,
                fontFamily = CinzelFamily,
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(16.dp),
            )
        }
    }
}
