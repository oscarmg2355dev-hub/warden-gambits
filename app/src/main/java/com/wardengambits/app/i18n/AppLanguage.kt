package com.wardengambits.app.i18n

import com.wardengambits.app.i18n.tengwar.TengwarMode
import java.util.Locale

/** Language of the whole app (UI and story). [code] is what the settings store; [displayName] is the endonym. */
enum class AppLanguage(val code: String, val displayName: String, val tengwarMode: TengwarMode) {
    ES("es", "Español", TengwarMode.SPANISH),
    EN("en", "English", TengwarMode.ENGLISH),
    ;

    val locale: Locale get() = Locale.forLanguageTag(code)

    fun other(): AppLanguage = when (this) {
        ES -> EN
        EN -> ES
    }

    companion object {
        fun fromCode(code: String?): AppLanguage? = entries.firstOrNull { it.code == code }

        /** Spanish for any Spanish locale, English otherwise. */
        fun fromLocale(locale: Locale): AppLanguage = if (locale.language.startsWith("es")) ES else EN
    }
}
