package service

import kotlinx.coroutines.sync.Semaphore

/**
 * Singleton object that manages semaphores for controlling concurrent access to resources.
 *
 * This object is responsible for managing semaphores that limit the number of concurrent
 * operations for image previews, text previews, and unpacking zip archives.
 * Each semaphore is initialized with a maximum limit defined in the Constants class.
 *
 * imagePreviewsSemaphore: A semaphore that controls the number of concurrent image and pdf preview operations.
 * textPreviewsSemaphore: A semaphore that controls the number of concurrent text preview operations.
 * zipUnpackSemaphore: A semaphore that controls the number of concurrent zip unpacking operations.
 */
object SemaphoreManager {
    val imagePreviewsSemaphore = Semaphore(Constants.MAX_IMAGE_PREVIEWS)  // 2
    val textPreviewsSemaphore = Semaphore(Constants.MAX_TEXT_PREVIEWS)  // 10 - lightweight jobs
    val zipUnpackSemaphore = Semaphore(Constants.MAX_UNZIPPED_DIRS)  // 1
}
