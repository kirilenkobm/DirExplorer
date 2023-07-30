package model
import java.nio.file.Files
import java.nio.file.Paths

class ExplorerSymLink(override val path: String): FileSystemEntity {
    val target: String
        get() = Files.readSymbolicLink(Paths.get(path)).toString()
}