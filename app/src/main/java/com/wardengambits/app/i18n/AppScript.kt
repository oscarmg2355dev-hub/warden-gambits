package com.wardengambits.app.i18n

/** Script the visible text is written in. [code] is what the settings store. */
enum class AppScript(val code: String) {
    LATIN("latin"),
    TENGWAR("tengwar"),
    ;

    fun other(): AppScript = when (this) {
        LATIN -> TENGWAR
        TENGWAR -> LATIN
    }

    companion object {
        fun fromCode(code: String?): AppScript? = entries.firstOrNull { it.code == code }
    }
}
