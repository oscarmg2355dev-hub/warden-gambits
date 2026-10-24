package com.wardengambits.app.ui.map

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wardengambits.app.R
import com.wardengambits.app.i18n.trText
import com.wardengambits.app.i18n.trTextResource
import com.wardengambits.app.ui.theme.CinzelFamily
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

const val CURRENT_MARKER_TAG = "map_current_marker"
const val TUTORIAL_STOP_TAG = "map_tutorial_stop"
const val TUTORIAL_CARD_TAG = "map_tutorial_card"

private val Parchment = Color(0xFFEBD9A8)
private val ParchmentEdge = Color(0xFF7A5A2C)
private val Ink = Color(0xFF3B2A14)
private val InkFaded = Color(0xFF6E6352)
private val Gold = Color(0xFFD8B04A)
private val GoldLight = Color(0xFFF4DC8A)
private val GoldDark = Color(0xFF4A3412)
private val Stone = Color(0xFF8A857C)
private val StoneLight = Color(0xFFB4AFA4)
private val StoneDark = Color(0xFF3A3632)
private val Crimson = Color(0xFF7A1F1F)
private val Shadow = Color(0x73000000)
private val Wood = Color(0xFF9A6A3A)
private val WoodLight = Color(0xFFC89A5E)
private val WoodDark = Color(0xFF4A2E14)
private val Straw = Color(0xFFE2C46A)

val TRAINING_MARKER_SIZE = 40.dp

fun markerSize(status: ZoneStatus): Dp = when (status) {
    ZoneStatus.DONE -> 40.dp
    ZoneStatus.CURRENT -> 60.dp
    ZoneStatus.LOCKED -> 36.dp
}

/** Drawn zone marker: gold shield with a check, big gold emblem, or grey shield with a lock. */
@Composable
fun ZoneMarker(name: String, status: ZoneStatus, modifier: Modifier = Modifier) {
    val state = stringResource(
        when (status) {
            ZoneStatus.DONE -> R.string.map_zone_done
            ZoneStatus.CURRENT -> R.string.map_zone_current
            ZoneStatus.LOCKED -> R.string.map_zone_locked
        },
        name,
    )
    Canvas(modifier.size(markerSize(status)).semantics { contentDescription = state }) {
        when (status) {
            ZoneStatus.DONE -> {
                shield(Gold, GoldLight, GoldDark)
                val check = Path().apply {
                    moveTo(size.width * 0.30f, size.height * 0.48f)
                    lineTo(size.width * 0.45f, size.height * 0.64f)
                    lineTo(size.width * 0.72f, size.height * 0.32f)
                }
                drawPath(check, GoldDark, style = Stroke(4.dp.toPx(), cap = StrokeCap.Square, join = StrokeJoin.Miter))
            }
            ZoneStatus.CURRENT -> emblem()
            ZoneStatus.LOCKED -> {
                shield(Stone, StoneLight, StoneDark)
                lock()
            }
        }
    }
}

/** Marker of the training ground: a wooden shield with a straw target, in the style of [ZoneMarker]. */
@Composable
fun TrainingMarker(modifier: Modifier = Modifier) {
    val name = stringResource(R.string.tutorial_title)
    Canvas(modifier.size(TRAINING_MARKER_SIZE).semantics { contentDescription = name }) {
        shield(Wood, WoodLight, WoodDark)
        val c = Offset(size.width * 0.5f, size.height * 0.46f)
        val r = size.minDimension * 0.24f
        drawCircle(Straw, r, c)
        drawCircle(WoodDark, r, c, style = Stroke(1.5.dp.toPx()))
        drawCircle(Crimson, r * 0.55f, c)
        drawCircle(Straw, r * 0.22f, c)
    }
}

private fun shieldPath(s: Size) = Path().apply {
    moveTo(s.width * 0.12f, s.height * 0.06f)
    lineTo(s.width * 0.88f, s.height * 0.06f)
    lineTo(s.width * 0.88f, s.height * 0.48f)
    quadraticTo(s.width * 0.86f, s.height * 0.82f, s.width * 0.5f, s.height * 0.97f)
    quadraticTo(s.width * 0.14f, s.height * 0.82f, s.width * 0.12f, s.height * 0.48f)
    close()
}

private fun DrawScope.shield(fill: Color, light: Color, dark: Color) {
    val path = shieldPath(size)
    translate(2.dp.toPx(), 2.dp.toPx()) { drawPath(path, Shadow) }
    drawPath(path, fill)
    // Left-half highlight, like a lit metal plate.
    drawRect(light.copy(alpha = 0.45f), Offset(size.width * 0.16f, size.height * 0.10f), Size(size.width * 0.16f, size.height * 0.40f))
    drawPath(path, dark, style = Stroke(2.5.dp.toPx(), join = StrokeJoin.Miter))
}

