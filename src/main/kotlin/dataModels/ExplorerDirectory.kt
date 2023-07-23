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
    val zipExtensions = setOf(".zip", ".7z", ".jar", ".war", ".rar", ".bz", ".bz2", ".gz")
    val sortOrder: SortOrder = SortOrder.TYPE

    // TODO: store in a attribute, call only once
    open suspend fun getContents(): List<FileSystemEntity> = withContext(Dispatchers.IO) {
        Files.list(Paths.get(path)).asSequence().mapNotNull { path ->
            when {
                // TODO: filter hidden files out on the UI layer
                //  Files.isHidden(path) && !Settings.showHiddenFiles -> null
                Files.isSymbolicLink(path) -> ExplorerSymLink(path.toString())
                Files.isRegularFile(path) -> {
                    // val mimeType = Files.probeContentType(path) ?: "unknown"
                    // TODO: check using MIME types - or maybe not
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

    open suspend fun getUniqueExtensions(): Set<String> = withContext(Dispatchers.IO) {
        getContents()
            .filterIsInstance<ExplorerFile>()  // Filter out non-files
            .map { it.extension }  // Map to file extensions
            .toSet()  // Remove duplicates
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
