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


// TODO: implement update on filter
class FilterPanel {
    private val filterPanel = JPanel()
    private val filterField = JTextField()
    private val clearFilterButton = JButton()

    init {
        setupFilterPanel()
    }

    fun setupFilterPanel() {
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

        filterPanel.layout = BorderLayout()
        filterPanel.add(filterField, BorderLayout.CENTER)

        filterPanel.preferredSize = Dimension(200, filterField.preferredSize.height)
        filterPanel.minimumSize = Dimension(200, filterField.preferredSize.height)
        filterPanel.maximumSize = Dimension(200, filterField.preferredSize.height)

        // Set the maximum height to the preferred height to prevent vertical growth
        filterPanel.maximumSize = Dimension(filterPanel.maximumSize.width, filterPanel.preferredSize.height)

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

        filterPanel.revalidate()
        filterPanel.repaint()
    }

    fun getPanel(): JPanel {
        return filterPanel
    }
}