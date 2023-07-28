package utils

import state.ColorTheme
import state.Settings
import state.SettingsObserver
import state.ViewMode
import java.awt.Color
import javax.swing.UIManager


/**
 * Implements workaround to change color scheme in
 * popup views like settings, error, loading bar, etc.
 */
object PopupViewThemeManager : SettingsObserver {
    init {
        Settings.addObserver(this)
    }

    private fun setDarkTheme() {
        UIManager.put("OptionPane.background", Color.DARK_GRAY)
        UIManager.put("Panel.background", Color.DARK_GRAY)
        UIManager.put("OptionPane.messageForeground", Color.WHITE)
        UIManager.put("Button.background", Color.GRAY)
        UIManager.put("Button.foreground", Color.WHITE)
    }

    private fun setDefaultTheme() {
        UIManager.put("OptionPane.background", null)
        UIManager.put("Panel.background", null)
        UIManager.put("OptionPane.messageForeground", null)
        UIManager.put("Button.background", null)
        UIManager.put("Button.foreground", null)
    }

    override fun onColorThemeChanged(newColorTheme: ColorTheme) {
        when (newColorTheme) {
            ColorTheme.LIGHT -> setDefaultTheme()
            ColorTheme.DARK -> setDarkTheme()
        }
    }

    override fun onShowHiddenFilesChanged(newShowHiddenFiles: Boolean) { }

    override fun onViewModeChanged(newViewMode: ViewMode) { }
}