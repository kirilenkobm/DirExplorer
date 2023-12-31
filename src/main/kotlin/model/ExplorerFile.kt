package model

import Constants
import java.nio.file.Files
import java.nio.file.Paths

/**
 * Data model class to implement methods relevant to Files (text, binary, etc).
 * Named ExplorerFile to avoid collision with java.io.File
 */
class ExplorerFile(override val path: String): FileSystemEntity {

    override val name: String
        get() = Paths.get(path).fileName.toString()

    val extension: String
        get() = Paths.get(path).fileName.toString().substringAfter(".", "")

    val fileType: String
        // Java built in method to determine MIME file type
        get() = Files.probeContentType(Paths.get(path)) ?: Constants.UNKNOWN_FILE_TYPE
}
