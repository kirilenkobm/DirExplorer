package model

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Not sure whether is needed.
 */
object RootsDirectoryWindows : ExplorableEntity {
    override val path: String
        get() = "/"

    // Not sure whether number of roots is that big that requires cache
    suspend fun getContents(): List<FileSystemEntity> = withContext(Dispatchers.IO) {
        File.listRoots().map { ExplorerDirectory(it.absolutePath) }
    }
}
