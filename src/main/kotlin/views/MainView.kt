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
import javax.swing.ButtonGroup
import javax.swing.JToggleButton
import javax.swing.SwingUtilities


class MainView {
    // TODO: better manage view updates and triggers
    // TODO: coroutines to get content of the current dir
    private val topBarView = TopBarView(this)
    // private val directoryView = DirectoryView(topBarView)
    private val tableView = TableDirectoryView(topBarView)

    fun updateView() {
        // directoryView.updateTableView()
        tableView.updateView()
        topBarView.updateView()
    }

    fun createAndShowGUI() {
        val frame = JFrame("DirExplorer")
        frame.defaultCloseOperation = JFrame.EXIT_ON_CLOSE

        // Main Panel
        val mainPanel = JPanel(BorderLayout())
        mainPanel.add(JScrollPane(tableView.getTable()), BorderLayout.CENTER)

        // Status bar

        // Compose views together
        frame.add(topBarView.getPanel(), BorderLayout.NORTH)
        frame.add(mainPanel, BorderLayout.CENTER)
        // frame.add(statusBar, BorderLayout.SOUTH)
        frame.pack()
        frame.isVisible = true
    }
}
