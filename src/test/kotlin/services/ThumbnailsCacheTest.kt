package services

import Constants
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import utils.IconManager
import java.util.concurrent.Callable
import java.util.concurrent.ExecutionException
import java.util.concurrent.Executors

class ThumbnailsCacheTest {

    @Test
    fun getAndSetTest() {
        ThumbnailsCache.invalidate()
        val expectedIcon1 = IconManager.folderOpenIcon
        val expectedIcon2 = IconManager.pdfIcon
        val testKey1 = "/some/test/key"
        val testKey2 = "/another/test/key"
        ThumbnailsCache[testKey1] = expectedIcon1
        ThumbnailsCache[testKey2] = expectedIcon2
        // Act
        val result1 = ThumbnailsCache[testKey1]
        val result2 = ThumbnailsCache[testKey2]
        val noResult = ThumbnailsCache["nothing"]

        // Assert
        assertNotNull(result1)
        assertNotNull(result2)
        assertNull(noResult)
        assertEquals(expectedIcon1, result1)
        assertEquals(expectedIcon2, result2)
    }

    @Test
    fun concurrentAccessConsistency() {
        // Test whether the cache is indeed thread safe?
        // Write using 100 concurrent threads
        ThumbnailsCache.invalidate()
        val numberOfThreads = 100
        val executorService = Executors.newFixedThreadPool(numberOfThreads)
        val tasks: MutableList<Callable<Unit>> = mutableListOf()
        val path = "testPath"

        for (i in 0..<numberOfThreads) {
            tasks.add(
                Callable<Unit> {
                    ThumbnailsCache[path + i] = IconManager.ellipsisIcon
                    ThumbnailsCache[path + i]
                }
            )
        }
        // Time to execute all of them
        val futures = executorService.invokeAll(tasks)

        // Assertion: check whether any exception occurred
        futures.forEach { future ->
            try {
                future.get()
            } catch (e: ExecutionException) {
                fail("Exception thrown during task execution: ${e.cause}")
            }
        }

        assertEquals(numberOfThreads, ThumbnailsCache.size())
    }

    @Test
    fun cacheExceedsSizeLimitMaintained() {
        // Arrange
        ThumbnailsCache.invalidate()
        val extraVolume = 1000  // let's save 11000 elements to cache
        val pathsAndIcons =
            (0..<Constants.THUMBNAIL_CACHE_SIZE + extraVolume).map { Pair("path_num_$it", IconManager.ellipsisIcon) }

        // Add max + extra elements
        pathsAndIcons.forEach { (path, icon) ->
            ThumbnailsCache[path] = icon
        }

        // Size must stay the same
        assertEquals(Constants.THUMBNAIL_CACHE_SIZE, ThumbnailsCache.size())
    }
}
