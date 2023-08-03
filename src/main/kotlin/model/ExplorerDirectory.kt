package model

import java.nio.file.Files
import java.nio.file.Paths
import kotlin.streams.asSequence
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import state.Settings


/**
 * Data model for a Directory.
 * Implements functions that are applicable to directories only.
 * Such as get contents (and keeps cache of the contents)
 * and to check whether there is access to the directory
 * or whether it's empty or not.
 * Also, methods to calculate total size of the content and number of items.
 */
open class ExplorerDirectory(override val path: String): ExplorableEntity {
    private var contentsCache: List<FileSystemEntity>? = null
    private val mutex = Mutex()

    open suspend fun getContents(): List<FileSystemEntity> = withContext(Dispatchers.IO) {
        // Idea is that if many processes need to acquire the content of the directory,
        // one the very first process collects the contents, and saves the results to cache
        // and following processes get data from cache when the mutex is released
        mutex.withLock {
            try {
                contentsCache?.let {
                    // println("Using cached contents for directory $path")
                    return@withContext it
                }

                // println("Fetching new contents for directory $path")
                contentsCache = Files.list(Paths.get(path)).asSequence().mapNotNull { path ->
                    FileSystemEntityFactory.createEntity(path.toString())
                }.toList()

                contentsCache!!
            } catch (e: Exception) {
                println("Exception in getContents: ${e.message}")
                e.printStackTrace()
                emptyList()
            }
        }
    }

    private val hasAccess: Boolean
        get() = Files.isReadable(Paths.get(path))

    val isEmpty: Boolean
        // As I understand, the Files.list returns a Stream
        // findAny() will stop once it meets a single document
        // so, even if directory contains 10000s of files, it will not hurt the
        // performance to check whether it's present or not.
        get() = if (hasAccess) {
            Files.list(Paths.get(path)).use { it.findAny().isPresent.not() }
        } else {
            // If we don't have access to the directory, assume it's not empty
            false
        }

    suspend fun getItemsCount(): Int = withContext(Dispatchers.IO) {
        try {
            getContents().filter { !it.isHidden || Settings.showHiddenFiles }.size
        } catch (e: Exception) {
            println("Exception in getItemsCount: ${e.message}")
            e.printStackTrace()
            -1
        }
    }

    suspend fun getTotalSize(): Long = withContext(Dispatchers.IO) {
        try {
            getContents().filter { !it.isHidden || Settings.showHiddenFiles }.sumOf { it.size }
        } catch (e: Exception) {
            println("Exception in getTotalSize: ${e.message}")
            e.printStackTrace()
            -1L
        }
    }

    fun invalidateCache() {
        contentsCache = null
    }
}
