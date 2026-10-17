package com.wardengambits.app.i18n

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import android.content.res.Configuration
import com.wardengambits.app.i18n.tengwar.Tengwar
import com.wardengambits.app.ui.theme.AlcarinFamily

/** Language chosen in Options (also needed by logic, e.g. which story translation to show). */
val LocalAppLanguage = staticCompositionLocalOf { AppLanguage.ES }

/** Script chosen in Options. */
val LocalScript = staticCompositionLocalOf { AppScript.LATIN }

/** CSUR block used by Alcarin Tengwar. */
private val TENGWAR_RANGE = ''..''

/**
 * Root provider: a [LocalContext] (and its configuration and resources) set to [language], so
 * `stringResource` follows the chosen language without recreating the Activity, plus [LocalAppLanguage]
 * and [LocalScript]. The composition below keeps its state when either value changes.
 */
@Composable
fun ProvideLanguageAndScript(language: AppLanguage, script: AppScript, content: @Composable () -> Unit) {
    val base = LocalContext.current
    val baseConfiguration = LocalConfiguration.current
    val localized = remember(base, baseConfiguration, language) {
        val configuration = Configuration(baseConfiguration).apply { setLocale(language.locale) }
        base.createConfigurationContext(configuration)
    }
    CompositionLocalProvider(
        LocalContext provides localized,
        LocalConfiguration provides localized.resources.configuration,
        LocalResources provides localized.resources,
        LocalAppLanguage provides language,
        LocalScript provides script,
        content = content,
    )
}

/**
 * What a `Text` shows for [text]: in tengwar, the transcription with the tengwar codepoints in Alcarin and
 * everything else (digits, combo codes, symbols) in the style's Latin font.
 */
@Composable
fun trText(text: String): AnnotatedString {
    val script = LocalScript.current
    val language = LocalAppLanguage.current
    return remember(text, script, language) { scriptAnnotated(text, script, language) }
}

/**
 * [trText] for a text that contains a fast-changing [number] (e.g. the combat tick): only the parts around
 * its last occurrence are transcribed, so the transcription cache does not get a new entry per value.
 */
@Composable
fun trTextKeepingNumber(text: String, number: Int): AnnotatedString {
    val digits = number.toString()
    val at = text.lastIndexOf(digits)
    if (at < 0) return trText(text)
    val before = trText(text.substring(0, at))
    val after = trText(text.substring(at + digits.length))
    return before + AnnotatedString(digits) + after
}

/** [trText] of a string resource. */
@Composable
fun trTextResource(@StringRes id: Int, vararg formatArgs: Any): AnnotatedString = trText(stringResource(id, *formatArgs))

internal fun scriptString(text: String, script: AppScript, language: AppLanguage): String = when (script) {
    AppScript.LATIN -> text
    AppScript.TENGWAR -> Tengwar.transcribe(text, language.tengwarMode)
}

internal fun scriptAnnotated(text: String, script: AppScript, language: AppLanguage): AnnotatedString =
    annotateTengwar(scriptString(text, script, language))

/** [written] with every run of tengwar codepoints in Alcarin; the rest keeps the surrounding font. */
internal fun annotateTengwar(written: String): AnnotatedString {
    if (written.none { it in TENGWAR_RANGE }) return AnnotatedString(written)
    return buildAnnotatedString {
        append(written)
        var i = 0
        while (i < written.length) {
            if (written[i] !in TENGWAR_RANGE) {
                i++
                continue
            }
            val start = i
            while (i < written.length && written[i] in TENGWAR_RANGE) i++
            addStyle(SpanStyle(fontFamily = AlcarinFamily), start, i)
        }
    }
}
