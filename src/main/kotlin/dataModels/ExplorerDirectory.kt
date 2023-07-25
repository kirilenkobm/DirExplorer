// Class that manages directory class
package dataModels
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.streams.asSequence
import kotlinx.coroutines.*
import state.SortOrder

// Open to allow inheritance in ZipArchive
open class ExplorerDirectory(override val path: String): ExplorableEntity {
    // TODO: do not forget the manage in UI
    var sortOrder: SortOrder = SortOrder.TYPE
    // Works bad with zips, if decompression is too slow
    private var contentsCache: List<FileSystemEntity>? = null

    // TODO: store in a attribute, call only once
    open suspend fun getContents(): List<FileSystemEntity> = withContext(Dispatchers.IO) {
        contentsCache?.let { return@withContext it }

        contentsCache = Files.list(Paths.get(path)).asSequence().mapNotNull { path ->
            FileSystemEntityFactory.createEntity(path.toString())
        }.toList()

        contentsCache!!
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
}
