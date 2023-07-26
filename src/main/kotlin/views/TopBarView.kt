package views

import dataModels.DirectoryObserver
import dataModels.ExplorerDirectory
import state.AppState
import state.Settings
import state.ViewMode
import java.awt.*
import javax.swing.*


// TODO: lock buttons for not available actions, like go up if already at root
class TopBarView(private val frame: JFrame) {
    private val topBar = JSplitPane()
    private val leftPanel = JPanel()
    private val rightPanel = JPanel(FlowLayout(FlowLayout.RIGHT))
    private val addressBarView = AddressBarView()
    private var settingsDialog: SettingsDialog? = null
    private val filterPanel = FilterPanel()


    private fun createButton(icon: ImageIcon,
                             size: Int,
                             action: () -> Unit): JButton
    {
        val resizedIcon = ImageIcon(icon.image.getScaledInstance(size, size, Image.SCALE_SMOOTH))
        return JButton(resizedIcon).apply {
            isContentAreaFilled = false // make the button transparent
            isBorderPainted = false // remove the border
            isFocusPainted = false // remove the focus highlight
            cursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
            addActionListener {
                action()
            }
        }
    }

    private fun createToggleButton(icon: ImageIcon,
                                   size: Int,
                                   action: () -> Unit): JToggleButton
    {
        val resizedIcon = ImageIcon(icon.image.getScaledInstance(size, size, Image.SCALE_SMOOTH))
        return JToggleButton(resizedIcon).apply {
            isContentAreaFilled = false // make the button transparent
            isBorderPainted = false // remove the border
            isFocusPainted = false // remove the focus highlight
            cursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
            addActionListener {
                action()
            }
        }
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

        val settingsButton = createButton(IconManager.settingsIcon, Settings.buttonSize) {
            if (settingsDialog?.isVisible != true)  // to ensure only one settings view is shown
            {
                settingsDialog = SettingsDialog().apply {
                    // to locate it in the middle of the main view, not in the
                    // top left corner of the screen
                    setLocationRelativeTo(frame)
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
        leftPanel.add(Box.createHorizontalStrut(20)) // add 100px of space

        val addressBar = addressBarView.getPanel()
        val filterLabel = JLabel(" /*. ")
        val filterView = filterPanel.getPanel()

        leftPanel.add(addressBar)

        val addressBarHeight = addressBar.preferredSize.height
        filterView.preferredSize = Dimension(filterView.preferredSize.width, addressBarHeight)
        filterView.minimumSize = Dimension(filterView.minimumSize.width, addressBarHeight)
        filterView.maximumSize = Dimension(filterView.maximumSize.width, addressBarHeight)

        // leftPanel.add(Box.createHorizontalStrut(20)) // add 100px of space
        leftPanel.add(filterLabel)
        leftPanel.add(filterView)

        // right-aligned components
        rightPanel.add(tableButton)
        rightPanel.add(iconButton)
        rightPanel.add(Box.createHorizontalStrut(20)) // add 100px of space
        rightPanel.add(settingsButton)

        topBar.leftComponent = leftPanel
        topBar.rightComponent = rightPanel
        topBar.resizeWeight = 0.7
    }

    fun getPanel(): JSplitPane {
        return topBar
    }
}
