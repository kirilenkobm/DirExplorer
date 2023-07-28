package dataModels

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import state.AppState
import java.io.IOException
import java.nio.file.*
import java.nio.file.attribute.BasicFileAttributes
import java.util.UUID
import java.util.zip.ZipFile
import kotlin.coroutines.CoroutineContext


class ZipArchive(override val path: String) : ExplorableEntity {
    var tempDir: Path? = null

    val extension: String
        get() = Paths.get(path).fileName.toString().substringAfter(".", "")
}
