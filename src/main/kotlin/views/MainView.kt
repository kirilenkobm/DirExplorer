package views

import dataModels.ExplorerFile
import dataModels.ExplorerDirectory
import state.AppState
import java.awt.BorderLayout
import java.awt.FlowLayout
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.BorderFactory
import javax.swing.JButton
import javax.swing.JFrame
import javax.swing.JLabel
import javax.swing.JScrollPane
import javax.swing.JTable
import javax.swing.JPanel
import javax.swing.JTextField
import javax.swing.table.DefaultTableModel
import dataModels.FileSystemEntity
import state.Settings
import state.ViewMode
import java.awt.Dimension
import java.awt.event.ComponentAdapter
import java.awt.event.ComponentEvent
import javax.swing.ButtonGroup
import javax.swing.JToggleButton
import javax.swing.SwingUtilities


class MainView {
    // TODO: better manage view updates and triggers
    // TODO: coroutines to get content of the current dir
    private val topBarView = TopBarView(this)
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
        val frame = JFrame("DirExplorer")
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
