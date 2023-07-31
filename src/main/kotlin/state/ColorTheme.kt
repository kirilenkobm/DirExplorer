package state

import java.util.ResourceBundle

/**
 * Enum representing the color themes available in the application.
 */
enum class ColorTheme(private val key: String) {
    LIGHT("LightTheme") {
        override fun toString(): String {
            return displayName
        }
    },
    DARK("DarkTheme") {
        override fun toString(): String {
            return displayName
        }
    };

    val displayName: String
        get() = ResourceBundle.getBundle(
            Constants.LANGUAGE_BUNDLE_PATH,
            Settings.language.getLocale())
            .getString(key)
}
