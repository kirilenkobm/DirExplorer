// Settings, especially defined by user
package state
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.Properties

object Settings {
    // TODO: implement a smarter persistent storage
    private const val SETTINGS_FILE = "settings.properties"

    var showHiddenFiles: Boolean = false
    var colorTheme: ColorTheme = ColorTheme.SYSTEM
    var viewMode: ViewMode = ViewMode.TABLE
    var buttonSize: Int = 20
    var iconSize: Int = 48
    var language: Language = Language.GERMAN

    fun toggleShowHiddenFiles() {
        showHiddenFiles = !showHiddenFiles
    }

    fun changeColorTheme(newColorTheme: ColorTheme) {
        colorTheme = newColorTheme
    }

    fun updateViewMode(newViewMode: ViewMode) {
        viewMode = newViewMode
    }

    fun updateLanguage(newLanguage: Language) {
        language = newLanguage
    }

    fun updateButtonSize(newButtonSize: Int) {
        buttonSize = newButtonSize
    }

    fun updateIconSize(newIconSize: Int) {
        iconSize = newIconSize
    }

    fun loadSettings() {
        val properties = Properties()
        try {
            FileInputStream(SETTINGS_FILE).use { properties.load(it) }
            showHiddenFiles = properties.getProperty("showHiddenFiles", "false").toBoolean()
            colorTheme = ColorTheme.valueOf(properties.getProperty("colorTheme", "SYSTEM"))
            viewMode = ViewMode.valueOf(properties.getProperty("viewMode", "TABLE"))
        } catch (e: Exception) {
            // TODO: handle exception, just pass?
            println("Error! Could not load settings")
        }
    }

    fun saveSettings() {
        val properties = Properties()
        properties.setProperty("showHiddenFiles", showHiddenFiles.toString())
        properties.setProperty("colorTheme", colorTheme.name)
        properties.setProperty("viewMode", viewMode.name)

        try {
            FileOutputStream(SETTINGS_FILE).use { properties.store(it, null) }
        } catch (e: Exception) {
            println("Error! Could ot save settings!")
        }
    }
}
