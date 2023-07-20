package views
import dataModels.ExplorerFile
import dataModels.ExplorerDirectory
import state.AppState
import javax.swing.JFrame
import javax.swing.JScrollPane
import javax.swing.JTable
import javax.swing.table.DefaultTableModel

class MainView {
    fun createAndShowGUI() {
        val frame = JFrame("Explorer")
        frame.defaultCloseOperation = JFrame.EXIT_ON_CLOSE

        val columnNames = arrayOf("Type", "Name", "Size", "Last Modified")
        val data = AppState.currentExplorerDirectory.getContents().map {entity ->
            when (entity) {
                is ExplorerFile -> arrayOf<Any>("File", entity.name, "${entity.size} bytes", entity.lastModified)
                is ExplorerDirectory -> arrayOf<Any>("Directory", entity.name, "-", "-")
                else -> arrayOf<Any>("Unknown", "-", "-", "-")
            }
        }.toTypedArray()

        val model = DefaultTableModel(data, columnNames)
        val table = JTable(model)

        frame.add(JScrollPane(table))

        frame.pack()
        frame.isVisible = true
    }
}