package views.directoryviews

import dataModels.*
import kotlinx.coroutines.launch
import state.AppState
import state.Settings
import views.IconManager
import views.TopBarView
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.util.*
import javax.swing.ImageIcon
import javax.swing.JTable
import javax.swing.SwingUtilities
import javax.swing.table.DefaultTableModel


// View that controls directory view in the table mode
class TableDirectoryView(topBarView: TopBarView) : AbstractDirectoryView(topBarView) {
    private val table = JTable()
    private var model: DefaultTableModel? = null
    private val entityIconSize: Int = 16
    private var filteredAndSortedContents: List<FileSystemEntity> = emptyList()
    private val bundle = ResourceBundle.getBundle("languages/Messages", Settings.language.getLocale())

    init {
        setupTableMouseListener()
        launch {
            currentContents = AppState.currentExplorerDirectory.getContents()
            filteredAndSortedContents = filterAndSortContents(currentContents)
            model = createTableModel()
            table.model = model

            // Set the column width here
            table.columnModel.getColumn(0).preferredWidth = 20
            table.columnModel.getColumn(0).maxWidth = 20
        }
    }

    private fun resizeTableIcon(icon: ImageIcon, size: Int = entityIconSize): ImageIcon {
        val image = icon.image
        val newImage = image.getScaledInstance(size, size, java.awt.Image.SCALE_SMOOTH)
        return ImageIcon(newImage)
    }

    private fun mapEntitiesToData(): Array<Array<Any>> {
        return filteredAndSortedContents.map { entity ->
            when (entity) {
                is ExplorerFile -> arrayOf<Any>(
                    resizeTableIcon(IconManager.getIconForFileType(entity.fileType)),
                    entity.name,
                    humanReadableSize(entity.size),
                    dateFormat.format(Date(entity.lastModified))
                )
                is ExplorerDirectory -> arrayOf<Any>(
                    resizeTableIcon(IconManager.getIconForDir(entity)),
                    entity.name,
                    "-",
                    dateFormat.format(Date(entity.lastModified))
                )
                is ExplorerSymLink -> arrayOf<Any>(
                    resizeTableIcon(IconManager.linkIcon),
                    entity.name,
                    "-",
                    dateFormat.format(Date(entity.lastModified))
                )
                is ZipArchive -> arrayOf<Any>(
                    resizeTableIcon(IconManager.folderZipIcon),
                    entity.name,
                    humanReadableSize(entity.size),
                    dateFormat.format(Date(entity.lastModified))
                )
                is UnknownEntity -> arrayOf<Any>(
                    resizeTableIcon(IconManager.helpCenterIcon),
                    entity.name,
                    humanReadableSize(entity.size),
                    dateFormat.format(Date(entity.lastModified))
                )
                else -> arrayOf<Any>(
                    // should be not reachable?
                    resizeTableIcon(IconManager.helpCenterIcon),
                    entity.name,
                    "-",
                    dateFormat.format(Date(entity.lastModified))
                )
            }
        }.toTypedArray()
    }

    private fun createTableModel(): DefaultTableModel {
        val columnNames = arrayOf(
            "",
            bundle.getString("Name"),
            bundle.getString("Size"),
            bundle.getString("LastModified")
        )
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
            filteredAndSortedContents = filterAndSortContents(currentContents)
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

    private fun setupTableMouseListener() {
        table.addMouseListener(object : MouseAdapter() {
            override fun mouseClicked(e: MouseEvent) {
                val row = table.rowAtPoint(e.point)
                if (row >= 0 && row < filteredAndSortedContents.size) {
                    val entity = filteredAndSortedContents[row]
                    performEntityAction(entity)
                }
            }
        })
    }

    fun getTable(): JTable {
        return table
    }
}