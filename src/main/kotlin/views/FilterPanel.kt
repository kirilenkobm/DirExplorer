package views

import state.AppState
import state.ColorTheme
import state.Settings
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Dimension
import java.awt.Font
import javax.swing.BorderFactory
import javax.swing.JButton
import javax.swing.JPanel
import javax.swing.JTextField
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener


class FilterPanel {
    private val filterPanel = JPanel(BorderLayout())
    private val filterField = JTextField()
    private val clearFilterButton = JButton()

    init {
        setupFilterPanel()
    }

    private fun setupFilterPanel() {
        // Configure filter field
        filterField.text = AppState.currentExtensionFilter
        filterField.border = BorderFactory.createEmptyBorder()
        filterField.font = Font("Arial", Font.PLAIN, 14)
        filterField.background = if (Settings.colorTheme == ColorTheme.LIGHT) {
            Color.WHITE
        } else {
            Color.DARK_GRAY
        }
        filterField.foreground = if (Settings.colorTheme == ColorTheme.LIGHT) {
            Color.BLACK
        } else {
            Color.WHITE
        }
        // Add a document listener to update AppState.currentFilter whenever the text changes
        filterField.document.addDocumentListener(object : DocumentListener {
            override fun insertUpdate(e: DocumentEvent?) {
                AppState.updateFilter(filterField.text)
            }

            override fun removeUpdate(e: DocumentEvent?) {
                AppState.updateFilter(filterField.text)
            }

            override fun changedUpdate(e: DocumentEvent?) {
                AppState.updateFilter(filterField.text)
            }
        })

        filterPanel.add(filterField, BorderLayout.CENTER)

        // Configure clear filter button
        clearFilterButton.icon = IconManager.backSpaceIcon
        clearFilterButton.isContentAreaFilled = false // transparent
        clearFilterButton.isBorderPainted = false // remove border
        clearFilterButton.isFocusPainted = false  // rm focus highlight
        clearFilterButton.addActionListener {
            filterField.text = ""
            AppState.updateFilter("")
        }
        filterPanel.add(clearFilterButton, BorderLayout.EAST)

        filterPanel.border = BorderFactory.createMatteBorder(1, 1, 1, 1, Color.BLACK)
        filterPanel.background = if (Settings.colorTheme == ColorTheme.LIGHT) {
            Color(255, 255, 255, 255)
        } else {
            Color.DARK_GRAY
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
