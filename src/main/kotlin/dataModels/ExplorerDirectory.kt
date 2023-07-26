// Class that manages directory class
package dataModels
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.streams.asSequence
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import state.SortOrder

// Open to allow inheritance in ZipArchive
open class ExplorerDirectory(override val path: String): ExplorableEntity {
    var sortOrder: SortOrder = SortOrder.TYPE
    private var contentsCache: List<FileSystemEntity>? = null
    val mutex = Mutex()

    open suspend fun getContents(): List<FileSystemEntity> = withContext(Dispatchers.IO) {
        mutex.withLock {
            try {
                contentsCache?.let {
                    println("Using cached contents for directory $path")
                    return@withContext it
                }

                println("Fetching new contents for directory $path")
                contentsCache = Files.list(Paths.get(path)).asSequence().mapNotNull { path ->
                    FileSystemEntityFactory.createEntity(path.toString())
                }.toList()

                contentsCache!!
            } catch (e: Exception) {
                println("Exception in getContents: ${e.message}")
                e.printStackTrace()
                emptyList<FileSystemEntity>()
            }
        }
    }

    val hasAccess: Boolean
        get() = Files.isReadable(Paths.get(path))

    val isEmpty: Boolean
        // As I understand, the Files.list returns a Stream
        // findAny() will stop once it meets a single document
        // so, even if directory contains 10000s of files, it will not hurt the
        // performance to check whether it's present or not.
        // TODO: test this idea
        get() = if (hasAccess) {
            Files.list(Paths.get(path)).use { it.findAny().isPresent.not() }
        } else {
            // If we don't have access to the directory, assume it's not empty
            false
        }

    fun invalidateCache() {
        contentsCache = null
    }
}
