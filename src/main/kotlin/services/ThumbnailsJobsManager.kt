package services

import views.iconviews.FileIconView

/**
 * Object keeps track of all present file icon views.
 * Has a function to cancel all thumbnail generation coroutines.
 */
object ThumbnailsJobsManager {
    private val fileIconViews = mutableListOf<FileIconView>()

    fun addFileIconView(view: FileIconView) {
        fileIconViews.add(view)
    }

    fun cancelThumbnailGenerationTasks() {
        fileIconViews.forEach { it.dispose() }
        fileIconViews.clear()
    }
}
