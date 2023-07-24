package views

import state.AppState
import state.Settings
import state.ViewMode
import java.awt.*
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import javax.swing.*
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener


// TODO: lock buttons for not available actions, like go up if already at root
class TopBarView(private val mainView: MainView, private val frame: JFrame) {
    private val topBar = JSplitPane()
    private val leftPanel = JPanel()
    private val rightPanel = JPanel(FlowLayout(FlowLayout.RIGHT))
    private val addressBarView = AddressBarView(mainView)
    private var settingsDialog: SettingsDialog? = null


    private fun createButton(icon: ImageIcon,
                             size: Int,
                             action: () -> Unit): JButton
    {
        val resizedIcon = ImageIcon(icon.image.getScaledInstance(size, size, Image.SCALE_SMOOTH))
        return JButton(resizedIcon).apply {
            addActionListener {
                action()
                mainView.updateView()
            }
        }
    }

    private fun createToggleButton(icon: ImageIcon,
                                   size: Int,
                                   action: () -> Unit): JToggleButton
    {
        val resizedIcon = ImageIcon(icon.image.getScaledInstance(size, size, Image.SCALE_SMOOTH))
        return JToggleButton(resizedIcon).apply {
            addActionListener {
                action()
                mainView.updateView()
                mainView.updateMainPanel()
            }
        }
    }

    private fun createFilterPanel(): JPanel {
        val filterField = JTextField(AppState.currentExtensionFilter)
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
                AppState.currentExtensionFilter = filterField.text
                // TODO: 3 updates -> probably I messed something up here
                updateView()
                mainView.updateView()
                mainView.updateMainPanel()
            }

            override fun removeUpdate(e: DocumentEvent?) {
                AppState.currentExtensionFilter = filterField.text
                updateView()
                mainView.updateView()
                mainView.updateMainPanel()
            }

            override fun changedUpdate(e: DocumentEvent?) {
                AppState.currentExtensionFilter = filterField.text
                updateView()
                mainView.updateView()
                mainView.updateMainPanel()
            }
        })

        // Add a clear button to the filter field
        val clearFilterButton = JButton("x")
        clearFilterButton.addActionListener {
            filterField.text = ""
            AppState.currentExtensionFilter = ""
        }
        filterPanel.add(clearFilterButton, BorderLayout.EAST)

        return filterPanel
    }

    init {
        leftPanel.layout = BoxLayout(leftPanel, BoxLayout.X_AXIS)

        // Buttons on the left: for navigation
        val backButton = createButton(IconManager.backArrowIcon, Settings.buttonSize) { AppState.goBack() }

        val forwardButton = createButton(IconManager.forwardArrowIcon, Settings.buttonSize) { AppState.goForward() }

        val homeButton = createButton(IconManager.homeIcon, Settings.buttonSize) { AppState.goHome() }

        val upButton = createButton(IconManager.upArrowIcon, Settings.buttonSize) { AppState.goUp() }
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
        val tableButton = createToggleButton(IconManager.tocIcon, Settings.buttonSize) {
            Settings.updateViewMode(ViewMode.TABLE)
        }.apply {
            isSelected = Settings.viewMode == ViewMode.TABLE
        }


        val iconButton = createToggleButton(IconManager.viewModuleIcon, Settings.buttonSize) {
            Settings.updateViewMode(ViewMode.ICONS)
        }.apply {
            isSelected = Settings.viewMode == ViewMode.ICONS
        }

        viewModeGroup.add(tableButton)
        viewModeGroup.add(iconButton)

        // TODO: save changes right after the window is closed
        val settingsButton = createButton(IconManager.settingsIcon, Settings.buttonSize) {
            if (settingsDialog?.isVisible != true)  // to ensure only one settings view is shown
            {
                settingsDialog = SettingsDialog().apply {
                    // to locate it in the middle of the main view, not in the
                    // top left corner of the screen
                    setLocationRelativeTo(frame)

                    // ideally to apply changes after the window is closed
                    addWindowListener(object : WindowAdapter() {
                        override fun windowClosed(e: WindowEvent?) {
                            mainView.updateView()
                            mainView.updateMainPanel()
                        }
                    })
                    isVisible = true
                }
            }
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
        rightPanel.add(Box.createHorizontalStrut(20)) // add 100px of space
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
