package views.iconviews

import state.AppState
import views.AddressBarView
import views.MainView
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Dimension
import javax.swing.*
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener

// Extension filter view
class FilterPanel(private val mainView: MainView, private val addressBarView: AddressBarView) {
    val filterPanel = JPanel()

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
        val clearFilterButton = JButton("x").apply {
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

    }

    private fun updateFilter(text: String) {
        AppState.currentExtensionFilter = text
        addressBarView.updateView()
        mainView.updateView()
        mainView.updateMainPanel()
    }

    fun getPanel(): JPanel {
        return filterPanel
    }
}