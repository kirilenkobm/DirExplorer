package service

import kotlinx.coroutines.sync.Semaphore

/**
 * Dedicated singleton to store semaphores and manage access to them
 */
object SemaphoreManager {
    val imagePreviewsSemaphore = Semaphore(Constants.MAX_IMAGE_PREVIEWS)
    val textPreviewsSemaphore = Semaphore(Constants.MAX_TEXT_PREVIEWS)
    val zipUnpackSemaphore = Semaphore(Constants.MAX_UNZIPPED_DIRS)
}
