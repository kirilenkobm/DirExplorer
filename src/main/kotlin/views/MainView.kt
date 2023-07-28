package views

import Constants
import dataModels.DirectoryObserver
import dataModels.ExplorerDirectory
import state.*
import views.customcomponents.CustomScrollPane
import views.directoryviews.GridDirectoryView
import views.directoryviews.TableDirectoryView
import java.awt.*
import javax.swing.JFrame
import javax.swing.JPanel
import javax.swing.JScrollPane


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
    }

    fun createAndShowGUI() {
        frame.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
        mainPanel.preferredSize = Dimension(1280, 960)

        // draw the proper main panel that shows the directory view
        updateViewMode()
        // draw status bar
        // Compose views together
        frame.add(topBarView.getPanel(), BorderLayout.NORTH)
        frame.add(mainPanel, BorderLayout.CENTER)
        frame.add(statusBarView, BorderLayout.SOUTH)
        frame.pack()
        frame.isVisible = true
    }

    /**
     * Repaint the main panel that shows the content of the current dir
     * when the Settings.viewMode changes
     */
    private fun updateViewMode() {
        mainPanel.removeAll()

        when (Settings.viewMode) {
            ViewMode.TABLE -> {
                val scrollPane = JScrollPane(tableView.getTable())
                scrollPane.border = null
                if (Settings.colorTheme == ColorTheme.DARK) {
                    scrollPane.viewport.background = Constants.BACKGROUND_COLOR_DARK
                }
                mainPanel.add(scrollPane, BorderLayout.CENTER)
            }
            ViewMode.ICONS -> {
                val scrollPane = CustomScrollPane(iconsView.getPanel())
                scrollPane.border = null
                mainPanel.add(scrollPane, BorderLayout.CENTER)
            }
        }

        mainPanel.revalidate()
        mainPanel.repaint()
    }


    /**
     * When the AppState notifies the MainView about the
     * change of the current directory.
     */
    override fun onDirectoryChanged(newDirectory: ExplorerDirectory) {
        // No need to do anything here if the views update themselves
        // The child views will update themselves independently
        // because they also implement the DirectoryObserver interface
    }

    override fun onShowHiddenFilesChanged(newShowHiddenFiles: Boolean) {
        // Do nothing here?
    }

    override fun onViewModeChanged(newViewMode: ViewMode) {
        updateViewMode()
    }

    override fun onColorThemeChanged(newColorTheme: ColorTheme) {
        updateViewMode()
    }
}
