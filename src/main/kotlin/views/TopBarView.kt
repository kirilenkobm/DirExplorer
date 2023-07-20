package views

import state.AppState
import state.Settings
import state.ViewMode
import java.awt.BorderLayout
import java.awt.FlowLayout
import javax.swing.*

class TopBarView(private val mainView: MainView) {
    private val topBar = JSplitPane()
    private val leftPanel = JPanel()
    private val rightPanel = JPanel(FlowLayout(FlowLayout.RIGHT))
    private val addressBarView = AddressBarView(mainView)

    init {
        leftPanel.layout = BoxLayout(leftPanel, BoxLayout.X_AXIS)

        val backButton = JButton("Back")
        val forwardButton = JButton("Forward")
        val homeButton = JButton("Home")
        val upButton = JButton("Up")
        val filterField = JTextField(AppState.currentFilter)
        val settingsButton = JButton("Settings")

        val viewModeGroup = ButtonGroup()
        val columnButton = JToggleButton("Col")
        val tableButton = JToggleButton("Tab")
        val iconButton = JToggleButton("Icon")

        columnButton.addActionListener {
            Settings.updateViewMode(ViewMode.COLUMNS)
            mainView.updateView()
        }

        tableButton.addActionListener {
            Settings.updateViewMode(ViewMode.TABLE)
            mainView.updateView()
        }

        iconButton.addActionListener {
            Settings.updateViewMode(ViewMode.ICONS)
            mainView.updateView()
        }

        viewModeGroup.add(columnButton)
        viewModeGroup.add(tableButton)
        viewModeGroup.add(iconButton)

        backButton.addActionListener {
            AppState.goBack()
            mainView.updateView()
        }

        forwardButton.addActionListener {
            AppState.goForward()
            mainView.updateView()
        }

        homeButton.addActionListener {
            AppState.goHome()
            mainView.updateView()
        }

        upButton.addActionListener {
            AppState.goUp()
            mainView.updateView()
        }

        // left-aligned components
        leftPanel.add(backButton)
        leftPanel.add(forwardButton)
        leftPanel.add(homeButton)
        leftPanel.add(upButton)
        leftPanel.add(Box.createHorizontalGlue())
        leftPanel.add(addressBarView.getPanel())
        leftPanel.add(filterField)

        // right-aligned components
        rightPanel.add(columnButton)
        rightPanel.add(tableButton)
        rightPanel.add(iconButton)
        rightPanel.add(settingsButton)

//        topBar.add(leftPanel, BorderLayout.WEST)
//        topBar.add(rightPanel, BorderLayout.EAST)

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