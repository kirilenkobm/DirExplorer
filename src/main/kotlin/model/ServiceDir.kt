package model

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Special entity that mimics a ExplorerDirectory, however, is not conected to the actual
 * file system content. Is used at shutdown to show an empty directory.
 */
class ServiceDir(override val path: String) : ExplorerDirectory(path) {
    override suspend fun getContents(): List<FileSystemEntity> = withContext(Dispatchers.IO) {
        return@withContext emptyList()
    }
}
