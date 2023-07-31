package view

import state.AppState
import state.ColorTheme
import state.Settings
import util.IconManager
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Font
import javax.swing.BorderFactory
import javax.swing.JButton
import javax.swing.JPanel
import javax.swing.JTextField
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener

/**
 * Class representing the file extension filter panel in the user interface.
 *
 * It consists of a text field where users can enter the desired file extension, and a button to clear the filter.
 *
 * The filter panel is updated in real-time as the user types into the text field.
 * The current filter is stored in the AppState singleton object.
 */
class FilterPanel {
    private val filterPanel = JPanel(BorderLayout())
    private val filterField = JTextField()
    private val clearFilterButton = JButton()

    init {
        setupFilterPanel()
    }

    private fun setupFilterPanel() {
        configureFilterField()
        configureClearFilterButton()

        filterPanel.add(filterField, BorderLayout.CENTER)
        filterPanel.add(clearFilterButton, BorderLayout.EAST)

        filterPanel.border = BorderFactory.createMatteBorder(1, 1, 1, 1, Color.BLACK)
        filterPanel.background = getBackgroundColor()
    }

    private fun configureFilterField() {
        filterField.apply {
            text = AppState.getFilter()
            border = BorderFactory.createEmptyBorder()
            font = Font("Arial", Font.PLAIN, 14)
            background = getBackgroundColor()
            foreground = getForegroundColor()
            caretColor = getForegroundColor()
            document.addDocumentListener(object : DocumentListener {
                override fun insertUpdate(e: DocumentEvent?) {
                    AppState.updateFilter(text)
                }

                override fun removeUpdate(e: DocumentEvent?) {
                    AppState.updateFilter(text)
                }

                override fun changedUpdate(e: DocumentEvent?) {
                    AppState.updateFilter(text)
                }
            })
        }
    }

    private fun getBackgroundColor(): Color {
        return if (Settings.colorTheme == ColorTheme.LIGHT) {
            Color.WHITE
        } else {
            Color.DARK_GRAY
        }
    }

    private fun getForegroundColor(): Color {
        return if (Settings.colorTheme == ColorTheme.LIGHT) {
            Color.BLACK
        } else {
            Color.WHITE
        }
    }

    private fun configureClearFilterButton() {
        clearFilterButton.apply {
            icon = IconManager.backSpaceIcon
            isContentAreaFilled = false // transparent
            isBorderPainted = false // remove border
            isFocusPainted = false  // rm focus highlight
            addActionListener {
                filterField.text = ""
                AppState.updateFilter("")
            }
        }
    }

    fun updateView() {
        setupFilterPanel()
        filterPanel.revalidate()
        filterPanel.repaint()
    }

    fun getPanel(): JPanel {
        return filterPanel
    }
}
