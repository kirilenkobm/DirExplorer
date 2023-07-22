package dataModels

import java.nio.file.Files
import java.nio.file.Paths

// Separate entity to handle ZIP archives
class ZipArchive(override val path: String) : FileSystemEntity {

    val size: Long  // Will be converted to human-readable in the UI
        get() = Files.size(Paths.get(path))

    fun getContents(): List<FileSystemEntity> {
        // TODO: implement the logic here
        return emptyList()
    }
}
