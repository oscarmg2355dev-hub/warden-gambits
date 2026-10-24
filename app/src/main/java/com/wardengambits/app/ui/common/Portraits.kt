package com.wardengambits.app.ui.common

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import com.wardengambits.app.R
import kotlin.math.roundToInt

/** A small pixel-art picture: 16 rows of 16 chars; `'.'` is transparent, any other char is a [palette] color. */
data class PixelSprite(val rows: List<String>, val palette: Map<Char, Color>)

/**
 * Fighter portrait: the drawable `portrait_<key>` when it exists (pixel art, drawn without smoothing), otherwise the
 * 16×16 sprite of [PortraitSprites.baseType]. [dimmed] (a defeated fighter) shows it in greys at 60 % alpha.
 */
@Composable
fun Portrait(key: String, size: Dp, dimmed: Boolean = false, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val description = stringResource(R.string.portrait_description)
    val resId = remember(key) { context.resources.getIdentifier("portrait_$key", "drawable", context.packageName) }
    val colorFilter = if (dimmed) ColorFilter.colorMatrix(ColorMatrix().apply { setToSaturation(0f) }) else null
    val alpha = if (dimmed) 0.6f else 1f
    val frame = modifier.size(size)
    if (resId != 0) {
        Image(
            bitmap = ImageBitmap.imageResource(resId),
            contentDescription = description,
            modifier = frame,
            contentScale = ContentScale.Fit,
            alpha = alpha,
            colorFilter = colorFilter,
            filterQuality = FilterQuality.None,
        )
    } else {
        val sprite = PortraitSprites.spriteFor(key)
        Canvas(frame.semantics { contentDescription = description }) {
            val side = this.size.minDimension
            val left0 = (this.size.width - side) / 2f
            val top0 = (this.size.height - side) / 2f
            fun edge(i: Int): Float = (i * side / 16f).roundToInt().toFloat()
            sprite.rows.forEachIndexed { y, row ->
                row.forEachIndexed { x, c ->
                    if (c != '.') {
                        drawRect(
                            color = sprite.palette.getValue(c),
                            topLeft = Offset(left0 + edge(x), top0 + edge(y)),
                            size = Size(edge(x + 1) - edge(x), edge(y + 1) - edge(y)),
                            alpha = alpha,
                            colorFilter = colorFilter,
                        )
                    }
                }
            }
        }
    }
}

/** Code-drawn portraits used while a fighter has no `portrait_<key>` image. Presentation only, not game rules. */
object PortraitSprites {
    /** Boss portrait key -> common type whose sprite it borrows. */
    private val BOSS_BASE = mapOf(
        "boss_bree" to "bandit",
        "boss_chet" to "wolf",
        "boss_cima" to "dunlending",
        "boss_moscagua" to "swarm",
        "boss_puente" to "warg",
        "boss_quebradas" to "troll",
        "boss_bruinen" to "dunlending",
        "boss_eregion" to "dunlending",
        "boss_caradhras" to "crebain",
        "boss_durin" to "swarm",
        "boss_moria" to "troll",
        "boss_sombrio" to "orc",
        "boss_nimrodel" to "orc_archer",
        "boss_anduin" to "orc_archer",
        "boss_argonath" to "uruk",
        "boss_amonhen" to "uruk",
        "boss_emynmuil" to "troll",
        "boss_cienagas" to "marsh_wraith",
        "boss_ithilien" to "haradrim",
        "boss_cirithungol" to "spider",
        // The Nazgul on its winged beast borrows the spectral figure until it has its own image.
        "boss_gorgoroth" to "marsh_wraith",
    )

