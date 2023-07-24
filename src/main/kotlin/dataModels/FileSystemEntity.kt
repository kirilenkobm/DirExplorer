// Interface to make working with groups of directory contents more convenient
package dataModels
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Paths


interface FileSystemEntity {
    val path: String
    val name: String
        get() = Paths.get(path).fileName.toString()

    val isHidden: Boolean
        get() = try {
            Files.isHidden(Paths.get(path))
        } catch (e: IOException) {
            false
        }

    val lastModified: Long
        get() = try {
            Files.getLastModifiedTime(Paths.get(path)).toMillis()
        } catch (e: IOException) {
            0L
        }

    val size: Long
        get() = try {
            Files.size(Paths.get(path))
        } catch (e: IOException) {
            0L
        }
}
