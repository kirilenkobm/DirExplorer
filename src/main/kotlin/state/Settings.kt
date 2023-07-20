// Settings, especially defined by user
package state

object Settings {
    var showHiddenFiles: Boolean = false
    var colorTheme: ColorTheme = ColorTheme.SYSTEM
    var defaultViewMode: ViewMode = ViewMode.ICONS

    fun loadSettings() {
        // Implement logic to load settings from persistent storage
    }

    fun saveSettings() {
        // Implement logic to save settings to persistent storage
    }

    fun toggleShowHiddenFiles() {
        // Implement logic to toggle show hidden files setting
    }

    fun changeColorTheme(newColorTheme: ColorTheme) {
        // Implement logic to change color theme
    }

    fun changeDefaultViewMode(newViewMode: ViewMode) {
        // Implement logic to change default view mode
    }

    // TODO: icon size
}
