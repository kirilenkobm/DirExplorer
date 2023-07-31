package util

import state.*
import java.awt.Color
import javax.swing.SwingUtilities
import javax.swing.UIManager


/**
 * Singleton object responsible for managing the color scheme of popup views in the application.
 *
 * This object observes changes in the application settings and updates the color scheme of popup views accordingly.
 * It implements the SettingsObserver interface to respond to these changes.
 **/
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
        UIManager.put("ComboBox.background", Color.DARK_GRAY)
        UIManager.put("ComboBox.foreground", Color.WHITE)
        UIManager.put("ComboBox.selectionBackground", Color.GRAY)
        UIManager.put("ComboBox.selectionForeground", Color.WHITE)
    }

    private fun setDefaultTheme() {
        UIManager.put("OptionPane.background", null)
        UIManager.put("Panel.background", null)
        UIManager.put("OptionPane.messageForeground", null)
        UIManager.put("Button.background", null)
        UIManager.put("Button.foreground", null)
        UIManager.put("ComboBox.background", null)
        UIManager.put("ComboBox.foreground", null)
        UIManager.put("ComboBox.selectionBackground", null)
        UIManager.put("ComboBox.selectionForeground", null)
    }

    override fun onColorThemeChanged(newColorTheme: ColorTheme) {
        when (newColorTheme) {
            ColorTheme.LIGHT -> setDefaultTheme()
            ColorTheme.DARK -> setDarkTheme()
        }
    }

    override fun onShowHiddenFilesChanged(newShowHiddenFiles: Boolean) { }

    override fun onViewModeChanged(newViewMode: ViewMode) { }

    override fun onLanguageChanged(newLanguage: Language) { }
}
