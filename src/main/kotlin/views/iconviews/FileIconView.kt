package views.iconviews

import dataModels.ExplorerFile
import kotlinx.coroutines.*
import services.ThumbnailService
import state.ColorTheme
import utils.Utils
import views.IconManager
import views.directoryviews.GridDirectoryView
import kotlin.coroutines.CoroutineContext


class FileIconView(
    entity: ExplorerFile,
    parentDirView: GridDirectoryView,
    colorTheme: ColorTheme
): AbstractIconEntityView(entity, parentDirView, colorTheme) {
    private val fileEntity = entity
    private val thumbnailService = ThumbnailService(fileEntity, this)

    override fun setIcon() {
        // default base image
        iconLabel.icon = Utils.resizeIcon(IconManager.getIconForFileType(fileEntity.fileType))
        // TODO: choose one of them
        thumbnailService.startThumbnailGeneration()
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
        thumbnailService.dispose()
    }
}
