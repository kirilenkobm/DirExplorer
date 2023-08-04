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
            return "Русский"
        }
    },
    GERMAN("de") {
        override fun toString(): String {
            return "Deutsch"
        }
    };

    fun getLocale(): Locale = Locale(code)
}
