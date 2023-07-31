package state

import java.util.Locale

/**
 * Enum representing the languages available in the application.
 */
enum class Language(private val code: String) {
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
