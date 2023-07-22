package views

import dataModels.*
import kotlinx.coroutines.launch
import state.AppState
import state.SortOrder
import java.awt.Desktop
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.io.File
import java.io.IOException
import java.util.*
import javax.swing.ImageIcon
import javax.swing.JTable
import javax.swing.SwingUtilities
import javax.swing.table.DefaultTableModel


class TableDirectoryView(private val topBarView: TopBarView) : AbstractDirectoryView() {
    private val table = JTable()
    private var model: DefaultTableModel? = null
    private val entityIconSize: Int = 16

    init {
        setupTableMouseListener()
        launch {
            currentContents = AppState.currentExplorerDirectory.getContents()
            model = createTableModel()
            table.model = model

            // Set the column width here
            table.columnModel.getColumn(0).preferredWidth = 20
            table.columnModel.getColumn(0).maxWidth = 20
        }
    }

    fun resizeIcon(icon: ImageIcon, size: Int): ImageIcon {
        val image = icon.image
        val newImage = image.getScaledInstance(size, size, java.awt.Image.SCALE_SMOOTH)
        return ImageIcon(newImage)
    }

    private fun mapEntitiesToData(): Array<Array<Any>> {
        val sortedContents = when (AppState.currentExplorerDirectory.sortOrder) {
            SortOrder.NAME -> currentContents.sortedBy { it.name }
            SortOrder.TYPE -> currentContents.sortedWith(
                compareBy<FileSystemEntity> {
                    when (it) {
                        is ExplorerDirectory -> 0
                        is ExplorerFile -> 1
                        is ExplorerSymLink -> 2
                        else -> 3
                    }
                }.thenBy { it.name }
            )
            SortOrder.DATE_CREATED -> currentContents // TODO: Implement sorting by date created
        }

        return sortedContents.filter { entity ->
            if (AppState.currentFilter.isNotEmpty()) {
                entity is ExplorerFile && entity.extension == AppState.currentFilter
            } else {
                true
            }
        }.map { entity ->
            when (entity) {
                is ExplorerFile -> arrayOf<Any>(
                    resizeIcon(IconManager.getIconForFileType(entity.fileType), entityIconSize),
                    entity.name,
                    humanReadableSize(entity.size),
                    dateFormat.format(Date(entity.lastModified))
                )
                is ExplorerDirectory -> arrayOf<Any>(
                    resizeIcon(IconManager.folderIcon, entityIconSize),
                    entity.name,
                    "-",
                    "-",
                )
                is ExplorerSymLink -> arrayOf<Any>(
                    resizeIcon(IconManager.linkIcon, entityIconSize),
                    entity.name,
                    "-",
                    "-"
                )
                is UnknownEntity -> arrayOf<Any>(
                    resizeIcon(IconManager.helpCenterIcon, entityIconSize),
                    "-",
                    "-",
                    "-"
                )
                else -> arrayOf<Any>(
                    resizeIcon(IconManager.helpCenterIcon, entityIconSize),
                    "-",
                    "-",
                    "-",
                )
            }
        }.toTypedArray()
    }


    private fun createTableModel(): DefaultTableModel {
        val columnNames = arrayOf("Type", "Name", "Size", "Last Modified")
        val data = mapEntitiesToData()
        return object : DefaultTableModel(data, columnNames) {
            override fun getColumnClass(column: Int): Class<*> {
                return if (column == 0) ImageIcon::class.java else super.getColumnClass(column)
            }
        }
    }

    override fun updateView() {
        launch {
            currentContents = AppState.currentExplorerDirectory.getContents()
            val data = mapEntitiesToData()

            SwingUtilities.invokeLater {
                model?.dataVector?.clear()
                for (row in data) {
                    model?.addRow(row)
                }
                model?.fireTableDataChanged()

                // Set the column width here
                table.columnModel.getColumn(0).preferredWidth = 20
                table.columnModel.getColumn(0).maxWidth = 20
            }
        }
        onCurrentDirectoryChanged()
    }

    override fun setupTableMouseListener() {
        table.addMouseListener(object : MouseAdapter() {
            override fun mouseClicked(e: MouseEvent) {
                val row = table.rowAtPoint(e.point)
                if (row >= 0 && row < currentContents.size) {
                    val entity = currentContents[row]
                    if (entity is ExplorerDirectory) {
                        AppState.updateDirectory(entity)
                        updateView()
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

    fun getTable(): JTable {
        return table
    }
}