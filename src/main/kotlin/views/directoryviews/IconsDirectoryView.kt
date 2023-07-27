package views.directoryviews

import Constants
import dataModels.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import state.*
import views.iconviews.*
import java.awt.*
import java.awt.event.ComponentAdapter
import java.awt.event.ComponentEvent
import javax.swing.JPanel
import javax.swing.SwingUtilities


// TODO: evaluate all constraints
class IconsDirectoryView : AbstractDirectoryView() {
    private val gridPanel = JPanel()
    private val panel = JPanel(BorderLayout())
    private var updateJob: Job? = null
    private var filteredAndSortedContents: List<FileSystemEntity> = emptyList()
    // Limit number of thumbnails rendered at once
    private val semaphorePermitsForImagePreviewsGeneration = 2
    private val imagePreviewsSemaphore = Semaphore(semaphorePermitsForImagePreviewsGeneration)
    private val semaphorePermitsForTextPreviewsGeneration = 10  // pretty lightweight
    private val textPreviewsSemaphore = Semaphore(semaphorePermitsForTextPreviewsGeneration)
    private val fileIconViews = mutableListOf<FileIconView>()  // keep track of all launched thumbnail generation jobs
    private var selectedView: AbstractIconEntityView? = null  // TODO: move down to IconView

    init {
        gridPanel.isOpaque = false
        panel.add(gridPanel, BorderLayout.NORTH)
        panel.addComponentListener(object : ComponentAdapter() {
            override fun componentResized(e: ComponentEvent?) {
                super.componentResized(e)
                // debouncing -> such that update view involving async
                // is executed only if needed, and prev jobs are cancelled
                updateJob?.cancel() // cancel the previous job if it's still running
                updateJob = launch {
                    delay(400) // wait for X milliseconds before updating the view
                    updateView()
                }
            }
        })
        updateView()
        // panel.isOpaque = false
        panel.background = if (Settings.colorTheme == ColorTheme.LIGHT) {
            Constants.BACKGROUND_COLOR_LIGHT
        } else {
            Constants.BACKGROUND_COLOR_DARK
        }
    }

    private fun createEntityView(entity: FileSystemEntity): JPanel {
        return when (entity) {
            is ExplorerFile -> {
                val view = FileIconView(
                    entity,
                    this,
                    imagePreviewsSemaphore,
                    textPreviewsSemaphore,
                    Settings.colorTheme
                )
                fileIconViews.add(view)
                view.createView()
            }
            is ExplorerDirectory -> DirectoryIconView(entity, this, Settings.colorTheme).createView()
            is ExplorerSymLink -> SymlinkIconView(entity, this, Settings.colorTheme).createView()
            is ZipArchive -> ZipArchiveIconView(entity, this, Settings.colorTheme).createView()  // Add this line
            else -> {
                // To handle type mismatch in else branch:
                // UnknownEntity handles all other possible cases
                val unknownEntity = UnknownEntity(entity.path)
                UnknownIconView(unknownEntity, this, Settings.colorTheme).createView()
            }
        }
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
            currentContents = AppState.currentExplorerDirectory.getContents()
            filteredAndSortedContents = filterAndSortContents(currentContents)

            SwingUtilities.invokeLater {
                gridPanel.removeAll()

                val columnWidth = 90
                var numberOfColumns = panel.width / columnWidth - 1
                if  (numberOfColumns <= 0) { numberOfColumns = 1}
                gridPanel.layout = GridLayout(0, numberOfColumns) // Set new layout with updated number of columns

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
