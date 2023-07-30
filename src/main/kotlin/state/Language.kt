package state

import java.util.Locale

enum class Language(val code: String) {
    ENGLISH("en") {
        override fun toString(): String {
            return "English"
        }
    },
    RUSSIAN("ru") {
        override fun toString(): String {
            return "Russian"
        }
    },
    GERMAN("de") {
        override fun toString(): String {
            return "German"
        }
    };

    fun getLocale(): Locale = Locale(code)
}
