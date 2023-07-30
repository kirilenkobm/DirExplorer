package model

import java.nio.file.*

/**
 * Class representing a zip archive in the file system.
 *
 * This class extends the ExplorableEntity interface, allowing the application
 * to treat zip archives as if they were directories. This is achieved by creating a hidden
 * temporary directory and asynchronously extracting the contents of the zip archive into this directory.
 * The extraction process is managed by the ZipArchiveService class.
 *
 * tempDir holds the path to the temporary directory where the contents of the zip archive are extracted.
 * This property is nullable and will be null if the contents have not yet been extracted.
 */
class ZipArchive(override val path: String) : ExplorableEntity {
    var tempDir: Path? = null

    val extension: String
        get() = Paths.get(path).fileName.toString().substringAfter(".", "")
}
