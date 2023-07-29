package views.directoryviews

import Constants
import dataModels.*
import kotlinx.coroutines.launch
import services.DirectoryContentService
import services.EntityIconViewFactory
import services.ThumbnailsJobsManager
import state.*
import views.customcomponents.WrapLayout
import java.awt.*
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.JPanel
import javax.swing.JScrollPane
import javax.swing.SwingUtilities


class GridDirectoryView : AbstractDirectoryView() {
    private val gridPanel = JPanel(WrapLayout(FlowLayout.LEFT, 10, 10))
    private var filteredAndSortedContents: List<FileSystemEntity> = emptyList()
    // keep track of all launched thumbnail generation jobs
    private val contentService = DirectoryContentService()

    init {
        setBackgroundColor()
        updateView()
        setupMouseListener()  // simply to deselect selected icon if clicked outside any icon
    }

    private fun setupMouseListener() {
        gridPanel.addMouseListener(object : MouseAdapter() {
            override fun mouseClicked(e: MouseEvent) {
                if (e.source == gridPanel) {
                    SelectedIconManager.deselect()
                }
            }
        })
    }

    /**
     * Update number of columns according to the view width
     */
    private fun updateLayout() {
        gridPanel.revalidate()
        gridPanel.repaint()
    }

    override fun updateView() {
        ThumbnailsJobsManager.cancelThumbnailGenerationTasks()

        launch {
            filteredAndSortedContents = contentService.generateContentForView()
            SwingUtilities.invokeLater {
                setBackgroundColor()
                clearAndRedrawGridPanel()
                revalidateAndRepaintScrollPane()
            }
        }
    }

    private fun clearAndRedrawGridPanel() {
        gridPanel.removeAll()
        updateLayout()

        for (entity in filteredAndSortedContents) {
            val entityIcon = EntityIconViewFactory.createEntityView(entity)
            entityIcon.isOpaque = false
            gridPanel.add(entityIcon)
        }

        gridPanel.revalidate()
        gridPanel.repaint()
    }

    /**
     * Had to add this because otherwise if a directory contains
     * too many items, vertical scroll might be not present
     * at rare occasion. ? to avoid NullPointerException
     */
    private fun revalidateAndRepaintScrollPane() {
        if (gridPanel.parent?.parent is JScrollPane) {
            (gridPanel.parent?.parent as JScrollPane?)?.apply {
                revalidate()
                repaint()
            }
        }
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

    override fun onLanguageChanged(newLanguage: Language) { }
}
