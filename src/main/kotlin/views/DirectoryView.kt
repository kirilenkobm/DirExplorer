package views

import dataModels.ExplorerDirectory
import dataModels.ExplorerFile
import dataModels.FileSystemEntity
import state.AppState
import state.SortOrder
import java.awt.Desktop
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import javax.swing.JTable
import javax.swing.SwingUtilities
import javax.swing.table.DefaultTableModel

// TODO: add icon and columns view types, right now only the primitive table is done
class DirectoryView(private val topBarView: TopBarView) {
    private val table = JTable()
    private var model: DefaultTableModel
    private var currentContents: List<FileSystemEntity>

    val dateFormat = SimpleDateFormat("yyyy.MM.dd 'at' HH:mm:ss")

    fun humanReadableSize(bytes: Long): String {
        val kilobyte = 1024.0
        val megabyte = kilobyte * 1024
        val gigabyte = megabyte * 1024
        val terabyte = gigabyte * 1024

        return when {
            bytes < kilobyte -> "$bytes B"
            bytes < megabyte -> String.format("%.1f KB", bytes / kilobyte)
            bytes < gigabyte -> String.format("%.1f MB", bytes / megabyte)
            bytes < terabyte -> String.format("%.1f GB", bytes / gigabyte)
            else -> String.format("%.1f TB", bytes / terabyte)
        }
    }

    init {
        currentContents = AppState.currentExplorerDirectory.contents
        model = createTableModel()
        table.model = model
        setupTableMouseListener()
    }

    private fun createTableModel(): DefaultTableModel {
        val columnNames = arrayOf("Type", "Name", "Size", "Last Modified")

        val data = currentContents.map { entity ->
            when (entity) {
                is ExplorerFile -> arrayOf<Any>(
                    "File",
                    entity.name,
                    humanReadableSize(entity.size),
                    dateFormat.format(Date(entity.lastModified))
                )
                is ExplorerDirectory -> arrayOf<Any>("Directory", entity.name, "-", "-")
                else -> arrayOf<Any>("Unknown", "-", "-", "-")
            }
        }.toTypedArray()

        return DefaultTableModel(data, columnNames)
    }

    private fun setupTableMouseListener() {
        table.addMouseListener(object : MouseAdapter() {
            override fun mouseClicked(e: MouseEvent) {
                val row = table.rowAtPoint(e.point)
                if (row >= 0 && row < currentContents.size) {
                    val entity = currentContents[row]
                    if (entity is ExplorerDirectory) {
                        AppState.updateDirectory(entity)
                        updateTableView()
                        topBarView.updateView()
                    } else if (entity is ExplorerFile) {
                        if (Desktop.isDesktopSupported()) {
                            try {
                                Desktop.getDesktop().open(File(entity.path))
                            } catch (ex: IOException) {
                                ex.printStackTrace()
                                println("Error! Cannot open ....")
                                // TODO: show error message
                            }
                        }
                    }
                }  // outside the table >>>>
            }
        })
    }

    fun updateTableView() {
        currentContents = AppState.currentExplorerDirectory.contents

        val data = currentContents.map { entity ->
            when (entity) {
                is ExplorerFile -> arrayOf<Any>(
                    "file",
                    entity.name,
                    humanReadableSize(entity.size),
                    dateFormat.format(Date(entity.lastModified))
                )
                is ExplorerDirectory -> arrayOf<Any>("dir", entity.name, "-", "-")
                else -> arrayOf<Any>("Unknown", "-", "-", "-")
            }
        }.toTypedArray()

        SwingUtilities.invokeLater {
            model.dataVector.clear()
            for (row in data) {
                model.addRow(row)
            }
            model.fireTableDataChanged()
        }
    }

    fun getTable(): JTable {
        return table
    }
}