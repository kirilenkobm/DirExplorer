package model

import java.nio.file.*


class ZipArchive(override val path: String) : ExplorableEntity {
    var tempDir: Path? = null

    val extension: String
        get() = Paths.get(path).fileName.toString().substringAfter(".", "")
}
