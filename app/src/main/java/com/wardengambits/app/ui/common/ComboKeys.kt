package com.wardengambits.app.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wardengambits.app.R
import com.wardengambits.app.ui.theme.KeyAction
import com.wardengambits.app.ui.theme.KeyActionContent
import com.wardengambits.app.ui.theme.KeyFist
import com.wardengambits.app.ui.theme.KeyFistContent
import com.wardengambits.app.ui.theme.KeyShield
import com.wardengambits.app.ui.theme.KeyShieldContent
import com.wardengambits.app.ui.theme.KeySpear
import com.wardengambits.app.ui.theme.KeySpearContent

/** Colors of the combat keys: '1' spear, '2' shield, '3' fist; anything else is neutral. */
object KeyColors {
    val actionBackground: Color = KeyAction
    val actionContent: Color = KeyActionContent

    fun background(c: Char): Color = when (c) {
        '1' -> KeySpear
        '2' -> KeyShield
        '3' -> KeyFist
        else -> Color.DarkGray
    }

    fun content(c: Char): Color = when (c) {
        '1' -> KeySpearContent
        '2' -> KeyShieldContent
        '3' -> KeyFistContent
        else -> Color.White
    }
}

/** A key sequence ("121") drawn as one small colored square per input. */
@Composable
fun ComboKeys(sequence: String, size: Dp = 28.dp, modifier: Modifier = Modifier) {
    Row(modifier, horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
        sequence.forEach { ch ->
            Box(
                Modifier.size(size).clip(RoundedCornerShape(size / 4)).background(KeyColors.background(ch)),
                contentAlignment = Alignment.Center,
            ) {
                Text(ch.toString(), color = KeyColors.content(ch), fontSize = (size.value * 0.6f).sp)
            }
        }
    }
}

/** Alpha of a disabled combat key. */
const val DisabledAlpha = 0.35f

/** Big combat button: key '1'/'2'/'3', or null for the Action (sword) key. */
@Composable
fun KeyButton(key: Char?, onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true) {
    val bg = if (key == null) KeyColors.actionBackground else KeyColors.background(key)
    val fg = if (key == null) KeyColors.actionContent else KeyColors.content(key)
    Box(
        modifier.height(72.dp).alpha(if (enabled) 1f else DisabledAlpha).clip(RoundedCornerShape(12.dp)).background(bg)
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        if (key == null) {
            Icon(
                painterResource(R.drawable.ic_sword),
                contentDescription = stringResource(R.string.key_action),
                tint = fg,
                modifier = Modifier.size(36.dp),
            )
        } else {
            Text(key.toString(), color = fg, fontSize = 28.sp)
        }
    }
}
