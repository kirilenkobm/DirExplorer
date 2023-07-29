// Settings, especially defined by user
package state
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.Properties

object Settings {
    private const val SETTINGS_FILE = "settings.properties"
    private val observers: MutableList<SettingsObserver> = mutableListOf()

    var showHiddenFiles: Boolean = false
        set(value) {
            field = value
            observers.forEach { it.onShowHiddenFilesChanged(value) }
        }

    var viewMode: ViewMode = ViewMode.GRID
        set(value) {
            field = value
            observers.forEach { it.onViewModeChanged(value) }
        }

    var colorTheme: ColorTheme = ColorTheme.DARK
        set(value) {
            field = value
            observers.forEach { it.onColorThemeChanged(value) }
        }

    var buttonSize: Int = 20
    var iconSize: Int = 72
    var language: Language = Language.ENGLISH
        set(value) {
            field = value
            observers.forEach { it.onLanguageChanged(value) }
        }

    var maxImageSizeToShowThumbnail = 6000

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

    fun loadSettings() {
        val properties = Properties()
        try {
            FileInputStream(SETTINGS_FILE).use { properties.load(it) }
            showHiddenFiles = properties.getProperty("showHiddenFiles", "false").toBoolean()
            colorTheme = ColorTheme.valueOf(properties.getProperty("colorTheme", "SYSTEM"))
            viewMode = ViewMode.valueOf(properties.getProperty("viewMode", "TABLE"))
            language = Language.valueOf(properties.getProperty("language", "ENGLISH"))
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
        properties.setProperty("language", language.name)

        try {
            FileOutputStream(SETTINGS_FILE).use { properties.store(it, null) }
        } catch (e: Exception) {
            println("Error! Could ot save settings!")
        }
    }

    fun addObserver(observer: SettingsObserver) {
        observers.add(observer)
    }

    // TODO: do not forget to cleanup observers
    fun removeObserver(observer: SettingsObserver) {
        observers.remove(observer)
    }
}
