package state

import java.util.ResourceBundle

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
            "languages/Messages",
            Settings.language.getLocale())
            .getString(key)
}
