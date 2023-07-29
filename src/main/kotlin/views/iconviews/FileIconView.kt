package views.iconviews

import dataModels.ExplorerFile
import services.ThumbnailGenerationService
import utils.Utils
import utils.IconManager


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
