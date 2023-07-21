package views

import state.AppState
import state.Settings
import state.ViewMode
import java.awt.*
import javax.swing.*
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener

class TopBarView(private val mainView: MainView) {
    private val topBar = JSplitPane()
    private val leftPanel = JPanel()
    private val rightPanel = JPanel(FlowLayout(FlowLayout.RIGHT))
    private val addressBarView = AddressBarView(mainView)

    private fun createButton(icon: ImageIcon, size: Int, action: () -> Unit): JButton {
        val resizedIcon = ImageIcon(icon.image.getScaledInstance(size, size, Image.SCALE_SMOOTH))
        return JButton(resizedIcon).apply {
            addActionListener {
                action()
                mainView.updateView()
            }
        }
    }

    private fun createToggleButton(icon: ImageIcon, size: Int, action: () -> Unit): JToggleButton {
        val resizedIcon = ImageIcon(icon.image.getScaledInstance(size, size, Image.SCALE_SMOOTH))
        return JToggleButton(resizedIcon).apply {
            addActionListener {
                action()
                mainView.updateView()
            }
        }
    }

    private fun createFilterPanel(): JPanel {
        val filterField = JTextField(AppState.currentFilter)
        val filterPanel = JPanel(BorderLayout())
        filterPanel.add(filterField, BorderLayout.CENTER)
        filterPanel.preferredSize = Dimension(200, filterField.preferredSize.height)
        filterPanel.minimumSize = Dimension(200, filterField.preferredSize.height)
        filterPanel.maximumSize = Dimension(200, filterField.preferredSize.height)

        // Set the maximum height to the preferred height to prevent vertical growth
        filterPanel.maximumSize = Dimension(filterPanel.maximumSize.width, filterPanel.preferredSize.height)

        // Add a document listener to update AppState.currentFilter whenever the text changes
        filterField.document.addDocumentListener(object : DocumentListener {
            override fun insertUpdate(e: DocumentEvent?) {
                AppState.currentFilter = filterField.text
            }

            override fun removeUpdate(e: DocumentEvent?) {
                AppState.currentFilter = filterField.text
            }

            override fun changedUpdate(e: DocumentEvent?) {
                AppState.currentFilter = filterField.text
            }
        })

        // Add a clear button to the filter field
        val clearFilterButton = JButton("x")
        clearFilterButton.addActionListener {
            filterField.text = ""
            AppState.currentFilter = ""
        }
        filterPanel.add(clearFilterButton, BorderLayout.EAST)

        return filterPanel
    }


    init {
        leftPanel.layout = BoxLayout(leftPanel, BoxLayout.X_AXIS)

        // Buttons on the left: for navigation
        val backButton = createButton(IconManager.backArrowIcon, Settings.iconSize) {
            AppState.goBack()
        }

        val forwardButton = createButton(IconManager.forwardArrowIcon, Settings.iconSize) {
            AppState.goForward()
        }

        val homeButton = createButton(IconManager.homeIcon, Settings.iconSize) {
            AppState.goHome()
        }

        val upButton = createButton(IconManager.upArrowIcon, Settings.iconSize) {
            AppState.goUp()
        }
        // >>>> Buttons on the left: navigation

        // Address bar and filter
        val addressBarPanel = addressBarView.getPanel()
        addressBarPanel.maximumSize = Dimension(addressBarPanel.maximumSize.width, addressBarPanel.preferredSize.height)
        addressBarPanel.preferredSize = Dimension(400, addressBarPanel.preferredSize.height)
        addressBarPanel.minimumSize = Dimension(400, addressBarPanel.preferredSize.height)

        val filterPanel = createFilterPanel()
        // >>>> Address bar and filter

        // Buttons on the right: Settings
        val viewModeGroup = ButtonGroup()
        val tableButton = createToggleButton(IconManager.tocIcon, Settings.iconSize) {
            Settings.updateViewMode(ViewMode.TABLE)
        }

        val iconButton = createToggleButton(IconManager.viewModuleIcon, Settings.iconSize) {
            Settings.updateViewMode(ViewMode.ICONS)
        }
        viewModeGroup.add(tableButton)
        viewModeGroup.add(iconButton)

        val settingsButton = createButton(IconManager.settingsIcon, Settings.iconSize) {
            // TODO: Define action for settings button
        }
        // >>>> Buttons on the right: Settings

        // left-aligned components
        leftPanel.add(backButton)
        leftPanel.add(forwardButton)
        leftPanel.add(homeButton)
        leftPanel.add(upButton)
        leftPanel.add(Box.createHorizontalStrut(100)) // add 100px of space

        leftPanel.add(addressBarView.getPanel())
        leftPanel.add(Box.createHorizontalStrut(20)) // add 100px of space
        leftPanel.add(filterPanel)

        // right-aligned components
        rightPanel.add(tableButton)
        rightPanel.add(iconButton)
        rightPanel.add(settingsButton)

        topBar.leftComponent = leftPanel
        topBar.rightComponent = rightPanel
        topBar.resizeWeight = 0.7
    }

    fun updateView() {
        addressBarView.updateView()
    }

    fun getPanel(): JSplitPane {
        return topBar
    }
}