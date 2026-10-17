package com.wardengambits.app.ui.character

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wardengambits.app.R
import com.wardengambits.app.i18n.trText
import com.wardengambits.app.i18n.trTextResource
import com.wardengambits.app.ui.common.Portrait
import com.wardengambits.app.ui.common.ScreenHeader
import com.wardengambits.app.ui.theme.AlmendraFamily
import com.wardengambits.app.ui.theme.UncialFamily
import com.wardengambits.app.ui.theme.WardenColors

/** Groups thousands with a narrow no-break space: 166000 → "166 000". */
fun formatStat(value: Int): String = value.toString().reversed().chunked(3).joinToString(" ").reversed()

internal val UncialHeading = TextStyle(fontFamily = UncialFamily, fontSize = 19.sp, color = ParchmentColors.Ink)
internal val AlmendraBody = TextStyle(fontFamily = AlmendraFamily, fontSize = 18.sp, color = ParchmentColors.Ink)

@Composable
fun CharacterScreen(ui: CharacterUi, onBack: () -> Unit, onHistory: () -> Unit) {
    // Transparent: the app-wide RPG pattern shows around the sheet.
    Surface(modifier = Modifier.fillMaxSize(), color = Color.Transparent, contentColor = WardenColors.Text) {
        Column(Modifier.fillMaxSize()) {
            ScreenHeader(stringResource(R.string.menu_character), onBack)
            CenteredSheetPage(Modifier.fillMaxWidth().weight(1f)) { Sheet(ui, onHistory) }
        }
    }
}

/** Test tag of the parchment character sheet. */
const val CHARACTER_SHEET_TAG = "character_sheet"

@Composable
private fun Sheet(ui: CharacterUi, onHistory: () -> Unit) {
    Box(Modifier.fillMaxWidth().testTag(CHARACTER_SHEET_TAG).parchment()) {
        Column(Modifier.fillMaxWidth().padding(start = 18.dp, end = 18.dp, top = 18.dp, bottom = 16.dp)) {
            Identity(ui)
            SectionHeading(stringResource(R.string.character_attributes))
            Attributes(ui)
            SectionHeading(stringResource(R.string.character_journey))
            Journey(ui)
            ChronicleButton(onHistory)
        }
        WaxSeal(
            SealColors.Red,
            62.dp,
            Modifier.align(Alignment.TopEnd).padding(top = 6.dp, end = 16.dp).semantics(mergeDescendants = true) {},
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    trTextResource(R.string.character_seal_level),
                    style = TextStyle(fontFamily = UncialFamily, fontSize = 10.sp, letterSpacing = 1.sp, color = ParchmentColors.Gold),
                )
                Text(
                    ui.level.toString(),
                    style = TextStyle(fontFamily = UncialFamily, fontSize = 24.sp, lineHeight = 24.sp, color = ParchmentColors.Gold),
                )
            }
        }
    }
}

@Composable
private fun Identity(ui: CharacterUi) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
        GoldFrame { Portrait("warden", 100.dp, modifier = Modifier.clip(RoundedCornerShape(3.dp))) }
        Column {
            Text(
                trTextResource(R.string.character_name),
                modifier = Modifier.padding(end = 48.dp),
                style = TextStyle(fontFamily = UncialFamily, fontSize = 34.sp, lineHeight = 36.sp, color = ParchmentColors.Ink),
            )
            val sub = AlmendraBody.copy(fontSize = 15.sp, color = ParchmentColors.Ink.copy(alpha = 0.85f))
            Text(trTextResource(R.string.character_title), modifier = Modifier.padding(top = 4.dp), style = sub)
            Text(trTextResource(R.string.character_zone, ui.zoneName, ui.progress), style = sub)
        }
    }
}

/** ~110 dp gold bevelled frame (135° gradient) around the portrait. */
@Composable
private fun GoldFrame(content: @Composable () -> Unit) {
    val shape = RoundedCornerShape(6.dp)
    Box(
        Modifier.size(110.dp).shadow(4.dp, shape)
            .background(
                Brush.linearGradient(
                    0f to Color(0xFFF6DC8A),
                    0.45f to Color(0xFF8A6420),
                    0.6f to Color(0xFFE9C66A),
                    1f to Color(0xFF6B4A14),
                ),
                shape,
            )
            .padding(5.dp),
        contentAlignment = Alignment.Center,
    ) { content() }
}

