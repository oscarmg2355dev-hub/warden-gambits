package com.wardengambits.app.ui.about

import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wardengambits.app.R
import com.wardengambits.app.i18n.trTextResource
import com.wardengambits.app.ui.character.AlmendraBody
import com.wardengambits.app.ui.character.CenteredSheetPage
import com.wardengambits.app.ui.character.ParchmentColors
import com.wardengambits.app.ui.character.SectionHeading
import com.wardengambits.app.ui.character.parchment
import com.wardengambits.app.ui.common.ScreenHeader
import com.wardengambits.app.ui.theme.WardenColors

/** Public GitHub repository with the game's source code, linked from «Acerca de». */
const val SOURCE_CODE_URL = "https://github.com/oscarmg2355dev-hub/warden-gambits"

/** «Acerca de»: how the game was made, its author and the credits, on a parchment sheet like the character's. */
@Composable
fun AboutScreen(onBack: () -> Unit) {
    // Transparent: the app-wide RPG pattern shows around the sheet.
    Surface(modifier = Modifier.fillMaxSize(), color = Color.Transparent, contentColor = WardenColors.Text) {
        Column(Modifier.fillMaxSize()) {
            ScreenHeader(stringResource(R.string.menu_about), onBack)
            CenteredSheetPage(Modifier.fillMaxWidth().weight(1f)) {
                Column(Modifier.fillMaxWidth().parchment().padding(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 20.dp)) {
                    SectionHeading(stringResource(R.string.about_how_heading))
                    Paragraph(R.string.about_how_1)
                    Paragraph(R.string.about_how_2)
                    SectionHeading(stringResource(R.string.about_author_heading))
                    Paragraph(R.string.about_author_body)
                    SectionHeading(stringResource(R.string.about_source_heading))
                    Paragraph(R.string.about_source_body)
                    SourceCodeLink()
                    SectionHeading(stringResource(R.string.about_credits_heading))
                    Paragraph(R.string.about_credits_fonts)
                    Paragraph(R.string.about_credits_music)
                    Paragraph(R.string.about_credits_narration)
                    Paragraph(R.string.about_credits_disclaimer, italic = true)
                }
            }
        }
    }
}

@Composable
private fun Paragraph(@StringRes id: Int, italic: Boolean = false) {
    Text(
        trTextResource(id),
        modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
        style = AlmendraBody.copy(
            fontSize = 16.sp,
            lineHeight = 22.sp,
            fontStyle = if (italic) FontStyle.Italic else FontStyle.Normal,
        ),
    )
}

/** The repository URL, always in Latin letters (also in tengwar) so it can be read and typed; tapping opens the browser. */
@Composable
private fun SourceCodeLink() {
    val uriHandler = LocalUriHandler.current
    Text(
        SOURCE_CODE_URL,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(role = Role.Button) { uriHandler.openUri(SOURCE_CODE_URL) }
            .padding(vertical = 8.dp),
        style = AlmendraBody.copy(fontSize = 15.sp, color = ParchmentColors.Rubric, textDecoration = TextDecoration.Underline),
    )
}
