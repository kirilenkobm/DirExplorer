package views.iconviews

import dataModels.ExplorerFile
import kotlinx.coroutines.*
import state.ColorTheme
import views.IconManager
import views.directoryviews.IconsDirectoryView
import javax.swing.SwingUtilities
import kotlin.coroutines.CoroutineContext


class FileIconView(
    entity: ExplorerFile,
    parentDirView: IconsDirectoryView,
    colorTheme: ColorTheme
): AbstractIconEntityView(entity, parentDirView, colorTheme), CoroutineScope {
    private val fileEntity = entity
    private val job = Job()

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    override fun setIcon() {
        // default base image
        iconLabel.icon = resizeIcon(IconManager.getIconForFileType(fileEntity.fileType))

        // Start thumbnail generation without waiting for it to finish
        val thumbnailDeferred = fileEntity.startThumbnailGeneration()

        // Launch a new coroutine to update the UI as soon as the thumbnail is ready
        launch {
            val thumbnail = thumbnailDeferred.await()

            // If the thumbnail is not null, set it
            if (thumbnail != null) {
                SwingUtilities.invokeLater {
                    iconLabel.icon = thumbnail
                }
            }
        }
    }

    fun dispose() {
        job.cancel()
        fileEntity.dispose()
    }
}
