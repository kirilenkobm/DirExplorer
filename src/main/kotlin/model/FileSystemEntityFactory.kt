package model

import Constants
import java.nio.file.Files
import java.nio.file.Paths

/**
 *  This factory class is responsible for creating different types
 *  of FileSystemEntity objects based on the nature of the
 *  file system entity at a given path.
 *  It supports the creation of symbolic links, regular files, directories, and zip archives.
 *  If the entity type at the path is not recognized, an UnknownEntity is created.
 */
object FileSystemEntityFactory {
    fun createEntity(path: String): FileSystemEntity {
        return when {
            Files.isSymbolicLink(Paths.get(path)) -> ExplorerSymLink(path)
            Files.isRegularFile(Paths.get(path)) -> {
                if (path.endsWith(Constants.ZIP_EXTENSION)) {
                    ZipArchive(path)
                } else {
                    ExplorerFile(path)
                }
            }
            Files.isDirectory(Paths.get(path)) -> ExplorerDirectory(path)
            else -> UnknownEntity(path)
        }
    }
}
