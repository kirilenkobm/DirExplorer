package views.directoryviews

import dataModels.*
import kotlinx.coroutines.launch
import state.AppState
import state.Settings
import state.SortOrder
import views.IconManager
import views.TopBarView
import views.showErrorDialog
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
import javax.swing.table.TableRowSorter


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

    fun resizeIcon(icon: ImageIcon, size: Int): ImageIcon {
        val image = icon.image
        val newImage = image.getScaledInstance(size, size, java.awt.Image.SCALE_SMOOTH)
        return ImageIcon(newImage)
    }

    private fun mapEntitiesToData(): Array<Array<Any>> {
        return filteredAndSortedContents.map { entity ->
            when (entity) {
                is ExplorerFile -> arrayOf<Any>(
                    resizeIcon(IconManager.getIconForFileType(entity.fileType), entityIconSize),
                    entity.name,
                    humanReadableSize(entity.size),
                    dateFormat.format(Date(entity.lastModified))
                )
                is ExplorerDirectory -> arrayOf<Any>(
                    resizeIcon(IconManager.getIconForDir(entity), entityIconSize),
                    entity.name,
                    "-",
                    dateFormat.format(Date(entity.lastModified))
                )
                is ExplorerSymLink -> arrayOf<Any>(
                    resizeIcon(IconManager.linkIcon, entityIconSize),
                    entity.name,
                    "-",
                    dateFormat.format(Date(entity.lastModified))
                )
                is ZipArchive -> arrayOf<Any>(
                    resizeIcon(IconManager.folderZipIcon, entityIconSize),
                    entity.name,
                    humanReadableSize(entity.size),
                    dateFormat.format(Date(entity.lastModified))
                )
                is UnknownEntity -> arrayOf<Any>(
                    resizeIcon(IconManager.helpCenterIcon, entityIconSize),
                    entity.name,
                    humanReadableSize(entity.size),
                    dateFormat.format(Date(entity.lastModified))
                )
                else -> arrayOf<Any>(
                    // should be not reachable?
                    resizeIcon(IconManager.helpCenterIcon, entityIconSize),
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

//    private fun setupTableMouseListener() {
//        table.addMouseListener(object : MouseAdapter() {
//            override fun mouseClicked(e: MouseEvent) {
//                val row = table.rowAtPoint(e.point)
//                if (row >= 0 && row < filteredAndSortedContents.size) {
//                    val entity = filteredAndSortedContents[row]
//                    // TODO: create separate functions that are called
//                    // here and in icons?
//                    if (entity is ExplorerDirectory) {
//                        AppState.updateDirectory(entity)
//                        updateView()
//                        topBarView.updateView()
//                    } else if (entity is ExplorerFile) {
//                        if (Desktop.isDesktopSupported()) {
//                            try {
//                                Desktop.getDesktop().open(File(entity.path))
//                            } catch (ex: IOException) {
//                                ex.printStackTrace()
//                                showErrorDialog("Error: Unable to open the file at ${entity.path}.")
//                            }
//                        } else {
//                            // Desktop is not supported
//                            showErrorDialog("Error: Desktop operations are not supported on this system.")
//                        }
//                    } else if (entity is UnknownEntity) {
//                        showErrorDialog("Not supported file system entity")
//                    }
//                }  // outside the table >>>>
//            }
//        })
//    }

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