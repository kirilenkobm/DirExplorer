package views

import state.ColorTheme
import state.Settings
import java.awt.Component
import java.awt.Dimension
import java.awt.GridLayout
import java.awt.event.ItemEvent
import javax.swing.*

class SettingsDialog: JDialog() {
    init {
        title = "Settings"
        layout = GridLayout(0, 1) // TODO: define later

        // checkbox for hidden files
        val showHiddenFilesCheckbox = JCheckBox("Show hidden files").apply {
            isSelected = Settings.showHiddenFiles
            addItemListener {e ->
                Settings.toggleShowHiddenFiles()
                isSelected = Settings.showHiddenFiles
            }
        }

        // Dropdown for colorTheme
        // TODO: fix this warning:
        // 'Enum.values()' is recommended to be replaced by 'Enum.entries' since 1.9
        val colorThemeDropdown = JComboBox<ColorTheme>(ColorTheme.values()).apply {
            selectedItem = Settings.colorTheme
            addItemListener {e ->
                if (e.stateChange == ItemEvent.SELECTED) {
                    Settings.changeColorTheme(e.item as ColorTheme)
                }
            }
        }

        // Arrange the view
        val panel = JPanel()
        panel.layout = BoxLayout(panel, BoxLayout.Y_AXIS)
        panel.border = BorderFactory.createEmptyBorder(10, 10, 10, 10)

        val colorThemeLabel = JLabel("Color Theme")
        colorThemeLabel.alignmentX = Component.LEFT_ALIGNMENT
        panel.add(colorThemeLabel)
        panel.add(Box.createRigidArea(Dimension(0, 5))) // Add 5 pixes of space below
        colorThemeDropdown.alignmentX = Component.LEFT_ALIGNMENT
        panel.add(colorThemeDropdown)
        panel.add(Box.createRigidArea(Dimension(0, 5))) // again 5 pixels below
        showHiddenFilesCheckbox.alignmentX = Component.LEFT_ALIGNMENT
        panel.add(showHiddenFilesCheckbox)

        add(panel)
        pack()
        isResizable = false
    }
}