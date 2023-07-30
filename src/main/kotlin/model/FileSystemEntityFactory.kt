package model

import Constants
import java.nio.file.Files
import java.nio.file.Paths

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