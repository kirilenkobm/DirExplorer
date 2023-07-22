package views

import java.awt.BorderLayout
import javax.swing.JFrame
import javax.swing.JScrollPane
import javax.swing.JPanel
import state.Settings
import state.ViewMode
import views.directoryviews.IconsDirectoryView
import views.directoryviews.TableDirectoryView
import java.awt.Dimension


class MainView {
    // TODO: better manage view updates and triggers
    // TODO: coroutines to get content of the current dir
    private val frame = JFrame("DirExplorer")
    private val topBarView = TopBarView(this, frame)
    private val tableView = TableDirectoryView(topBarView)
    private val iconsView = IconsDirectoryView(topBarView)
    private val mainPanel = JPanel(BorderLayout())

    fun updateView() {  // TODO: rename
        when (Settings.viewMode) {
            ViewMode.TABLE -> tableView.updateView()
            ViewMode.ICONS -> iconsView.updateView()
            ViewMode.COLUMNS -> iconsView.updateView()  // TODO
        }
        topBarView.updateView()
    }

    fun createAndShowGUI() {
        frame.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
        // Main Panel
        mainPanel.preferredSize = Dimension(800, 600) // Set to your preferred width and height

        when (Settings.viewMode) {
            ViewMode.TABLE -> mainPanel.add(JScrollPane(tableView.getTable()), BorderLayout.CENTER)
            ViewMode.ICONS -> mainPanel.add(JScrollPane(iconsView.getPanel()), BorderLayout.CENTER)
            ViewMode.COLUMNS -> mainPanel.add(JScrollPane(iconsView.getPanel()), BorderLayout.CENTER)  // TODO
        }

        // TODO: status bar
        // Compose views together
        frame.add(topBarView.getPanel(), BorderLayout.NORTH)
        frame.add(mainPanel, BorderLayout.CENTER)
        // frame.add(statusBar, BorderLayout.SOUTH)
        frame.pack()
        frame.isVisible = true
    }

    fun updateMainPanel() {
        mainPanel.removeAll()

        when (Settings.viewMode) {
            ViewMode.TABLE -> mainPanel.add(JScrollPane(tableView.getTable()), BorderLayout.CENTER)
            ViewMode.ICONS -> mainPanel.add(JScrollPane(iconsView.getPanel()), BorderLayout.CENTER)
            ViewMode.COLUMNS -> mainPanel.add(JScrollPane(iconsView.getPanel()), BorderLayout.CENTER)  // TODO
        }

        mainPanel.revalidate()
        mainPanel.repaint()
    }
}
