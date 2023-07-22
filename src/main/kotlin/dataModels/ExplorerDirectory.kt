// Class that manages directory class
package dataModels
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.streams.asSequence
import kotlinx.coroutines.*
import state.Settings
import state.SortOrder

// Open to allow inheritance in ZipArchive
open class ExplorerDirectory(override val path: String): FileSystemEntity {
    val zipExtensions = setOf(".zip", ".jar", ".war", ".ear", ".apk", ".gz")
    val sortOrder: SortOrder = SortOrder.TYPE

    open suspend fun getContents(): List<FileSystemEntity> = withContext(Dispatchers.IO) {
        Files.list(Paths.get(path)).asSequence().mapNotNull { path ->
            when {
                // TODO: filter hidden files out on the UI layer
                Files.isHidden(path) && !Settings.showHiddenFiles -> null
                Files.isSymbolicLink(path) -> ExplorerSymLink(path.toString())
                Files.isRegularFile(path) -> {
                    // TODO: check using MIME types
                    if (zipExtensions.any { ext -> path.toString().endsWith(ext) }) {
                        ZipArchive(path.toString())
                    } else {
                        ExplorerFile(path.toString())
                    }
                }
                Files.isDirectory(path) -> ExplorerDirectory(path.toString())
                else -> UnknownEntity(path.toString())
            }
        }.toList()
    }
}