@Composable
internal fun SectionHeading(text: String) {
    Column(Modifier.fillMaxWidth().padding(top = 14.dp, bottom = 6.dp)) {
        Text(trText(text), modifier = Modifier.fillMaxWidth(), style = UncialHeading, textAlign = TextAlign.Center)
        HorizontalDivider(Modifier.padding(top = 3.dp), thickness = 1.dp, color = ParchmentColors.Rule)
    }
}

@Composable
private fun Attributes(ui: CharacterUi) {
    val s = ui.stats
    StatRow("❤", stringResource(R.string.character_stat_hp), s.maxHp)
    StatRow("✦", stringResource(R.string.character_stat_power), s.maxPower)
    StatRow("↻", stringResource(R.string.character_stat_regen), s.powerRegen)
    StatRow("⚔", stringResource(R.string.character_stat_attack), s.attack)
    StatRow("✚", stringResource(R.string.character_stat_heal), s.heal)
    StatRow("⛨", stringResource(R.string.character_stat_defense), s.defense)
}

@Composable
private fun StatRow(symbol: String, label: String, value: Int) {
    Row(
        Modifier.fillMaxWidth().padding(vertical = 3.dp).semantics(mergeDescendants = true) {},
        verticalAlignment = Alignment.Bottom,
    ) {
        Text(
            symbol,
            modifier = Modifier.width(20.dp),
            style = AlmendraBody.copy(color = ParchmentColors.Rubric),
            textAlign = TextAlign.Center,
        )
        Text(trText(label), modifier = Modifier.padding(start = 6.dp), style = AlmendraBody.copy(fontStyle = FontStyle.Italic))
        DottedLeader()
        Text(trText(formatStat(value)), style = AlmendraBody.copy(fontWeight = FontWeight.Bold))
    }
}

@Composable
private fun RowScope.DottedLeader() {
    Canvas(Modifier.weight(1f).padding(start = 6.dp, end = 6.dp, bottom = 7.dp).height(2.dp)) {
        val y = size.height / 2f
        drawLine(
            ParchmentColors.Leader,
            start = Offset(0f, y),
            end = Offset(size.width, y),
            strokeWidth = size.height,
            cap = StrokeCap.Round,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(0.01f, 4.dp.toPx())),
        )
    }
}

@Composable
private fun Journey(ui: CharacterUi) {
    Row(Modifier.fillMaxWidth()) {
        JourneyItem(stringResource(R.string.character_journey_lap), ui.lap)
        JourneyItem(stringResource(R.string.character_journey_wins), ui.wins)
    }
    Row(Modifier.fillMaxWidth().padding(top = 4.dp)) {
        JourneyItem(stringResource(R.string.character_journey_deaths), ui.deaths)
        JourneyItem(stringResource(R.string.character_journey_completed), ui.completedLaps)
    }
}

@Composable
private fun RowScope.JourneyItem(label: String, value: Int) {
    Text(
        buildAnnotatedString {
            append(trText("$label: "))
            withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append(value.toString()) }
        },
        modifier = Modifier.weight(1f),
        style = AlmendraBody.copy(fontSize = 17.sp),
    )
}

@Composable
private fun ColumnScope.ChronicleButton(onClick: () -> Unit) {
    val shape = RoundedCornerShape(8.dp)
    Box(
        Modifier.align(Alignment.CenterHorizontally).padding(top = 14.dp, bottom = 3.dp).fillMaxWidth(0.88f)
            .drawBehind {
                // 3 dp dark bottom edge, as in the mockup's `box-shadow: 0 3px 0`.
                drawRoundRect(
                    ParchmentColors.ButtonShadow,
                    topLeft = Offset(0f, 3.dp.toPx()),
                    cornerRadius = CornerRadius(8.dp.toPx()),
                )
            }
            .clip(shape)
            .background(Brush.verticalGradient(listOf(ParchmentColors.ButtonTop, ParchmentColors.ButtonBottom)))
            .clickable(role = Role.Button, onClick = onClick)
            .heightIn(min = 48.dp)
            .padding(vertical = 9.dp),
        contentAlignment = Alignment.Center,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("📜", modifier = Modifier.offset(y = (-1).dp).padding(end = 6.dp), fontSize = 16.sp)
            Text(
                trTextResource(R.string.character_chronicle),
                style = TextStyle(fontFamily = UncialFamily, fontSize = 18.sp, color = ParchmentColors.Gold),
            )
        }
    }
}
