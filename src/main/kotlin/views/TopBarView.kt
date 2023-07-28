package views

import Constants
import state.*
import utils.IconManager
import views.popupwindows.SettingsDialog
import java.awt.*
import javax.swing.*


class TopBarView(private val frame: JFrame) : SettingsObserver {
    private val topBar = JSplitPane()
    private val leftPanel = JPanel()
    private val rightPanel = JPanel(FlowLayout(FlowLayout.RIGHT))
    private val addressBarView = AddressBarView()
    private var settingsDialog: SettingsDialog? = null
    private val filterPanel = FilterPanel()

    init {
        configurePanel()
        Settings.addObserver(this)
    }

    private fun configurePanel() {
        leftPanel.layout = BoxLayout(leftPanel, BoxLayout.X_AXIS)
        addNavigationButtons()
        addAddressBarAndFilter()
        addRightPanelButtons()
        applyThemeColors()

        topBar.leftComponent = leftPanel
        topBar.rightComponent = rightPanel
        topBar.resizeWeight = 0.7
    }

    private fun addNavigationButtons() {
        val navigationButtons = arrayOf(
            Pair(IconManager.backArrowIcon) { AppState.goBack() },
            Pair(IconManager.forwardArrowIcon) { AppState.goForward() },
            Pair(IconManager.homeIcon) { AppState.goHome() },
            Pair(IconManager.upArrowIcon) { AppState.goUp() }
        )

        navigationButtons.forEach {
            val (icon, action) = it
            leftPanel.add(createButton(icon, Settings.buttonSize, action))
        }

        leftPanel.add(Box.createHorizontalStrut(20)) // add 100px of space
    }

    private fun addAddressBarAndFilter() {
        val addressBarPanel = addressBarView.getPanel()
        addressBarPanel.maximumSize = Dimension(addressBarPanel.maximumSize.width, addressBarPanel.preferredSize.height)
        addressBarPanel.preferredSize = Dimension(400, addressBarPanel.preferredSize.height)
        addressBarPanel.minimumSize = Dimension(400, addressBarPanel.preferredSize.height)

        val addressBar = addressBarView.getPanel()
        val filterLabel = JLabel(" /*. ")
        filterLabel.foreground = if (Settings.colorTheme == ColorTheme.LIGHT) {
            Color.BLACK
        } else {
            Color.WHITE
        }
        filterLabel.font = Font("Arial", Font.PLAIN, 14)
        val filterView = filterPanel.getPanel()

        val addressBarHeight = addressBar.preferredSize.height
        filterView.preferredSize = Dimension(filterView.preferredSize.width, addressBarHeight)
        filterView.minimumSize = Dimension(filterView.minimumSize.width, addressBarHeight)
        filterView.maximumSize = Dimension(filterView.maximumSize.width, addressBarHeight)

        leftPanel.add(addressBar)
        leftPanel.add(filterLabel)
        leftPanel.add(filterView)
    }

    private fun addRightPanelButtons() {
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

        rightPanel.add(tableButton)
        rightPanel.add(iconButton)
        rightPanel.add(Box.createHorizontalStrut(20)) // add 100px of space
        rightPanel.add(settingsButton)
    }

    private fun applyThemeColors() {
        if (Settings.colorTheme == ColorTheme.DARK) {
            leftPanel.background = Color.DARK_GRAY
            rightPanel.background = Color.DARK_GRAY
            topBar.background = Color.DARK_GRAY
        } else {
            leftPanel.background = Constants.DEFAULT_SWING_BACKGROUND_COLOR
            rightPanel.background = Constants.DEFAULT_SWING_BACKGROUND_COLOR
            topBar.background = Constants.DEFAULT_SWING_BACKGROUND_COLOR
        }
    }

    private fun <T : AbstractButton> createButtonWithIcon(
        button: T,
        iconArg: ImageIcon,
        size: Int,
        action: () -> Unit
    ): T {
        val resizedIcon = ImageIcon(iconArg.image.getScaledInstance(size, size, Image.SCALE_SMOOTH))
        return button.apply {
            icon = resizedIcon
            isContentAreaFilled = false // make the button transparent
            isBorderPainted = false // remove the border
            isFocusPainted = false // remove the focus highlight
            cursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
            addActionListener {
                action()
            }
        }
    }

    private fun createButton(icon: ImageIcon, size: Int, action: () -> Unit) =
        createButtonWithIcon(JButton(), icon, size, action)

    private fun createToggleButton(icon: ImageIcon, size: Int, action: () -> Unit) =
        createButtonWithIcon(JToggleButton(), icon, size, action)

    fun getPanel(): JSplitPane {
        return topBar
    }

    override fun onShowHiddenFilesChanged(newShowHiddenFiles: Boolean) { }

    override fun onViewModeChanged(newViewMode: ViewMode) { }

    override fun onColorThemeChanged(newColorTheme: ColorTheme) {
        // remove all components
        leftPanel.removeAll()
        rightPanel.removeAll()

        // recreate panel
        configurePanel()

        // update view
        addressBarView.updateView()
        filterPanel.updateView()

        // revalidate and repaint
        leftPanel.revalidate()
        leftPanel.repaint()
        rightPanel.revalidate()
        rightPanel.repaint()
    }
}
