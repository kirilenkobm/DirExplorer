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
    val sortOrder: SortOrder = SortOrder.NAME

    open suspend fun getContents(): List<FileSystemEntity> = withContext(Dispatchers.IO) {
        Files.list(Paths.get(path)).asSequence().mapNotNull { path ->
            when {
                Files.isHidden(path) && !Settings.showHiddenFiles -> null
                Files.isRegularFile(path) -> {
                    // not the most reliable way to check whether it is an archive
                    if (zipExtensions.any { ext -> path.toString().endsWith(ext) }) {
                        ZipArchive(path.toString())
                    } else {
                        ExplorerFile(path.toString())
                    }
                }
                Files.isDirectory(path) -> ExplorerDirectory(path.toString())
                Files.isSymbolicLink(path) -> ExplorerSymLink(path.toString())
                // else -> UnknownEntity(path.toString())  # TODO: handle this case
                else -> null
            }
        }.toList()  // TODO: implement sorting
    }

//    open val contents: List<FileSystemEntity>
//        get() = Files.list(Paths.get(path)).asSequence().mapNotNull { path ->
//            when {
//                Files.isHidden(path) && !Settings.showHiddenFiles -> null
//                Files.isRegularFile(path) -> {
//                    // not the most reliable way to check whether it is an archive
//                    if (zipExtensions.any { ext -> path.toString().endsWith(ext) }) {
//                        ZipArchive(path.toString())
//                    } else {
//                        ExplorerFile(path.toString())
//                    }
//                }
//                Files.isDirectory(path) -> ExplorerDirectory(path.toString())
//                Files.isSymbolicLink(path) -> ExplorerSymLink(path.toString())
//                // else -> UnknownEntity(path.toString())  # TODO: handle this case
//                else -> null
//            }
//        }.toList()  // TODO: implement sorting
    //        }.sortedWith(when (sortOrder) {
    //            SortOrder.NAME -> compareBy { it.name }
    //            SortOrder.TYPE -> compareBy { it.type }
    //            SortOrder.DATE_CREATED -> compareBy { it.dateCreated }
    //        }).toList()

    val name: String
        get() = Paths.get(path).fileName.toString()

    val isHidden: Boolean
        get() = Files.isHidden(Paths.get(path))

    open fun getSize(): Long {
        // Implement logic to get total size of directory
        return 0
    }

    open fun sortByName() {
        // Implement logic to sort files and directories by name
    }

    open fun sortBySize() {
        // Implement logic to sort files and directories by size
    }

//    fun showAllContents() {
//        contents.sortedBy { it.path }.forEach { entity ->
//            when (entity) {
//                is ExplorerFile -> println("File: ${entity.path}, Size: ${entity.size}")
//                is ExplorerDirectory -> println("Directory: ${entity.path}")
//                is ExplorerSymLink -> println("SymLink: ${entity.path}, Target: ${entity.target}")
//            }
//        }
//    }
}
