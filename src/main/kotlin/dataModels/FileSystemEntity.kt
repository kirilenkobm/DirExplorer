// Interface to make working with groups of directory contents more convenient
package dataModels
import java.nio.file.Files
import java.nio.file.Paths

interface FileSystemEntity {
    val path: String
    val name: String
        get() = Paths.get(path).fileName.toString()
    val isHidden: Boolean  // TODO: check whether is applicable to any entity
        // returns false for inaccessible
        get() = Files.isHidden(Paths.get(path))
    val lastModified: Long
        get() = Files.getLastModifiedTime(Paths.get(path)).toMillis()
    // TODO date created: check whether it's quite irreliable on some systems
}
