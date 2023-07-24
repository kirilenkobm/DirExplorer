package dataModels
import java.nio.file.Files
import java.nio.file.Paths

// Named ExplorerFile to avoid collision with java.io.File
class ExplorerFile(override val path: String): FileSystemEntity {

    override val name: String
        get() = Paths.get(path).fileName.toString()

    val extension: String
        get() = Paths.get(path).fileName.toString().substringAfter(".", "")

    val fileType: String
        // Java built in method to determine file type
        get() = Files.probeContentType(Paths.get(path)) ?: "unknown"

    fun readContents(): String {
        // Implement logic to read file contents
        return "Dummy"
    }
}
