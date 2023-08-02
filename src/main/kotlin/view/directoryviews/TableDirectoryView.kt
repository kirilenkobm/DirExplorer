package view.directoryviews

import Constants
import model.*
import kotlinx.coroutines.launch
import service.DirectoryContentService
import service.EntityActionsHandler
import service.TableItemsMapper
import service.ZipExtractionStatus
import state.*
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
 * Class to display the directory's content in the grid mode.
 */
class TableDirectoryView : AbstractDirectoryView() {
    private var table = JTable()
    private var model: DefaultTableModel? = null
    private var filteredAndSortedContents: List<FileSystemEntity> = emptyList()
    private val contentService = DirectoryContentService()
    private val controller = TableItemsMapper(contentService)

    // localization related
    private var bundle = ResourceBundle.getBundle(Constants.LANGUAGE_BUNDLE_PATH, Settings.language.getLocale())

    init {
        AppState.addDirectoryObserver(this)
        Settings.addObserver(this)
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
        table.tableHeader.addMouseListener(object : MouseAdapter() {
            override fun mouseClicked(e: MouseEvent) {
                when (table.columnAtPoint(e.point)) {
                    0 -> contentService.updateSortOrder(SortOrder.TYPE)
                    1 -> contentService.updateSortOrder(SortOrder.NAME)
                    2 -> contentService.updateSortOrder(SortOrder.SIZE)
                    3 -> contentService.updateSortOrder(SortOrder.LAST_MODIFIED)
                }
                updateView()
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

    private fun getColumnNames(): Array<String> {
        return arrayOf(
            "",
            bundle.getString("Name"),
            bundle.getString("Size"),
            bundle.getString("LastModified")
        )
    }

    private fun createTableModel(data: List<Array<Any>>): DefaultTableModel {
        return object : DefaultTableModel(data.toTypedArray(), getColumnNames()) {
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

    override fun onLanguageChanged(newLanguage: Language) {
        SwingUtilities.invokeLater {
            // Update the resource bundle
            bundle = ResourceBundle.getBundle(Constants.LANGUAGE_BUNDLE_PATH, newLanguage.getLocale())

            // Update the headers
            table.columnModel.getColumn(1).headerValue = bundle.getString("Name")
            table.columnModel.getColumn(2).headerValue = bundle.getString("Size")
            table.columnModel.getColumn(3).headerValue = bundle.getString("LastModified")

            // This will force the JTableHeader to repaint and show the new column names
            table.tableHeader.repaint()

            // Update the view
            updateView()
        }
    }

    override fun onExtractionStatusChanged(newStatus: ZipExtractionStatus) {
        // println("Status changed to $newStatus")
    }
}
