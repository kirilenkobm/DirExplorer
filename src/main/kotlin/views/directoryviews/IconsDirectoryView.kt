package views.directoryviews

import dataModels.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import state.AppState
import state.ColorTheme
import state.Settings
import views.*
import views.iconviews.*
import java.awt.*
import java.awt.event.ComponentAdapter
import java.awt.event.ComponentEvent
import javax.swing.ImageIcon
import javax.swing.JPanel
import javax.swing.SwingUtilities


// TODO: evaluate all constraints
class IconsDirectoryView(topBarView: TopBarView) : AbstractDirectoryView(topBarView) {
    private val gridPanel = JPanel()
    private val panel = JPanel(BorderLayout())
    private var updateJob: Job? = null
    private var filteredAndSortedContents: List<FileSystemEntity> = emptyList()
    // If I render many thumbnails at once, I would like to avoid rendering many of them
    // at once (for example, if directory contains 1000s of them)
    private val semaphorePermitsForThumbnailGeneration = 2
    // Limit to N concurrent tasks
    private val thumbnailSemaphore = Semaphore(semaphorePermitsForThumbnailGeneration)

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

    private fun resizeIcon(icon: ImageIcon, size: Int): ImageIcon {
        val image = icon.image
        val newImage = image.getScaledInstance(size, size, java.awt.Image.SCALE_FAST)
        return ImageIcon(newImage)
    }

    private fun createEntityView(entity: FileSystemEntity): JPanel {
        return when (entity) {
            is ExplorerFile -> FileIconView(entity, this, thumbnailSemaphore).createView()
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

            // Wait for all thumbnail generation tasks to complete
            while (thumbnailSemaphore.availablePermits < semaphorePermitsForThumbnailGeneration) {
                delay(100) // Wait for a short time before checking again
            }

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
//                if (Settings.colorTheme == ColorTheme.DARK) {
//                    gridPanel.background = Color.DARK_GRAY
//                }
            }
        }
        onCurrentDirectoryChanged()
    }

    fun getPanel(): JPanel {
        return panel
    }
}
