package state

import java.util.Locale

enum class Language(val code: String) {
    ENGLISH("en"),
    RUSSIAN("ru"),
    GERMAN("de");

    fun getLocale(): Locale = Locale(code)
}
