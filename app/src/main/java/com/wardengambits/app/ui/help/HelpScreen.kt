package com.wardengambits.app.ui.help

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.dp
import com.wardengambits.app.R
import com.wardengambits.app.i18n.trText
import com.wardengambits.app.i18n.trTextResource
import com.wardengambits.app.ui.common.ScreenHeader
import com.wardengambits.app.ui.common.ComboKeys
import com.wardengambits.app.ui.common.EffectBadge
import com.wardengambits.app.ui.common.comboBadge
import com.wardengambits.app.ui.theme.CinzelFamily
import com.wardengambits.app.ui.theme.WardenColors
import com.wardengambits.engine.combo.ComboDefinition
import com.wardengambits.engine.combo.ComboFamily

@Composable
fun HelpScreen(groups: Map<ComboFamily, List<ComboDefinition>>, onBack: () -> Unit) {
    Surface(modifier = Modifier.fillMaxSize(), color = Color.Transparent, contentColor = MaterialTheme.colorScheme.onBackground) {
        Column(Modifier.fillMaxSize()) {
            ScreenHeader(stringResource(R.string.menu_help), onBack)
            LazyColumn(Modifier.fillMaxWidth().padding(horizontal = 16.dp).weight(1f).testTag("help_list"), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                item { Text(trTextResource(R.string.help_intro), color = WardenColors.Text) }
                groups.forEach { (family, defs) ->
                    item(key = family.name) {
                        Text(
                            trTextResource(ComboDescriber.titleRes(family)),
                            fontFamily = CinzelFamily,
                            fontSize = 18.sp,
                            color = WardenColors.Primary,
                            modifier = Modifier.padding(top = 8.dp),
                        )
                    }
                    items(defs.size, key = { defs[it].sequence }) { ComboRow(defs[it]) }
                }
            }
        }
    }
}

@Composable
private fun ComboRow(def: ComboDefinition) {
    Column(
        Modifier.fillMaxWidth().testTag("combo_row_${def.sequence}"),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            ComboKeys(def.sequence, size = 26.dp)
            Text(
                trTextResource(ComboDescriber.nameRes(def.sequence)),
                modifier = Modifier.weight(1f),
                fontFamily = CinzelFamily,
                fontSize = 16.sp,
                color = WardenColors.Text,
            )
            Text(trTextResource(R.string.help_cost, def.cost), color = WardenColors.Primary)
        }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(trText(ComboDescriber.effectText(LocalResources.current, def)), Modifier.weight(1f), fontSize = 15.sp, color = WardenColors.Text)
            comboBadge(def)?.let { EffectBadge(it) }
        }
    }
}
