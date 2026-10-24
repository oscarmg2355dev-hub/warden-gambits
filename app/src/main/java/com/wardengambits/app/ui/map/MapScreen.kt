package com.wardengambits.app.ui.map

import android.graphics.Bitmap
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.currentStateAsState
import com.wardengambits.app.R
import com.wardengambits.app.i18n.trTextResource
import com.wardengambits.app.ui.common.ScreenHeader
import com.wardengambits.app.ui.map.world.Tile
import com.wardengambits.app.ui.map.world.WorldGen
import com.wardengambits.app.ui.map.world.WorldMap
import com.wardengambits.app.ui.theme.WardenColors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.roundToInt

/** One map tile (one pixel of the generated world) in dp. */
const val TILE_DP = 4
const val ZONE_COLS = WorldGen.ZONE_COLS
const val ZONE_WIDTH_DP = TILE_DP * ZONE_COLS

/** Present once the world has been generated and the map is drawn. */
const val MAP_WORLD_TAG = "map_world"

/** Vertical centre, in dp from the top of the map, of a stop sitting on road tile [stop]. */
fun stopCenterYDp(stop: Tile): Float = (stop.y + 0.5f) * TILE_DP

/** Column of the training ground's marker: on Bree's terrain, to the left of its stop (UI only, not a zone). */
const val TUTORIAL_STOP_COL = 14

/** How far above Bree's stop the training ground sits, so neither its marker nor its label covers Bree's. */
private val TUTORIAL_STOP_RISE = 72.dp
private val TUTORIAL_LABEL_WIDTH = 112.dp

/**
 * Whether the map offers the tutorial card: a new game (still in the first zone, [newGame]) with some lesson of
 * [lessonIds] not done and the card not skipped (unknown until DataStore loads = no). Later in the run the stop
 * stays on the map but the card does not pull the map away from the current zone.
 */
fun showTutorialCard(done: Set<String>?, dismissed: Boolean?, lessonIds: List<String>, newGame: Boolean): Boolean =
    newGame && dismissed == false && done != null && !done.containsAll(lessonIds)

/** A new game for the tutorial card: the current zone is still the first one. */
fun isNewGame(rows: List<ZoneRowUi>): Boolean = rows.firstOrNull()?.status == ZoneStatus.CURRENT

@Composable
fun MapScreen(
    rows: List<ZoneRowUi>,
    onContinue: () -> Unit,
    onBack: () -> Unit,
    worldSource: WorldSource = WorldCache,
    showTutorialCard: Boolean = false,
    onTutorial: () -> Unit = {},
    onSkipTutorial: () -> Unit = {},
) {
    Surface(modifier = Modifier.fillMaxSize(), color = Color.Transparent, contentColor = MaterialTheme.colorScheme.onBackground) {
        Column(Modifier.fillMaxSize()) {
            ScreenHeader(stringResource(R.string.map_title), onBack)
            BoxWithConstraints(Modifier.fillMaxWidth().weight(1f)) {
                val tileRows = max(WorldGen.MIN_ROWS, ceil(maxHeight.value / TILE_DP).toInt())
                val zoneIds = remember(rows) { rows.map { it.id } }
                val initial = remember(worldSource, zoneIds, tileRows) { worldSource.cached(zoneIds, tileRows) }
                val world by produceState(initial, worldSource, zoneIds, tileRows) {
                    value = worldSource.cached(zoneIds, tileRows)
                        ?: withContext(Dispatchers.Default) { worldSource.load(zoneIds, tileRows) }
                }
                val ready = world
                if (ready == null) {
                    MapLoading()
                } else {
                    Overworld(rows, ready, maxWidth, onContinue, TutorialStopUi(showTutorialCard, onTutorial, onSkipTutorial))
                }
            }
        }
    }
}

@Composable
private fun MapLoading() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(trTextResource(R.string.map_loading), color = WardenColors.Text, style = MaterialTheme.typography.titleMedium)
    }
}

