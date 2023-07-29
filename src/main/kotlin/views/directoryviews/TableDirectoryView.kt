package views.directoryviews

import Constants
import dataModels.*
import kotlinx.coroutines.launch
import services.DirectoryContentService
import services.EntityActionsHandler
import services.TableDirectoryController
import state.ColorTheme
import state.Settings
import state.ViewMode
import utils.IconManager
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

// in views/directoryviews/TableDirectoryView.kt
class TableDirectoryView : AbstractDirectoryView() {
    private var table = JTable()
    private var model: DefaultTableModel? = null
    private var filteredAndSortedContents: List<FileSystemEntity> = emptyList()
    private val contentService = DirectoryContentService()
    private val controller = TableDirectoryController(contentService)

    // localization related
    private val bundle = ResourceBundle.getBundle(Constants.LANGUAGE_BUNDLE_PATH, Settings.language.getLocale())
    private val nameHeader = bundle.getString("Name")
    private val sizeHeader = bundle.getString("Size")
    private val lastModifiedHeader = bundle.getString("LastModified")

    init {
        // Override prepareRenderer to make even and odd rows colored differently
        table = object : JTable() {
            override fun prepareRenderer(renderer: TableCellRenderer, row: Int, column: Int): Component {
                val component = super.prepareRenderer(renderer, row, column)
                component.background = getRowColor(row)
                return component
            }
        }
        table.showHorizontalLines = false
        table.showVerticalLines = false
        table.border = null
        table.addMouseListener(object : MouseAdapter() {
            override fun mouseClicked(e: MouseEvent) {
                if (e.clickCount == 2) {
                    val row = table.rowAtPoint(e.point)
                    if (row >= 0 && row < filteredAndSortedContents.size) {
                        val entity = filteredAndSortedContents[row]
                        println("Calling entity action for: ${entity.path}")
                        EntityActionsHandler.performEntityAction(entity)
                    }
                }
            }
        })

        launch {
            val (entities, data) = controller.getContentForView()
            filteredAndSortedContents = entities
            model = createTableModel(data)
            table.model = model

            // Set the column width here
            table.columnModel.getColumn(0).preferredWidth = Constants.TABLE_VIEW_FIRST_COL_SIZE
            table.columnModel.getColumn(0).maxWidth = Constants.TABLE_VIEW_FIRST_COL_SIZE
            setTableColors()
        }
    }

    private fun getRowColor(row: Int) = when {
        table.isRowSelected(row) -> Constants.SELECTION_COLOR
        Settings.colorTheme == ColorTheme.LIGHT -> getLightThemeRowColor(row)
        else -> getDarkThemeRowColor(row)
    }

    private fun getLightThemeRowColor(row: Int) =
        if (row % 2 == 0) Constants.TABLE_EVEN_ROW_LIGHT_THEME_COLOR else Color.WHITE

    private fun getDarkThemeRowColor(row: Int) =
        if (row % 2 == 0) Constants.TABLE_EVEN_ROW_DARK_THEME_COLOR else Constants.TABLE_ODD_ROW_DARK_THEME_COLOR

    private fun setTableColors() {
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

    private fun createTableModel(data: List<Array<Any>>): DefaultTableModel {
        val columnNames = arrayOf(
            "",
            bundle.getString("Name"),
            bundle.getString("Size"),
            bundle.getString("LastModified")
        )
        return object : DefaultTableModel(data.toTypedArray(), columnNames) {
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
            val (entities, data) = controller.getContentForView()
            filteredAndSortedContents = entities

            SwingUtilities.invokeLater {
                model?.dataVector?.clear()
                for (row in data) {
                    model?.addRow(row)
                }
                model?.fireTableDataChanged()

                // Set the column width here
                table.columnModel.getColumn(0).preferredWidth = Constants.TABLE_VIEW_FIRST_COL_SIZE
                table.columnModel.getColumn(0).maxWidth = Constants.TABLE_VIEW_FIRST_COL_SIZE

                setTableColors()
            }
        }

        table.revalidate()
        table.repaint()
    }


    fun getTable(): JTable {
        return table
    }

    // Not relevant
    override fun onViewModeChanged(newViewMode: ViewMode) { }
}


