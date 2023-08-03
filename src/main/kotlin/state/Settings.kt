// Settings, especially defined by user
package state
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.Properties

/**
 * Singleton object representing the application settings.
 *
 * This object maintains the state of various user preferences and settings,
 * such as whether to show hidden files, the view mode, the color theme, the button and icon sizes,
 * the language, and the maximum image size for which to show thumbnails.
 *
 * The settings are stored in a properties file, and can be loaded from and saved to this file
 * using the `loadSettings` and `saveSettings` methods respectively.
 *
 * The `Settings` object also implements the observer pattern, allowing other parts of the
 * application to be notified when certain settings change. Observers are notified of
 * changes to the settings via the SettingsObserver interface.
 */
object Settings {
    private const val SETTINGS_FILE = "settings.properties"
    private val observers: MutableList<SettingsObserver> = mutableListOf()
    // Not sure whether belongs to settings
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

    var colorTheme: ColorTheme = ColorTheme.LIGHT
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
            // Just pass?
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
