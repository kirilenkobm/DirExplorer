package state

import java.util.ResourceBundle

enum class ColorTheme(val key: String) {
    LIGHT("LightTheme"),
    DARK("DarkTheme"),
    SYSTEM("SystemTheme");

    val displayName: String
        get() = ResourceBundle.getBundle(
            "languages/Messages",
            Settings.language.getLocale())
            .getString(key)
}