private fun DrawScope.lock() {
    val w = size.width
    val h = size.height
    val stroke = 3.dp.toPx()
    drawArc(
        StoneDark, 180f, 180f, false,
        topLeft = Offset(w * 0.37f, h * 0.22f), size = Size(w * 0.26f, h * 0.30f),
        style = Stroke(stroke),
    )
    drawRect(StoneDark, Offset(w * 0.31f, h * 0.38f), Size(w * 0.38f, h * 0.28f))
    drawRect(StoneLight, Offset(w * 0.47f, h * 0.45f), Size(w * 0.06f, h * 0.12f))
}

private fun DrawScope.emblem() {
    val r = size.minDimension / 2
    val c = center
    drawCircle(Shadow, r * 0.94f, c + Offset(2.dp.toPx(), 3.dp.toPx()))
    drawCircle(GoldDark, r * 0.96f, c)
    drawCircle(Gold, r * 0.88f, c)
    drawCircle(GoldLight, r * 0.88f, c, style = Stroke(1.5.dp.toPx()))
    drawCircle(GoldDark, r * 0.70f, c)
    drawCircle(Crimson, r * 0.64f, c)
    val star = Path()
    for (i in 0 until 10) {
        val radius = if (i % 2 == 0) r * 0.46f else r * 0.19f
        val a = -PI / 2 + i * PI / 5
        val p = Offset(c.x + radius * cos(a).toFloat(), c.y + radius * sin(a).toFloat())
        if (i == 0) star.moveTo(p.x, p.y) else star.lineTo(p.x, p.y)
    }
    star.close()
    drawPath(star, GoldLight)
    drawPath(star, GoldDark, style = Stroke(1.dp.toPx()))
}

/** Small parchment label with the zone name (all markers except the current one). */
@Composable
fun ParchmentLabel(name: String, locked: Boolean, modifier: Modifier = Modifier) {
    val shape = RoundedCornerShape(3.dp)
    Box(
        modifier
            .widthIn(max = 180.dp)
            .background(if (locked) Parchment.copy(alpha = 0.8f) else Parchment, shape)
            .border(1.5.dp, ParchmentEdge, shape)
            .padding(horizontal = 6.dp, vertical = 2.dp),
    ) {
        Text(
            trText(name),
            style = TextStyle(fontFamily = CinzelFamily, fontSize = 12.sp, lineHeight = 15.sp),
            color = if (locked) InkFaded else Ink,
            textAlign = TextAlign.Center,
            maxLines = 2,
        )
    }
}

/** Cartouche of the current zone: name, progress dots, "N/7" and Continuar. */
@Composable
fun CurrentZoneCartouche(name: String, level: Int, total: Int, onContinue: () -> Unit, modifier: Modifier = Modifier) {
    val shape = RoundedCornerShape(4.dp)
    Column(
        modifier
            .width(200.dp)
            .background(Parchment, shape)
            .border(2.dp, ParchmentEdge, shape)
            .padding(horizontal = 10.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(trText(name), style = MaterialTheme.typography.titleMedium, color = Ink, textAlign = TextAlign.Center)
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
            repeat(total) { dot ->
                Box(
                    Modifier.size(10.dp)
                        .background(if (dot < level) ParchmentEdge else Color.Transparent, CircleShape)
                        .border(1.5.dp, ParchmentEdge, CircleShape),
                )
            }
            Text(trText("$level/$total"), style = MaterialTheme.typography.labelLarge, color = Ink, modifier = Modifier.padding(start = 4.dp))
        }
        Button(
            onClick = onContinue,
            shape = RoundedCornerShape(6.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Crimson, contentColor = GoldLight),
        ) {
            Text(trTextResource(R.string.map_continue), style = MaterialTheme.typography.labelLarge)
        }
    }
}

/** Card of a new game over the map: offers the training ground's lessons or skipping them. */
@Composable
fun TutorialCard(onStart: () -> Unit, onSkip: () -> Unit, modifier: Modifier = Modifier) {
    val shape = RoundedCornerShape(4.dp)
    Column(
        modifier
            .testTag(TUTORIAL_CARD_TAG)
            .width(240.dp)
            .background(Parchment, shape)
            .border(2.dp, ParchmentEdge, shape)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(trTextResource(R.string.tutorial_title), style = MaterialTheme.typography.titleMedium, color = Ink, textAlign = TextAlign.Center)
        Text(trTextResource(R.string.tutorial_card_text), style = MaterialTheme.typography.bodyMedium, color = Ink, textAlign = TextAlign.Center)
        // Stacked so the long «Empezar entrenamiento» never breaks mid-word.
        Button(
            onClick = onStart,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(6.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Crimson, contentColor = GoldLight),
        ) {
            Text(trTextResource(R.string.tutorial_start), style = MaterialTheme.typography.labelLarge, textAlign = TextAlign.Center)
        }
        TextButton(onClick = onSkip, colors = ButtonDefaults.textButtonColors(contentColor = Ink)) {
            Text(trTextResource(R.string.tutorial_skip), style = MaterialTheme.typography.labelLarge)
        }
    }
}