    private val SPRITES: Map<String, PixelSprite> = mapOf(
        "bandit" to sprite(
            listOf(
                ".....kkkkkk.....",
                "....kHHHHHHk....",
                "...kHHhhhhHHk...",
                "..kHHhHHHHhHHk..",
                "..kHhHkkkkHhHk..",
                ".kHHkSSSSSSkHHk.",
                ".kHksssssssskHk.",
                ".kHkswessewskHk.",
                ".kHkmmmmmmmmkHk.",
                ".kHkmMmmmmMmkHk.",
                ".kHkmmmmmmmmkHk.",
                "..kHkmmmmmmkHk..",
                "..kHHkkkkkkHHk..",
                ".kHHHHHbbHHHHHk.",
                "kHHHHHHbbHHHHHHk",
                "kHHHHHbyybHHHHHk",
            ),
            'k' to 0xFF141010, 'H' to 0xFF4A3B2E, 'h' to 0xFF6B5641, 's' to 0xFFD7A57E, 'S' to 0xFF8A6448,
            'w' to 0xFFF2F2F2, 'e' to 0xFF1C1C1C, 'm' to 0xFF2A2A30, 'M' to 0xFF45454E,
            'b' to 0xFF6E4A28, 'y' to 0xFFD9B13B,
        ),
        "wolf" to sprite(
            listOf(
                "................",
                ".kk..........kk.",
                ".kgk........kgk.",
                ".kpgk......kgpk.",
                ".kppgkkkkkkgppk.",
                "kggggggggggggggk",
                "kgGGggggggggGGgk",
                "kgyykggggggkyygk",
                "kggggggwwggggggk",
                ".kggggwwwwggggk.",
                ".kgggwwwwwwgggk.",
                "..kgwwwnnwwwgk..",
                "..kwwwwnnwwwwk..",
                "...kwwwwwwwwk...",
                "....kwwrrwwk....",
                ".....kkkkkk.....",
            ),
            'k' to 0xFF1A1A1C, 'g' to 0xFF8C9198, 'G' to 0xFF5C6168, 'w' to 0xFFD8D8D2,
            'p' to 0xFFB98080, 'y' to 0xFFF0C040, 'n' to 0xFF0E0E0E, 'r' to 0xFFC04848,
        ),
        "wolf_saruman" to sprite(
            listOf(
                "................",
                ".kk..........kk.",
                ".kgk........kgk.",
                ".kpgk......kgpk.",
                ".kppgkkkkkkgppk.",
                "kggggwgwwgwggggk",
                "kgGGgwwwwwwgGGgk",
                "kgrrkggwwggkrrgk",
                "kggggggGGggggggk",
                ".kggggGGGGggggk.",
                ".kgggGGGGGGgggk.",
                "..kgGGGnnGGGgk..",
                "..kkwkkkkkkwkk..",
                "...kwrrrrrrwk...",
                "....kkrrrrkk....",
                ".....kkkkkk.....",
            ),
            'k' to 0xFF0C0A0A, 'g' to 0xFF3E3430, 'G' to 0xFF5E504A, 'w' to 0xFFF4F4F4,
            'p' to 0xFF6A3A3A, 'r' to 0xFFE02828, 'n' to 0xFF000000,
        ),
        "warg" to sprite(
            listOf(
                "k..............k",
                "kk............kk",
                "kgk..........kgk",
                "kggkkkkkkkkkkggk",
                "kggggggggggggggk",
                "kgGGGggggggGGGgk",
                "kgrrkggggggkrrgk",
                "kggggggggggggggk",
                "kgggggGGGGgggggk",
                "kggggGGnnGGggggk",
                "kgggkkkkkkkkgggk",
                "kggkwkrrrrkwkggk",
                "kggkrrrrrrrrkggk",
                ".kgkwkrrrrkwkgk.",
                "..kgkkkkkkkkgk..",
                "...kkkkkkkkkk...",
            ),
            'k' to 0xFF0E0A08, 'g' to 0xFF3F3229, 'G' to 0xFF5C4A3C, 'w' to 0xFFF0EAD8,
            'r' to 0xFFB02020, 'n' to 0xFF000000,
        ),
        "warg_rider" to sprite(
            listOf(
                "...........w....",
                ".....kkkk..w....",
                "....kHHHHk.b....",
                "....kgyggk.b....",
                "....kgggwk.b....",
                ".....kggk..b....",
                "....kLLLLkgb....",
                "...kLLLLLLkb....",
                "k.k.kLLLLk.b....",
                "kDkkkLLLLkkbkk..",
                "kDDDDkddkDDDDDkk",
                "kDrDDDDDDDDDDDDk",
                "kDDDDDDDDDDDDDk.",
                ".kwkDDkkkkDDDk..",
                "..k.kDk...kDk...",
                "....kkk...kkk...",
            ),
            'k' to 0xFF0E0A08, 'H' to 0xFF6A6C76, 'g' to 0xFF7E8C62, 'y' to 0xFFE8C030,
            'w' to 0xFFE8E4D8, 'L' to 0xFF8A5E36, 'b' to 0xFF9A7040, 'D' to 0xFF6E5644,
            'd' to 0xFF4A2E1C, 'r' to 0xFFE02020,
        ),
        "dunlending" to sprite(
            listOf(
                "..k.k.k..k.k.k..",
                ".kHkHkHkkHkHkHk.",
                "kHHHHHHHHHHHHHHk",
                "kHHHhhhhhhhhHHHk",
                "kHHhsssssssshHHk",
                "kHhssssssssssHhk",
                "kHhseksssskeshHk",
                "kHhssssnnsssshHk",
                "kHHBBssssssBBHHk",
                "kHHBBBBBBBBBBHHk",
                ".kHBBBkkkkBBBHk.",
                ".kHBBBBBBBBBBHk.",
                "..kBBBBBBBBBBk..",
                ".kfkBBBBBBBBkfk.",
                "kfFfkBBBBBBkfFfk",
                "kfFfFkkkkkkFfFfk",
            ),
            'k' to 0xFF140E0A, 'H' to 0xFF3A2618, 'h' to 0xFF54382A, 's' to 0xFFC8906A,
            'e' to 0xFFF2F2F2, 'n' to 0xFF8A5A40, 'B' to 0xFF5A3A22, 'f' to 0xFF8A7458,
            'F' to 0xFFB49C7A,
        ),
        "crebain" to sprite(
            listOf(
                "................",
                "......kkkk......",
                ".....kbbbbk.....",
                "....kbbbbbbk....",
                "...kbbyybbbbk...",
                "..kbbbykbbbbk...",
                "kkkkbbbbbbbbbk..",
                "kssskbbbbbbbbk..",
                ".kkkbbbbbbbbbbk.",
                "....kbBbbbbbbbbk",
                "....kbBBbbbbbbbk",
                ".....kbBBBbbbbk.",
                "......kbBBBbbk..",
                ".......kkbbbbk..",
                ".........kbbbbk.",
                "..........kkkkk.",
            ),
            'k' to 0xFF050508, 'b' to 0xFF22222E, 'B' to 0xFF3A3A52, 's' to 0xFF5A5A62,
            'y' to 0xFFE04040,
        ),
        "swarm" to sprite(
            listOf(
                "....w.ww.w......",
                ".w.w.y..y...w.w.",
                "..y..k..k....y..",
                "..k..........k..",
                "....w.w..w.w....",
                ".....y....y.....",
                ".....k....k..w.w",
                "..............y.",
                "w.w...w.w.....k.",
                ".y.....y..w.w...",
                ".k.....k...y....",
                "...........k....",
                "..w.w..w.w..w.w.",
                "...y....y....y..",
                "...k....k....k..",
                "................",
            ),
            'k' to 0xFF1A1408, 'w' to 0xFFB8C8D8, 'y' to 0xFFC89A28,
        ),
        "goblin" to sprite(
            listOf(
                "................",
                "................",
                "................",
                ".....kkkkkk.....",
                "k...kggggggk...k",
                "kgkkggggggggkkgk",
                ".kggggggggggggk.",
                "..kgyyggggyygk..",
                "..kgkyggggykgk..",
                "...kggggGgggk...",
                "...kgggGGgggk...",
                "...kgkwkwkwgk...",
                "....kgkkkkgk....",
                ".....kggggk.....",
                "....kLkkkkLk....",
                "...kLLLLLLLLk...",
            ),
            'k' to 0xFF141410, 'g' to 0xFF8E9A5C, 'G' to 0xFF66703E, 'y' to 0xFFF0E040,
            'w' to 0xFFF0ECD8, 'L' to 0xFF4A3828,
        ),
        "orc" to sprite(
            listOf(
                "................",
                "....kkkkkkkk....",
                "...kHHHHHHHHk...",
                "..kHHhHHHHhHHk..",
                ".kkkkkkkkkkkkkk.",
                "kgkggggggggggkgk",
                "kggkGGggggGGkggk",
                ".kgkyykggkyykgk.",
                "..kggggGGggggk..",
                "..kgggGnnGgggk..",
                "..kgwggggggwgk..",
                "..kgwkkkkkkwgk..",
                "..kggkrrrrkggk..",
                "...kggkkkkggk...",
                "....kkggggkk....",
                "......kkkk......",
            ),
            'k' to 0xFF0E0E0C, 'H' to 0xFF4C4E56, 'h' to 0xFF7C808A, 'g' to 0xFF6E7A5A,
            'G' to 0xFF4E583E, 'y' to 0xFFE8C030, 'n' to 0xFF2A2E22, 'w' to 0xFFEDE6D0,
            'r' to 0xFF7A2020,
        ),
        "orc_archer" to sprite(
            listOf(
                "..kk............",
                "..kbk...kkkkk...",
                ".kbsk..kLLLLLk..",
                ".kbks.kLLlllLLk.",
                "kbk.s.kLkkkkkLk.",
                "kbk.s.kgggggggk.",
                "kbk.s.kGGgggGGk.",
                "kbk.s.kgykgkygk.",
                "kbk.s.kgggGgggk.",
                "kbk.s.kgwgggwgk.",
                "kbk.s.kgwkkkwgk.",
                ".kbks..kgggggk..",
                ".kbaaaaaaaaaa>..",
                "..kbk..kLLLLLk..",
                "..kk..kLLLLLLLk.",
                "......kLLLLLLLk.",
            ),
            'k' to 0xFF0E0E0C, 'b' to 0xFF7A5430, 's' to 0xFFD8D0B8, 'L' to 0xFF4A3828,
            'l' to 0xFF6A5038, 'g' to 0xFF6E7A5A, 'G' to 0xFF4E583E, 'y' to 0xFFE8C030,
            'w' to 0xFFEDE6D0, 'r' to 0xFF7A2020, 'a' to 0xFF9A7A50, '>' to 0xFFB8B8C0,
        ),
        "troll" to sprite(
            listOf(
                "...kkkkkkkkkk...",
                "..kGGGGGGGGGGk..",
                ".kGggggggggggGk.",
                "kGggggggggggggGk",
                "kGgkkkkggkkkkgGk",
                "kGgGrkGggGkrGgGk",
                "kGggggGGGGggggGk",
                "kGgggGGNNGGgggGk",
                "kGggGGNNNNGGggGk",
                "kGggggggggggggGk",
                "kGgkwkkkkkkwkgGk",
                "kGgkwwrrrrwwkgGk",
                "kGggkkkkkkkkggGk",
                ".kGggggggggggGk.",
                "..kGGGggggGGGk..",
                "...kkkkkkkkkk...",
            ),
            'k' to 0xFF101012, 'G' to 0xFF5A5E64, 'g' to 0xFF8A8E90, 'N' to 0xFF6E7276,
            'r' to 0xFF5A2020, 'w' to 0xFFE0D8B8,
        ),
        "uruk" to sprite(
            listOf(
                "...kkkkkkkkkk...",
                "..kHHHwHwHwHHk..",
                "..kHHHwHwHwHHk..",
                "..kHHHwwwwwHwk..",
                "..kHHHwwwwwwHk..",
                "..kHHHHwwwwHHk..",
                "kkkkkkkkkkkkkkkk",
                "kdkddddddddddkdk",
                "kddkDDddddDDkddk",
                ".kdkrrkddkrrkdk.",
                "..kdddDDDDdddk..",
                "..kdwddnnddwdk..",
                "..kdwkkkkkkwdk..",
                "...kdkkkkkkdk...",
                "..kkkkddddkkkk..",
                ".kAAAAkkkkAAAAk.",
            ),
            'k' to 0xFF0C0A0A, 'H' to 0xFF3A3A42, 'w' to 0xFFF4F4F0, 'd' to 0xFF54423A, 'D' to 0xFF35291F,
            'r' to 0xFFE02020, 'n' to 0xFF1A1410, 'A' to 0xFF2A2A30,
        ),
        "mordor_orc" to sprite(
            listOf(
                "................",
                "....kkkkkkkk....",
                "...kHHHHHHHHk...",
                "..kHHrrrrrrHHk..",
                ".kHHrRRkkRRrHHk.",
                ".kHHHHrrrrHHHHk.",
                "kkkkkkkHHkkkkkkk",
                "kgggggkHHkgggggk",
                ".kgyygkHHkgyygk.",
                ".kgggggkkgggggk.",
                "..kgwggggggwgk..",
                "..kgwkkkkkkwgk..",
                "...kgkrrrrkgk...",
                "....kgkkkkgk....",
                ".kkkkkggggkkkkk.",
                "kAAAAAkkkkAAAAAk",
            ),
            'k' to 0xFF0A0A0A, 'H' to 0xFF2C2C34, 'r' to 0xFFE03018, 'R' to 0xFFFFA030, 'g' to 0xFF7E7856,
            'y' to 0xFFE8C030, 'w' to 0xFFEDE6D0, 'A' to 0xFF44444E,
        ),
        "wisp" to sprite(
            listOf(
                "................",
                "........a.......",
                ".......oa.......",
                "......oaao......",
                ".....oaaaao.....",
                "....oaabbaao....",
                "...oaabwwbaao...",
                "..o.abwwwwba.o..",
                "..oabwwwwwwbao..",
                "..oabwwwwwwbao..",
                "..o.abwwwwba.o..",
                "...oaabwwbaao...",
                "....oaabbaao....",
                ".....oaaaao.....",
                "...o..oooo..o...",
                "......o..o......",
            ),
            'o' to 0xFF3E6E68, 'a' to 0xFF8FC8A8, 'b' to 0xFFCFEFC8, 'w' to 0xFFF8FFF0,
        ),
        "marsh_wraith" to sprite(
            listOf(
                "WlllWWWWWWWlllWW",
                "WWWWWlllWWWWWWWW",
                "WWWWffffffffWWWW",
                "WWWffffffffffWWW",
                "WWffFFffffFFffWW",
                "WWfkkkffffkkkfWW",
                "WWfkgkffffkgkfWW",
                "WlfkkkFffFkkkflW",
                "WWffffFffFffffWW",
                "WWWfffFFFFfffWWW",
                "lllfffkkkkffflll",
                "WWWWffkkkkffWWWW",
                "WWWWffkkkkffWWWW",
                "WWWWWffffffWWWWW",
                "WlllWWffffWWlllW",
                "WWWWWWWWWWWWWWWW",
            ),
            'W' to 0xFF1C343A, 'l' to 0xFF5E9090, 'f' to 0xFFA8BCA8, 'F' to 0xFF6E8680, 'k' to 0xFF060A0A,
            'g' to 0xFFC0FFC0,
        ),
        "haradrim" to sprite(
            listOf(
                "......kkkk......",
                "....kkRRRRkk....",
                "...kRRrRRrRRk...",
                "..kRRRRrRRRRRk..",
                "..kRRrRRRRrRRk..",
                "..kyyyyyyyyyyk..",
                "..kRssssssssRk..",
                "..kRseksskesRk..",
                "..kRssssssssRk..",
                "..kRVVVVVVVVRk..",
                "..kyVVVVVVVVyk..",
                "...kVVVVVVVVk...",
                "....kVVVVVVk....",
                "..kkkBBkkBBkkk..",
                ".kBBBByBBBBBBBk.",
                "kBBBByBBBBBBBBBk",
            ),
            'k' to 0xFF100A08, 'R' to 0xFFC42828, 'r' to 0xFF861818, 'y' to 0xFFE0B040, 's' to 0xFF8A5A3A,
            'e' to 0xFFF2F2F2, 'V' to 0xFF2A1A1A, 'B' to 0xFF3A2A26,
        ),
        "spider" to sprite(
            listOf(
                "L..L........L..L",
                ".L..L......L..L.",
                ".L..L.kkkk.L..L.",
                "..L..kAAAAk..L..",
                "..L.kAAaaAAk.L..",
                "LL.kAAaAAaAAk.LL",
                "..LkAAAaaAAAkL..",
                "....kAAAAAAk....",
                "LLL.kkBBBBkk.LLL",
                "...LkBrBBrBkL...",
                "..L.kBBrrBBk.L..",
                ".L..kkBBBBkk..L.",
                ".L...kwkkwk...L.",
                "L....k.kk.k....L",
                "L..............L",
                "................",
            ),
            'k' to 0xFF060606, 'A' to 0xFF2A2830, 'a' to 0xFF8A8070, 'B' to 0xFF1E1C22, 'r' to 0xFFE03030,
            'w' to 0xFFE8E0C8, 'L' to 0xFF6A5C4E,
        ),
        // Training dummy of the tutorial: sack head with stitched eyes, straw, rope and a wooden shield.
        "dummy" to sprite(
            listOf(
                "......kkkk......",
                "....kkBBBBkk....",
                "...kBBbbbbBBk...",
                "..kBbBBBBBBbBk..",
                "..kBBxBBBBxBBk..",
                "..kBBBxBBxBBBk..",
                "..kBBxBBBBxBBk..",
                "..kBBBBBBBBBBk..",
                "..kBBmmmmmmBBk..",
                "...kBBBBBBBBk...",
                "..yykrrrrrrkyy..",
                ".kWWWkyyyyykyyk.",
                "kWwwwWkyyyyyyyyk",
                "kWwnwWkyyrrryyyk",
                "kWwwwWkyyyyyyyyk",
                ".kWWWk.kppppppk.",
            ),
            'k' to 0xFF141010, 'B' to 0xFFC8A472, 'b' to 0xFFE0C08C, 'x' to 0xFF3A2414, 'm' to 0xFF6A3A22,
            'y' to 0xFFD9B44A, 'r' to 0xFF8A5A2A, 'W' to 0xFF5A3A1C, 'w' to 0xFF8A5E30, 'n' to 0xFF9A9AA0,
            'p' to 0xFF6E4A28,
        ),
        "warden" to sprite(
            listOf(
                "......kkkk......",
                "....kkHHHHkk....",
                "...kHHhhhhHHk...",
                "..kHHhHHHHhHHk..",
                "..kHhHkkkkHhHk..",
                ".kHHkhhhhhhkHHk.",
                ".kHkhsssssshkHk.",
                ".kHkseksskeskHk.",
                ".kHksssssssskHk.",
                ".kHksssppssskHk.",
                ".kHHkssmmsskHHk.",
                "..kHHksssskHHk..",
                "..kHHHkkkkHHHk..",
                ".kHHHgGGGGgHHHk.",
                "kHHHHgGGGGgHHHHk",
                "kHHHHgGGGGgHHHHk",
            ),
            'k' to 0xFF141010, 'H' to 0xFF7A5232, 'h' to 0xFF9A6C44, 's' to 0xFFF0C8A0,
            'e' to 0xFF2A4A6A, 'p' to 0xFFD8A882, 'm' to 0xFFA8604E, 'g' to 0xFF6A6A70, 'G' to 0xFF3C5A3A,
        ),
    )

    /** The 18 common types (every sprite but the player's and the tutorial's training dummy). */
    val TYPES: Set<String> = SPRITES.keys - "warden" - "dummy"

    /** The sprite key for [key]: itself if it has a sprite, the boss's base type, or `warden` when unknown. */
    fun baseType(key: String): String = when {
        key in SPRITES -> key
        else -> BOSS_BASE[key] ?: "warden"
    }

    fun spriteFor(key: String): PixelSprite = SPRITES.getValue(baseType(key))
}

private fun sprite(rows: List<String>, vararg colors: Pair<Char, Long>) =
    PixelSprite(rows, colors.associate { (c, argb) -> c to Color(argb) })
