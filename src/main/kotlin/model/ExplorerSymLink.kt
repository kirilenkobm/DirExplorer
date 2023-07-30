package model

import java.nio.file.Files
import java.nio.file.Paths

/**
 * Data model class related to symlinks.
 * In fact, contains only the information about its target path,
 * which can be converted to another FileSystemEntity if need be.
 */
class ExplorerSymLink(override val path: String): FileSystemEntity {
    val target: String
        get() = Files.readSymbolicLink(Paths.get(path)).toString()
}
