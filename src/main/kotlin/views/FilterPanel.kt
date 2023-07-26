package views

import state.AppState
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Dimension
import javax.swing.BorderFactory
import javax.swing.JButton
import javax.swing.JPanel
import javax.swing.JTextField
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener


// TODO: implement update on filter
class FilterPanel {
    private val filterPanel = JPanel()

    init {
        setupFilterPanel()
    }

    private fun setupFilterPanel() {
        val filterField = JTextField(AppState.currentExtensionFilter).apply {
            border = BorderFactory.createEmptyBorder()
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
                updateFilter(filterField.text)
            }

            override fun removeUpdate(e: DocumentEvent?) {
                updateFilter(filterField.text)
            }

            override fun changedUpdate(e: DocumentEvent?) {
                updateFilter(filterField.text)
            }
        })

        // clear filter button
        val clearFilterButton = JButton(IconManager.backSpaceIcon).apply {
            isContentAreaFilled = false // transparent
            isBorderPainted = false // remove border
            isFocusPainted = false  // rm focus highlight
        }
        clearFilterButton.addActionListener {
            filterField.text = ""
            updateFilter("")
        }
        filterPanel.add(clearFilterButton, BorderLayout.EAST)
        filterPanel.border = BorderFactory.createMatteBorder(1, 1, 1, 1, Color.BLACK)
        filterPanel.background = Color(255, 255, 255, 255)
    }

    private fun updateFilter(text: String) {
        AppState.currentExtensionFilter = text
    }

    fun getPanel(): JPanel {
        return filterPanel
    }
}