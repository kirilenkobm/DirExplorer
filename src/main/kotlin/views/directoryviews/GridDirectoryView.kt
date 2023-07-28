package views.directoryviews

import Constants
import dataModels.*
import kotlinx.coroutines.launch
import services.DirectoryContentService
import state.*
import views.customcomponents.WrapLayout
import views.iconviews.*
import java.awt.*
import javax.swing.JPanel
import javax.swing.JScrollPane
import javax.swing.SwingUtilities


class GridDirectoryView : AbstractDirectoryView() {
    private val gridPanel = JPanel(WrapLayout(FlowLayout.LEFT, 10, 10))
    private var filteredAndSortedContents: List<FileSystemEntity> = emptyList()
    // keep track of all launched thumbnail generation jobs
    private val fileIconViews = mutableListOf<FileIconView>()
    // see setSelectedIcon
    private var selectedView: AbstractIconEntityView? = null
    private val contentService = DirectoryContentService()

    init {
        setBackgroundColor()
        updateView()
    }

    private fun createEntityView(entity: FileSystemEntity): JPanel {
        return when (entity) {
            is ExplorerFile -> createFileIconView(entity)
            is ExplorerDirectory -> DirectoryIconView(entity, this).createView()
            is ExplorerSymLink -> SymlinkIconView(entity, this).createView()
            is ZipArchive -> ZipArchiveIconView(entity, this).createView()
            else -> createUnknownIconView(entity)
        }
    }

    private fun createFileIconView(entity: ExplorerFile): JPanel {
        val view = FileIconView(entity, this)
        fileIconViews.add(view)
        return view.createView()
    }

    private fun createUnknownIconView(entity: FileSystemEntity): JPanel {
        val unknownEntity = UnknownEntity(entity.path)
        return UnknownIconView(unknownEntity, this).createView()
    }

    /**
     * Update number of columns according to the view width
     */
    private fun updateLayout() {
        gridPanel.revalidate()
        gridPanel.repaint()
    }

    override fun updateView() {
        // Cancel all ongoing thumbnail generation tasks
        for (view in fileIconViews) {
            view.dispose()
        }
        fileIconViews.clear()

        launch {
            filteredAndSortedContents = contentService.generateContentForView()
            SwingUtilities.invokeLater {
                setBackgroundColor()
                gridPanel.removeAll()
                updateLayout()

                for (entity in filteredAndSortedContents) {
                    val entityIcon = createEntityView(entity)
                    entityIcon.isOpaque = false
                    gridPanel.add(entityIcon)
                }

                gridPanel.revalidate()
                gridPanel.repaint()
                // Had to add this bc otherwise if a directory contains
                // too many items, vertical scroll might be not present
                // at rare occasion. ? to avoid NullPointerException
                if (gridPanel.parent?.parent is JScrollPane) {
                    (gridPanel.parent?.parent as JScrollPane?)?.revalidate()
                    (gridPanel.parent?.parent as JScrollPane?)?.repaint()
                }

            }
        }
    }

    // TODO: refactor this, the only purpose of keeping it here
    // is to make sure that 0 or 1 icons are selected at the same time
    fun setSelectedIcon(iconView: AbstractIconEntityView) {
        selectedView?.setSelected(false)
        iconView.setSelected(true)
        selectedView = iconView
    }

    private fun setBackgroundColor() {
        gridPanel.background = if (Settings.colorTheme == ColorTheme.LIGHT) {
            Constants.BACKGROUND_COLOR_LIGHT
        } else {
            Constants.BACKGROUND_COLOR_DARK
        }
    }

    fun getPanel(): JPanel {
        return gridPanel
    }

    override fun onViewModeChanged(newViewMode: ViewMode) {
        updateView()
    }
}
