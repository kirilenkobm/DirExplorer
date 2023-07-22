package views.directoryviews

import dataModels.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import state.AppState
import views.*
import views.iconviews.*
import java.awt.GridLayout
import javax.swing.ImageIcon
import javax.swing.JPanel
import javax.swing.SwingUtilities

const val semaphorePermitsForThumbnailGeneration = 2


class IconsDirectoryView(private val topBarView: TopBarView) : AbstractDirectoryView(), DirectoryViewUpdater {
    private val panel = JPanel(GridLayout(0, 5)) // TODO: adaptive number of columns
    private var filteredAndSortedContents: List<FileSystemEntity> = emptyList()
    // If I render many thumbnails at once, I would like to avoid rendering many of them
    // at once (for example, if directory contains 1000s of them)
    private val thumbnailSemaphore = Semaphore(semaphorePermitsForThumbnailGeneration) // Limit to 10 concurrent tasks


    init {
        updateView()
    }

    override fun updateTobBarView() {
        topBarView.updateView()
    }

    private fun resizeIcon(icon: ImageIcon, size: Int): ImageIcon {
        val image = icon.image
        val newImage = image.getScaledInstance(size, size, java.awt.Image.SCALE_FAST)
        return ImageIcon(newImage)
    }

    private fun createEntityView(entity: FileSystemEntity): JPanel {
        return when (entity) {
            is ExplorerFile -> FileIconView(entity, thumbnailSemaphore).createView()
            is ExplorerDirectory -> DirectoryIconView(entity, this).createView()
            is ExplorerSymLink -> SymlinkIconView(entity).createView()
            is ZipArchive -> ZipArchiveIconView(entity).createView()  // Add this line
            else -> {
                // To handle type mismatch in else branch:
                // UnknownEntity handles all other possible cases

                val unknownEntity = UnknownEntity(entity.path)
                UnknownIconView(unknownEntity).createView()
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
                panel.removeAll()
                for (entity in filteredAndSortedContents) {
                    panel.add(createEntityView(entity))
                }
                panel.revalidate()
                panel.repaint()
            }
        }
    }

    fun getPanel(): JPanel {
        return panel
    }
}
