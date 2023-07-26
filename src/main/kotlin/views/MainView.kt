package views

import state.AppState
import state.ColorTheme
import java.awt.BorderLayout
import javax.swing.JFrame
import javax.swing.JScrollPane
import javax.swing.JPanel
import state.Settings
import state.ViewMode
import views.directoryviews.IconsDirectoryView
import views.directoryviews.TableDirectoryView
import java.awt.Color
import java.awt.Dimension


class MainView {
    // TODO: better manage view updates and triggers
    private val frame = JFrame("DirExplorer")
    private val topBarView = TopBarView(this, frame)
    private val tableView = TableDirectoryView(topBarView)
    private val iconsView = IconsDirectoryView(topBarView)
    private val mainPanel = JPanel(BorderLayout())
    private val statusBarView = StatusBarView()


    fun updateView() {
        when (Settings.viewMode) {
            ViewMode.TABLE -> tableView.updateView()
            ViewMode.ICONS -> iconsView.updateView()
            ViewMode.COLUMNS -> iconsView.updateView()
        }
        topBarView.updateView()
    }


    fun createAndShowGUI() {
        frame.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
        mainPanel.preferredSize = Dimension(1280, 800)

        when (Settings.viewMode) {
            ViewMode.TABLE -> mainPanel.add(JScrollPane(tableView.getTable()), BorderLayout.CENTER)
            ViewMode.ICONS -> mainPanel.add(JScrollPane(iconsView.getPanel()), BorderLayout.CENTER)
            ViewMode.COLUMNS -> mainPanel.add(JScrollPane(iconsView.getPanel()), BorderLayout.CENTER)  // TODO
        }

        updateStatusBar()
        // Compose views together
        frame.add(topBarView.getPanel(), BorderLayout.NORTH)
        frame.add(mainPanel, BorderLayout.CENTER)
        frame.add(statusBarView, BorderLayout.SOUTH)
        frame.pack()
        frame.isVisible = true
    }

    private fun updateStatusBar() {
        val itemsCount = 1000
        val totalSize = 1000000000L
        statusBarView.updateStatus(itemsCount, totalSize)
    }

    fun updateMainPanel() {
        mainPanel.removeAll()

        when (Settings.viewMode) {
            ViewMode.TABLE -> mainPanel.add(JScrollPane(tableView.getTable()), BorderLayout.CENTER)
            ViewMode.ICONS -> mainPanel.add(JScrollPane(iconsView.getPanel()), BorderLayout.CENTER)
            ViewMode.COLUMNS -> mainPanel.add(JScrollPane(iconsView.getPanel()), BorderLayout.CENTER)  // TODO
        }
        updateStatusBar()

        mainPanel.revalidate()
        mainPanel.repaint()
    }
}
