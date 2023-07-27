package views.directoryviews

import Constants
import dataModels.*
import kotlinx.coroutines.launch
import state.AppState
import state.ColorTheme
import state.Settings
import state.ViewMode
import views.IconManager
import utils.Utils
import java.awt.Color
import java.awt.Component
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.util.*
import javax.swing.ImageIcon
import javax.swing.JTable
import javax.swing.SwingUtilities
import javax.swing.table.DefaultTableModel
import javax.swing.table.TableCellRenderer


/**
 * View that controls directory view in the table mode
 */
class TableDirectoryView : AbstractDirectoryView() {
    private var table = JTable()
    private var model: DefaultTableModel? = null
    private val entityIconSize: Int = 16
    private var filteredAndSortedContents: List<FileSystemEntity> = emptyList()
    private val bundle = ResourceBundle.getBundle("languages/Messages", Settings.language.getLocale())
    private val firstColumnWidth = 20

    init {
        // Override prepareRenderer to make even and odd rows colored differently
        table = object : JTable() {
            override fun prepareRenderer(renderer: TableCellRenderer, row: Int, column: Int): Component {
                val component = super.prepareRenderer(renderer, row, column)
                if (Settings.colorTheme == ColorTheme.LIGHT) {
                    val color = if (row % 2 == 0) Color(237, 237, 237) else Color.WHITE // Change colors as needed for light theme
                    component.background = color
                } else {
                    val color = if (row % 2 == 0) Color(50, 50, 50) else Color(30, 30, 30) // Change colors as needed for dark theme
                    component.background = color
                }
                return component
            }
        }

        setupTableMouseListener()

        launch {
            currentContents = AppState.currentExplorerDirectory.getContents()
            filteredAndSortedContents = filterAndSortContents(currentContents)
            model = createTableModel()
            table.model = model

            // Set the column width here
            table.columnModel.getColumn(0).preferredWidth = 20
            table.columnModel.getColumn(0).maxWidth = 20
            if (Settings.colorTheme == ColorTheme.LIGHT) {
                table.background = Constants.BACKGROUND_COLOR_LIGHT
                table.foreground = Color.DARK_GRAY
                table.tableHeader.background = Constants.DEFAULT_SWING_BACKGROUND_COLOR
                table.tableHeader.foreground = Color.BLACK

            } else {
                table.background = Constants.BACKGROUND_COLOR_DARK
                table.foreground = Color.LIGHT_GRAY
                table.tableHeader.background = Color.DARK_GRAY
                table.tableHeader.foreground = Color.WHITE
            }
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
                    Utils.humanReadableSize(entity.size),
                    Utils.formatDate(entity.lastModified)
                )
                is ExplorerDirectory -> arrayOf<Any>(
                    resizeTableIcon(IconManager.getIconForDir(entity)),
                    entity.name,
                    "-",
                    Utils.formatDate(entity.lastModified)
                )
                is ExplorerSymLink -> arrayOf<Any>(
                    resizeTableIcon(IconManager.linkIcon),
                    entity.name,
                    "-",
                    Utils.formatDate(entity.lastModified)
                )
                is ZipArchive -> arrayOf<Any>(
                    resizeTableIcon(IconManager.folderZipIcon),
                    entity.name,
                    Utils.humanReadableSize(entity.size),
                    Utils.formatDate(entity.lastModified)
                )
                is UnknownEntity -> arrayOf<Any>(
                    resizeTableIcon(IconManager.helpCenterIcon),
                    entity.name,
                    Utils.humanReadableSize(entity.size),
                    Utils.formatDate(entity.lastModified)
                )
                else -> arrayOf<Any>(
                    // should be not reachable?
                    resizeTableIcon(IconManager.helpCenterIcon),
                    entity.name,
                    "-",
                    Utils.formatDate(entity.lastModified)
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

            override fun isCellEditable(row: Int, column: Int): Boolean {
                return false
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
                table.columnModel.getColumn(0).preferredWidth = firstColumnWidth
                table.columnModel.getColumn(0).maxWidth = firstColumnWidth

                // TODO: create a separate function
                if (Settings.colorTheme == ColorTheme.LIGHT) {
                    table.background = Constants.BACKGROUND_COLOR_LIGHT
                    table.foreground = Color.DARK_GRAY
                    table.tableHeader.background = Constants.DEFAULT_SWING_BACKGROUND_COLOR
                    table.tableHeader.foreground = Color.BLACK

                } else {
                    table.background = Constants.BACKGROUND_COLOR_DARK
                    table.foreground = Color.LIGHT_GRAY
                    table.tableHeader.background = Color.DARK_GRAY
                    table.tableHeader.foreground = Color.WHITE
                }
            }
        }

        table.revalidate()
        table.repaint()
    }

    private fun setupTableMouseListener() {
        table.addMouseListener(object : MouseAdapter() {
            override fun mouseClicked(e: MouseEvent) {
                if (e.clickCount == 2) {
                    val row = table.rowAtPoint(e.point)
                    if (row >= 0 && row < filteredAndSortedContents.size) {
                        val entity = filteredAndSortedContents[row]
                        performEntityAction(entity)
                    }
                }
            }
        })
    }

    fun getTable(): JTable {
        return table
    }

    override fun onViewModeChanged(newViewMode: ViewMode) {
        // TODO: implement
    }
}
