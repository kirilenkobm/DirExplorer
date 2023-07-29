package state

interface SettingsObserver {
    fun onShowHiddenFilesChanged(newShowHiddenFiles: Boolean)
    fun onViewModeChanged(newViewMode: ViewMode)
    fun onColorThemeChanged(newColorTheme: ColorTheme)
    fun onLanguageChanged(newLanguage: Language)
    // TODO: check whether other methods needed
}
