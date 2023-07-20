// Class that manages directory class
package dataModels
import java.nio.file.Files
import java.nio.file.Paths


// Open to allow inheritance in ZipArchive
open class ExplorerDirectory(override val path: String): FileSystemEntity {
    val zipExtensions = setOf(".zip", ".jar", ".war", ".ear", ".apk", ".gz")

    open val explorerFiles: List<FileSystemEntity>
        get() = Files.list(Paths.get(path))
            .filter { Files.isRegularFile(it) }
            .map {
                // not the most reliable way to check whether it is an archive
                if (zipExtensions.any { ext -> it.toString().endsWith(ext) }) {
                    ZipArchive(it.toString())
                } else {
                    ExplorerFile(it.toString())
                }
            }
            .toList()

    open val directories: List<ExplorerDirectory>
        get() = Files.list(Paths.get(path))
            .filter { Files.isDirectory(it) }
            .map { ExplorerDirectory(it.toString()) }
            .toList()

    open val symLinks: List<ExplorerSymLink>
        get() = Files.list(Paths.get(path))
            .filter { Files.isSymbolicLink(it) }
            .map { ExplorerSymLink(it.toString()) }
            .toList()

    open fun getContents(): List<FileSystemEntity> {
        return explorerFiles + directories + symLinks
    }

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

    fun showAllContents() {
        getContents().sortedBy { it.path }.forEach { entity ->
            when (entity) {
                is ExplorerFile -> println("File: ${entity.path}, Size: ${entity.size}")
                is ExplorerDirectory -> println("Directory: ${entity.path}")
                is ExplorerSymLink -> println("SymLink: ${entity.path}, Target: ${entity.target}")
            }
        }
    }
}
