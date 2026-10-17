package com.wardengambits.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.wardengambits.app.R

val CinzelFamily = FontFamily(Font(R.font.cinzel, FontWeight.Normal))

/** Uncial Antiqua (OFL): headings of the character sheet. */
val UncialFamily = FontFamily(Font(R.font.uncial_antiqua, FontWeight.Normal))

/** Almendra (OFL): body text of the character sheet. */
val AlmendraFamily = FontFamily(
    Font(R.font.almendra_regular, FontWeight.Normal),
    Font(R.font.almendra_bold, FontWeight.Bold),
    Font(R.font.almendra_italic, FontWeight.Normal, FontStyle.Italic),
)

/** Alcarin Tengwar (OFL, CSUR): tengwar codepoints when the script is tengwar (see i18n/Tr.kt). */
val AlcarinFamily = FontFamily(Font(R.font.alcarin_tengwar, FontWeight.Normal))

val WardenTypography = Typography(
    displayMedium = TextStyle(fontFamily = CinzelFamily, fontWeight = FontWeight.Bold, fontSize = 36.sp),
    headlineSmall = TextStyle(fontFamily = CinzelFamily, fontWeight = FontWeight.Bold, fontSize = 24.sp),
    titleMedium = TextStyle(fontFamily = CinzelFamily, fontWeight = FontWeight.SemiBold, fontSize = 18.sp),
    labelLarge = TextStyle(fontFamily = CinzelFamily, fontWeight = FontWeight.SemiBold, fontSize = 16.sp),
    bodyLarge = TextStyle(fontFamily = FontFamily.Serif, fontSize = 16.sp, lineHeight = 24.sp),
    bodyMedium = TextStyle(fontFamily = FontFamily.Serif, fontSize = 14.sp, lineHeight = 20.sp),
    bodySmall = TextStyle(fontFamily = FontFamily.Serif, fontSize = 12.sp, lineHeight = 16.sp),
)
