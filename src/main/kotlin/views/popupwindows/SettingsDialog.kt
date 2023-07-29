package views.popupwindows

import state.ColorTheme
import state.Language
import state.Settings
import java.awt.Component
import java.awt.Dimension
import java.awt.GridLayout
import java.awt.event.ItemEvent
import java.util.ResourceBundle
import javax.swing.*

class SettingsDialog: JDialog() {
    init {
        val bundle = ResourceBundle.getBundle("languages/Messages", Settings.language.getLocale())
        title = bundle.getString("Settings")
        layout = GridLayout(0, 1)

        val showHiddenFilesCheckbox = createCheckbox(
            bundle.getString("ShowHidden"),
            Settings.showHiddenFiles
        ) {
            Settings.toggleShowHiddenFiles()
        }

        val colorThemeDropdown = createDropdown(
            ColorTheme.entries.toTypedArray(),
            Settings.colorTheme
        ) {
            Settings.changeColorTheme(it)
        }

        val languageDropdown = createDropdown(
            Language.entries.toTypedArray(),
            Settings.language
        ) {
            Settings.updateLanguage(it)
        }

        val panel = JPanel().apply {
            layout = BoxLayout(this, BoxLayout.Y_AXIS)
            border = BorderFactory.createEmptyBorder(10, 10, 10, 10)
        }

        addLabelAndDropdownToPanel(panel, bundle.getString("ColorTheme"), colorThemeDropdown)
        addLabelAndDropdownToPanel(panel, bundle.getString("SelectLanguage"), languageDropdown)
        panel.add(showHiddenFilesCheckbox)

        add(panel)
        pack()
        isResizable = false
    }

    private fun createCheckbox(text: String, isSelected: Boolean, onItemSelected: () -> Unit): JCheckBox {
        return JCheckBox(text).apply {
            this.isSelected = isSelected
            addItemListener {
                onItemSelected()
            }
        }
    }

    private fun <T> createDropdown(items: Array<T>, selectedItem: T, onItemSelected: (T) -> Unit): JComboBox<T> {
        return JComboBox(items).apply {
            this.selectedItem = selectedItem
            addItemListener { e ->
                if (e.stateChange == ItemEvent.SELECTED) {
                    @Suppress("UNCHECKED_CAST")
                    onItemSelected(e.item as T)
                }
            }
        }
    }

    private fun addLabelAndDropdownToPanel(panel: JPanel, labelText: String, dropdown: JComboBox<*>) {
        val label = JLabel(labelText).apply { alignmentX = Component.LEFT_ALIGNMENT }
        dropdown.alignmentX = Component.LEFT_ALIGNMENT
        panel.apply {
            add(label)
            add(Box.createRigidArea(Dimension(0, 5)))
            add(dropdown)
            add(Box.createRigidArea(Dimension(0, 5)))
        }
    }
}
