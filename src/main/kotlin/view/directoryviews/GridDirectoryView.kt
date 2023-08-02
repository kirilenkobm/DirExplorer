package view.directoryviews

import Constants
import model.*
import kotlinx.coroutines.launch
import service.DirectoryContentService
import service.EntityIconViewFactory
import service.ThumbnailJobController
import state.*
import view.customcomponents.CustomScrollPane
import view.customcomponents.WrapLayout
import java.awt.*
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.JPanel
import javax.swing.SwingUtilities

/**
 * Class to display the directory's content in the grid mode.
 */
class GridDirectoryView : AbstractDirectoryView() {
    private val gridPanel = JPanel(WrapLayout(FlowLayout.LEFT, 10, 10))
    private var filteredAndSortedContents: List<FileSystemEntity> = emptyList()
    private val contentService = DirectoryContentService()

    init {
        AppState.addDirectoryObserver(this)
        Settings.addObserver(this)
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

    private fun updateLayout() {
        gridPanel.removeAll()
        gridPanel.revalidate()
        gridPanel.repaint()
    }

    override fun updateView() {
        ThumbnailJobController.cancelThumbnailGenerationTasks()

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
        updateLayout()

        for (entity in filteredAndSortedContents) {
            val entityIcon = EntityIconViewFactory.createEntityView(entity)
            entityIcon.isOpaque = false
            gridPanel.add(entityIcon)
        }

        val spinner = EntityIconViewFactory.makeZipLoadingSpinner()
        spinner.isOpaque = false
        gridPanel.add(spinner)

        gridPanel.revalidate()
        gridPanel.repaint()
    }

    /**
     * Had to add this because otherwise if a directory contains
     * too many items, vertical scroll might be not present
     * at rare occasion. ? to avoid NullPointerException
     */
    private fun revalidateAndRepaintScrollPane() {
        if (gridPanel.parent?.parent is CustomScrollPane) {
            (gridPanel.parent?.parent as CustomScrollPane?)?.apply {
                revalidate()
                repaint()
                updatePreferredSize()
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
