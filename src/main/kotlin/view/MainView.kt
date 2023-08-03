package view

import Constants
import model.DirectoryObserver
import model.ExplorerDirectory
import state.*
import util.PopupViewThemeManager
import view.customcomponents.CustomScrollPane
import view.directoryviews.GridDirectoryView
import view.directoryviews.TableDirectoryView
import java.awt.*
import javax.swing.JFrame
import javax.swing.JPanel
import javax.swing.JScrollPane

/**
 * Primary user interface class for the DirExplorer application.
 *
 * The MainView class is initialized with the AppState and Settings objects,
 * and it registers itself as an observer of these objects in its init function.
 */
class MainView: DirectoryObserver, SettingsObserver {
    private val frame = JFrame("DirExplorer")
    private val topBarView = TopBarView(frame)
    private val tableView = TableDirectoryView()
    private val iconsView = GridDirectoryView()
    private val mainPanel = JPanel(BorderLayout())
    private val statusBarView = StatusBarView()

    init {
        AppState.addDirectoryObserver(this)
        Settings.addObserver(this)
        PopupViewThemeManager.onColorThemeChanged(Settings.colorTheme)
    }

    fun createAndShowGUI() {
        setupFrame()
        composeViews()
        frame.pack()
        frame.isVisible = true
    }

    private fun setupFrame() {
        frame.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
        mainPanel.preferredSize = Dimension(Constants.PREFERRED_WIDTH, 960)
        updateViewMode()
    }

    private fun composeViews() {
        frame.add(topBarView.getPanel(), BorderLayout.NORTH)
        frame.add(mainPanel, BorderLayout.CENTER)
        frame.add(statusBarView, BorderLayout.SOUTH)
    }

    /**
     * Controls whether to show grid or table
     */
    private fun updateViewMode() {
        mainPanel.removeAll()
        when (Settings.viewMode) {
            ViewMode.TABLE -> addTableView()
            ViewMode.GRID -> addIconsView()
        }
        mainPanel.revalidate()
        mainPanel.repaint()
    }

    private fun addTableView() {
        val scrollPane = JScrollPane(tableView.getTable()).apply {
            border = null
            if (Settings.colorTheme == ColorTheme.DARK) {
                viewport.background = Constants.BACKGROUND_COLOR_DARK
            }
        }
        mainPanel.add(scrollPane, BorderLayout.CENTER)
    }

    private fun addIconsView() {
        val scrollPane = CustomScrollPane(iconsView.getPanel()).apply {
            border = null
        }
        mainPanel.add(scrollPane, BorderLayout.CENTER)
    }

    /**
     * No need to do anything here if the child views will update
     * themselves independently because they also implement the DirectoryObserver interface
     */
    override fun onDirectoryChanged(newDirectory: ExplorerDirectory) {
        // Reset scroll position
        (mainPanel.getComponent(0) as JScrollPane).viewport.viewPosition = Point(0, 0)
    }
    // not applicable
    override fun onShowHiddenFilesChanged(newShowHiddenFiles: Boolean) {}

    override fun onViewModeChanged(newViewMode: ViewMode) {
        updateViewMode()
    }

    override fun onColorThemeChanged(newColorTheme: ColorTheme) {
        updateViewMode()
    }

    // there is nothing requiring localization
    override fun onLanguageChanged(newLanguage: Language) { }
}