@Composable
private fun Overworld(rows: List<ZoneRowUi>, world: WorldMap, viewportWidth: Dp, onContinue: () -> Unit, tutorial: TutorialStopUi) {
    val density = LocalDensity.current
    val scroll = rememberScrollState()
    val currentIndex = rows.indexOfFirst { it.status == ZoneStatus.CURRENT }
    val viewportPx = with(density) { viewportWidth.roundToPx() }
    LaunchedEffect(currentIndex, viewportPx, tutorial.showCard) {
        if (tutorial.showCard) {
            // A new game opens on the training ground, with its card.
            val centerPx = with(density) { (TUTORIAL_STOP_COL * TILE_DP + TILE_DP / 2).dp.roundToPx() }
            scroll.scrollTo((centerPx - viewportPx / 2).coerceAtLeast(0))
        } else if (currentIndex >= 0) {
            val centerPx = with(density) { (currentIndex * ZONE_WIDTH_DP + ZONE_WIDTH_DP / 2).dp.roundToPx() }
            scroll.scrollTo((centerPx - viewportPx / 2).coerceAtLeast(0))
        }
    }
    // One bitmap per world (≈0.8 MB for the whole route), never rebuilt on recomposition.
    val image = remember(world) {
        Bitmap.createBitmap(world.pixels, world.width, world.height, Bitmap.Config.ARGB_8888).asImageBitmap()
    }
    val lifecycleState by LocalLifecycleOwner.current.lifecycle.currentStateAsState()
    val running = lifecycleState.isAtLeast(Lifecycle.State.STARTED)
    val tilePx = with(density) { TILE_DP.dp.toPx() }
    Box(Modifier.fillMaxHeight().horizontalScroll(scroll)) {
        Box(
            Modifier
                .testTag(MAP_WORLD_TAG)
                .width((world.width * TILE_DP).dp)
                .fillMaxHeight()
                .drawBehind {
                    drawImage(
                        image,
                        dstSize = IntSize((world.width * tilePx).roundToInt(), (world.height * tilePx).roundToInt()),
                        filterQuality = FilterQuality.None,
                    )
                },
        ) {
            MapAmbience(world, scrollPx = { scroll.value }, viewportWidthPx = viewportPx, tilePx = tilePx, running = running)
            rows.forEachIndexed { index, row ->
                world.stops.getOrNull(index)?.let { stop -> ZoneStop(index, row, stop, onContinue) }
            }
            world.stops.firstOrNull()?.let { bree -> TrainingStop(bree, tutorial.onTutorial) }
            if (tutorial.showCard) {
                TutorialCard(tutorial.onTutorial, tutorial.onSkip, Modifier.offset(x = 8.dp, y = 8.dp))
            }
        }
    }
}

/**
 * Marker of one zone sitting on the path, with its parchment label (or the cartouche if current).
 * The label goes below the marker, above it when below would clip, or the whole stop is pinned
 * inside the map when the map is too short for either.
 */
@Composable
private fun ZoneStop(index: Int, row: ZoneRowUi, stop: Tile, onContinue: () -> Unit) {
    val centerY = stopCenterYDp(stop).dp
    val current = row.status == ZoneStatus.CURRENT
    val continueLabel = stringResource(R.string.map_continue_label)
    Layout(
        content = {
            val markerModifier = if (current) {
                Modifier.testTag(CURRENT_MARKER_TAG).clickable(onClickLabel = continueLabel, role = Role.Button, onClick = onContinue)
            } else {
                Modifier
            }
            ZoneMarker(row.name, row.status, markerModifier)
            if (current && row.level != null && row.total != null) {
                CurrentZoneCartouche(row.name, row.level, row.total, onContinue)
            } else {
                ParchmentLabel(row.name, locked = row.status == ZoneStatus.LOCKED)
            }
        },
        modifier = Modifier.offset(x = (index * ZONE_WIDTH_DP).dp).width(ZONE_WIDTH_DP.dp).fillMaxHeight(),
    ) { measurables, constraints ->
        val loose = constraints.copy(minWidth = 0, minHeight = 0, maxHeight = Constraints.Infinity)
        val marker = measurables[0].measure(loose)
        val label = measurables[1].measure(loose)
        val height = constraints.maxHeight
        val placement = stopPlacement(
            centerY = centerY.roundToPx(),
            marker = marker.height,
            gap = STOP_GAP.roundToPx(),
            label = label.height,
            mapHeight = height,
        )
        layout(constraints.maxWidth, height) {
            marker.place((constraints.maxWidth - marker.width) / 2, placement.markerTop)
            label.place((constraints.maxWidth - label.width) / 2, placement.labelTop)
        }
    }
}

