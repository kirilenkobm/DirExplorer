package views.directoryviews

import Constants
import dataModels.*
import kotlinx.coroutines.launch
import services.DirectoryContentService
import state.*
import views.WrapLayout
import views.iconviews.*
import java.awt.*
import javax.swing.JPanel
import javax.swing.SwingUtilities


class GridDirectoryView : AbstractDirectoryView() {
    private val gridPanel = JPanel()
    private val panel = JPanel(BorderLayout())
    private var filteredAndSortedContents: List<FileSystemEntity> = emptyList()
    // Limit number of thumbnails rendered at once
    private val fileIconViews = mutableListOf<FileIconView>()  // keep track of all launched thumbnail generation jobs
    private var selectedView: AbstractIconEntityView? = null  // TODO: move down to IconView
    private val contentService = DirectoryContentService()

    // private val columnWidth = 90

    init {
        gridPanel.isOpaque = false
        panel.add(gridPanel, BorderLayout.NORTH)
        panel.background = if (Settings.colorTheme == ColorTheme.LIGHT) {
            Constants.BACKGROUND_COLOR_LIGHT
        } else {
            Constants.BACKGROUND_COLOR_DARK
        }
        updateView()
    }

    private fun createEntityView(entity: FileSystemEntity): JPanel {
        return when (entity) {
            is ExplorerFile -> {
                val view = FileIconView(
                    entity,
                    this,
                    Settings.colorTheme
                )
                fileIconViews.add(view)
                view.createView()
            }
            is ExplorerDirectory -> DirectoryIconView(entity, this, Settings.colorTheme).createView()
            is ExplorerSymLink -> SymlinkIconView(entity, this, Settings.colorTheme).createView()
            is ZipArchive -> ZipArchiveIconView(entity, this, Settings.colorTheme).createView()
            else -> {
                // To handle type mismatch in else branch:
                // UnknownEntity handles all other possible cases
                val unknownEntity = UnknownEntity(entity.path)
                UnknownIconView(unknownEntity, this, Settings.colorTheme).createView()
            }
        }
    }

    /**
     * Update number of columns according to the view width
     */
    private fun updateLayout() {
//         var numberOfColumns = panel.width / columnWidth - 1
//         if  (numberOfColumns <= 0) { numberOfColumns = 1}
        // gridPanel.layout = GridLayout(0, numberOfColumns) // Set new layout with updated number of columns
        gridPanel.layout = WrapLayout(FlowLayout.LEFT, 10, 10)
        // gridPanel.layout = GridBagLayout()
        gridPanel.revalidate()
        gridPanel.repaint()
    }

    override fun updateView() {
        // Cancel all ongoing thumbnail generation tasks
        for (view in fileIconViews) {
            view.dispose()
        }
        fileIconViews.clear()
        panel.background = if (Settings.colorTheme == ColorTheme.LIGHT) {
            Constants.BACKGROUND_COLOR_LIGHT
        } else {
            Constants.BACKGROUND_COLOR_DARK
        }

        launch {
            filteredAndSortedContents = contentService.generateContentForView()
            SwingUtilities.invokeLater {
                gridPanel.removeAll()
                updateLayout()

                for (entity in filteredAndSortedContents) {
                    val entityIcon = createEntityView(entity)
                    entityIcon.isOpaque = false
                    gridPanel.add(entityIcon)
                }

                gridPanel.revalidate()
                gridPanel.repaint()
            }
        }
    }

    fun setSelectedIcon(iconView: AbstractIconEntityView) {
        selectedView?.setSelected(false)  // undo selection if any
        iconView.setSelected(true)
        selectedView = iconView
    }

    fun getPanel(): JPanel {
        return panel
    }

    override fun onViewModeChanged(newViewMode: ViewMode) {
        updateView()
    }
}
