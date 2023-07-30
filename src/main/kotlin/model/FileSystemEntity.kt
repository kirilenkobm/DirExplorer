// Interface to make working with groups of directory contents more convenient
package model

import Constants
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Paths

/**
 * Interface for all file system entities.
 * Implements values applicable (more or less) to all kinds of entities, such as:
 * - Directory
 * - File
 * - Symlink
 * - ZipArchive (which behaves as class and directory in this app)
 * - Unknown entity that covers all other cases such as named pipes.
 *
 * File system entities are generated from filesystem paths (in Strings)
 * using FileSystemEntityFactory class.
 */
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
            Constants.UNKNOWN_TIME
        }

    val size: Long
        get() = try {
            Files.size(Paths.get(path))
        } catch (e: IOException) {
            Constants.UNKNOWN_FILE_SIZE
        }
}
