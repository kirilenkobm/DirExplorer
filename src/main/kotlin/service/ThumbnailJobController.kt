package service

import view.iconviews.FileIconView

/**
 * Singleton object that manages FileIconView instances and their associated thumbnail generation tasks.
 *
 * This manager keeps track of all active FileIconView instances and provides functionality
 * to cancel all ongoing thumbnail generation tasks. This is particularly useful if user lefts
 * a directory where a large number of thumbnails are being generated.
 */
object ThumbnailJobController {
    private val fileIconViews = mutableListOf<FileIconView>()

    fun addFileIconView(view: FileIconView) {
        fileIconViews.add(view)
    }

    fun cancelThumbnailGenerationTasks() {
        fileIconViews.forEach { it.dispose() }
        fileIconViews.clear()
    }
}
