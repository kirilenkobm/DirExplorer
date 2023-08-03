package state

/**
 * Interface for observing changes in the application settings.
 *
 * This interface defines methods for handling changes to various settings in the application,
 * including whether to show hidden files, the view mode, the color theme, and the language.
 *
 * Classes implementing this interface will be notified when these settings change,
 * allowing them to update their state or the user interface accordingly.
 */
interface SettingsObserver {
    fun onShowHiddenFilesChanged(newShowHiddenFiles: Boolean)
    fun onViewModeChanged(newViewMode: ViewMode)
    fun onColorThemeChanged(newColorTheme: ColorTheme)
    fun onLanguageChanged(newLanguage: Language)
    // fill other settings if need be
}
