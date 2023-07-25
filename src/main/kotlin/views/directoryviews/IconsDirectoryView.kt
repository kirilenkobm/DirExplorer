package views.directoryviews

import dataModels.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import state.AppState
import views.*
import views.iconviews.*
import java.awt.*
import java.awt.event.ComponentAdapter
import java.awt.event.ComponentEvent
import javax.swing.JPanel
import javax.swing.SwingUtilities


// TODO: evaluate all constraints
class IconsDirectoryView(topBarView: TopBarView) : AbstractDirectoryView(topBarView) {
    private val gridPanel = JPanel()
    private val panel = JPanel(BorderLayout())
    private var updateJob: Job? = null
    private var filteredAndSortedContents: List<FileSystemEntity> = emptyList()
    // Limit number of thumbnails rendered at once
    private val semaphorePermitsForThumbnailGeneration = 2
    private val thumbnailSemaphore = Semaphore(semaphorePermitsForThumbnailGeneration)
    private val fileIconViews = mutableListOf<FileIconView>()  // keep track of all launched thumbnail generation jobs

    init {
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
//        if (Settings.colorTheme == ColorTheme.DARK) {
//            panel.background = Color.DARK_GRAY
//        }
        updateView()
    }

    private fun createEntityView(entity: FileSystemEntity): JPanel {
        return when (entity) {
            is ExplorerFile -> {
                val view = FileIconView(entity, this, thumbnailSemaphore)
                fileIconViews.add(view)
                view.createView()
            }
            is ExplorerDirectory -> DirectoryIconView(entity, this).createView()
            is ExplorerSymLink -> SymlinkIconView(entity, this).createView()
            is ZipArchive -> ZipArchiveIconView(entity, this).createView()  // Add this line
            else -> {
                // To handle type mismatch in else branch:
                // UnknownEntity handles all other possible cases
                val unknownEntity = UnknownEntity(entity.path)
                UnknownIconView(unknownEntity, this).createView()
            }
        }
    }

    override fun updateView() {
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
                    gridPanel.add(createEntityView(entity))
                }
                gridPanel.revalidate()
                gridPanel.repaint()
            }
        }
        // Cancel all ongoing thumbnail generation tasks
        for (view in fileIconViews) {
            view.dispose()
        }
        fileIconViews.clear()
        onCurrentDirectoryChanged()
    }

    fun getPanel(): JPanel {
        return panel
    }
}
