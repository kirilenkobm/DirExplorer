package services

import views.iconviews.FileIconView

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
