package view.iconviews

import model.ExplorerFile
import service.ThumbnailGenerationService
import util.Utils
import util.IconManager

/**
 * Icon view for displaying a file icon in the DirExplorer in the grid view mode.
 *
 * Additionally, a thumbnail image for the file (if applicable) is generated using the ThumbnailGenerationService
 * and displayed as the icon. The thumbnail generation process can be started asynchronously.
 */
class FileIconView(
    entity: ExplorerFile,
): AbstractIconEntityView(entity) {
    private val fileEntity = entity
    private val thumbnailGenerationService = ThumbnailGenerationService(fileEntity, this)

    override fun setIcon() {
        // default base image
        iconLabel.icon = Utils.resizeIcon(IconManager.getIconForFileType(fileEntity.fileType))
        // TODO: choose one of them
        thumbnailGenerationService.startThumbnailGeneration()
        // Start thumbnail generation without waiting for it to finish

//        val thumbnailDeferred = fileEntity.startThumbnailGeneration()
//
//        // Launch a new coroutine to update the UI as soon as the thumbnail is ready
//        launch {
//            val thumbnail = thumbnailDeferred.await()
//
//            // If the thumbnail is not null, set it
//            if (thumbnail != null) {
//                iconLabel.icon = thumbnail
//            }
//        }
    }

    fun dispose() {
        thumbnailGenerationService.dispose()
    }
}