private val STOP_GAP = 4.dp

private class TutorialStopUi(val showCard: Boolean, val onTutorial: () -> Unit, val onSkip: () -> Unit)

/**
 * The training ground: a marker on Bree's terrain, left of and above Bree's stop, with its label above it
 * (below when the map is too short). Tapping either opens the lesson list.
 */
@Composable
private fun TrainingStop(bree: Tile, onTutorial: () -> Unit) {
    val centerY = stopCenterYDp(bree).dp - TUTORIAL_STOP_RISE
    val centerX = (TUTORIAL_STOP_COL * TILE_DP + TILE_DP / 2).dp
    val name = stringResource(R.string.tutorial_title)
    Layout(
        content = {
            TrainingMarker(
                Modifier.testTag(TUTORIAL_STOP_TAG).clickable(onClickLabel = name, role = Role.Button, onClick = onTutorial),
            )
            ParchmentLabel(
                name,
                locked = false,
                Modifier.widthIn(max = TUTORIAL_LABEL_WIDTH).clickable(onClickLabel = name, role = Role.Button, onClick = onTutorial),
            )
        },
        modifier = Modifier.offset(x = centerX - TUTORIAL_LABEL_WIDTH / 2).width(TUTORIAL_LABEL_WIDTH).fillMaxHeight(),
    ) { measurables, constraints ->
        val loose = constraints.copy(minWidth = 0, minHeight = 0, maxHeight = Constraints.Infinity)
        val marker = measurables[0].measure(loose)
        val label = measurables[1].measure(loose)
        val height = constraints.maxHeight
        val gap = STOP_GAP.roundToPx()
        val markerTop = (centerY.roundToPx() - marker.height / 2).coerceAtMost(height - marker.height).coerceAtLeast(0)
        val above = markerTop - gap - label.height
        val labelTop = if (above >= 0) above else (markerTop + marker.height + gap).coerceAtMost(height - label.height).coerceAtLeast(0)
        layout(constraints.maxWidth, height) {
            marker.place((constraints.maxWidth - marker.width) / 2, markerTop)
            label.place((constraints.maxWidth - label.width) / 2, labelTop)
        }
    }
}

/** Where a zone stop's marker and label go, in px from the top of the map. */
data class StopPlacement(val markerTop: Int, val labelTop: Int)

fun stopPlacement(centerY: Int, marker: Int, gap: Int, label: Int, mapHeight: Int): StopPlacement {
    val markerTop = centerY - marker / 2
    val below = markerTop + marker + gap
    if (markerTop >= 0 && below + label <= mapHeight) return StopPlacement(markerTop, below)
    val above = markerTop - gap - label
    if (above >= 0 && markerTop + marker <= mapHeight) return StopPlacement(markerTop, above)
    // Neither side fits: pin the stack inside the map. If the map is shorter than the whole stack,
    // the label (with Continuar) wins and is drawn over the marker so it is never clipped.
    val top = markerTop.coerceAtMost(mapHeight - (marker + gap + label)).coerceAtLeast(0)
    val labelTop = (top + marker + gap).coerceAtMost(mapHeight - label).coerceAtLeast(0)
    return StopPlacement(top, labelTop)
}
