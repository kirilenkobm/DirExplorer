package dataModels

import java.nio.file.Files
import java.nio.file.Paths

// Separate entity to handle ZIP archives
class ZipArchive(override val path: String) : FileSystemEntity {

    fun getContents(): List<FileSystemEntity> {
        // TODO: implement the logic here
        return emptyList()
    }
}
