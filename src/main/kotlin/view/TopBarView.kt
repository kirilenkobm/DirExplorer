package view

import Constants
import state.*
import util.IconManager
import view.popupwindows.SettingsDialog
import java.awt.*
import javax.swing.*

/**
 * Class representing the top bar view of the application.
 *
 * This class is responsible for creating and managing the top bar of the application,
 * which includes navigation buttons, the address bar, the file filter, and settings button.
 *
 * The left panel contains the navigation buttons, the address bar, and the file filter.
 * The right panel contains the view mode buttons and the settings button.
 *
 * The class implements the `SettingsObserver` interface.
 */
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

    private fun setupAddressBarPanel(): JPanel {
        val addressBarPanel = addressBarView.getPanel()
        addressBarPanel.maximumSize = Dimension(
            addressBarPanel.maximumSize.width,
            addressBarPanel.preferredSize.height
        )
        addressBarPanel.preferredSize = Dimension(400, addressBarPanel.preferredSize.height)
        addressBarPanel.minimumSize = Dimension(400, addressBarPanel.preferredSize.height)
        return addressBarPanel
    }

    private fun setupFilterView(addressBarHeight: Int): JPanel {
        val filterView = filterPanel.getPanel()
        filterView.preferredSize = Dimension(filterView.preferredSize.width, addressBarHeight)
        filterView.minimumSize = Dimension(filterView.minimumSize.width, addressBarHeight)
        filterView.maximumSize = Dimension(filterView.maximumSize.width, addressBarHeight)
        return filterView
    }

    private fun addAddressBarAndFilter() {
        val addressBarPanel = setupAddressBarPanel()

        val filterLabel = JLabel(" /*. ").apply {
            foreground = if (Settings.colorTheme == ColorTheme.LIGHT) Color.BLACK else Color.WHITE
            font = Font("Arial", Font.PLAIN, 14)
        }

        val filterView = setupFilterView(addressBarPanel.preferredSize.height)

        leftPanel.add(addressBarPanel)
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
            Settings.updateViewMode(ViewMode.GRID)
        }.apply {
            isSelected = Settings.viewMode == ViewMode.GRID
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

    // Buttons are almost the same, to avoid code duplication
    // I created an abstract button
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

    // not applicable here
    override fun onShowHiddenFilesChanged(newShowHiddenFiles: Boolean) { }

    // not applicable here
    override fun onViewModeChanged(newViewMode: ViewMode) { }

    private fun refreshUIComponents() {
        leftPanel.revalidate()
        leftPanel.repaint()
        rightPanel.revalidate()
        rightPanel.repaint()
    }

    override fun onColorThemeChanged(newColorTheme: ColorTheme) {
        // remove all components and recreate the panel
        leftPanel.removeAll()
        rightPanel.removeAll()
        configurePanel()

        // update subviews
        addressBarView.updateView()
        filterPanel.updateView()
        refreshUIComponents()
    }

    override fun onLanguageChanged(newLanguage: Language) { }
}
