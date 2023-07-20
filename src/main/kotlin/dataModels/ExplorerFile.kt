package dataModels
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.ByteBuffer
import java.nio.channels.FileChannel


// Named ExplorerFile to avoid collision with java.io.File
class ExplorerFile(override val path: String): FileSystemEntity {
    val BUFFER_SIZE_TO_CHECK_IF_BINARY = 8192

    val size: Long  // Will be converted to human-readable in the UI
        get() = Files.size(Paths.get(path))

    val name: String
        get() = Paths.get(path).fileName.toString()

    val extension: String
        // TODO: ask whether extension of a file like *.txt.gz is .txt.gz or just .gz
        get() = Paths.get(path).fileName.toString().substringAfterLast(".", "")

    val lastModified: Long
        get() = Files.getLastModifiedTime(Paths.get(path)).toMillis()

    val isHidden: Boolean
        get() = Files.isHidden(Paths.get(path))

    fun isBinary(): Boolean {
        // TODO: implement a good method that checks whether file is binary or not
        return false
    }

    fun readContents(): String {
        // Implement logic to read file contents
        return "Dummy"
    }
}
