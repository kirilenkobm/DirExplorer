package model

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


class ServiceDir(override val path: String) : ExplorerDirectory(path) {
    override suspend fun getContents(): List<FileSystemEntity> = withContext(Dispatchers.IO) {
        return@withContext emptyList()
    }
}
