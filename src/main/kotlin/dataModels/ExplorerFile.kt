package dataModels
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.ByteBuffer
import java.nio.channels.FileChannel
import javax.swing.ImageIcon


// Named ExplorerFile to avoid collision with java.io.File
class ExplorerFile(override val path: String): FileSystemEntity {

    val size: Long  // Will be converted to human-readable in the UI
        get() = Files.size(Paths.get(path))

    override val name: String
        get() = Paths.get(path).fileName.toString()

    val extension: String
        // TODO: ask whether extension of a file like *.txt.gz is .txt.gz or just .gz
        get() = Paths.get(path).fileName.toString().substringAfterLast(".", "")

    val fileType: String
        // Java built in method to determine file type
        get() = Files.probeContentType(Paths.get(path)) ?: "unknown"

    fun readContents(): String {
        // Implement logic to read file contents
        return "Dummy"
    }
}
