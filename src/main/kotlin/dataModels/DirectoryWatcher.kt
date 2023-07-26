package dataModels

import kotlinx.coroutines.*
import java.nio.file.FileSystems
import java.nio.file.Paths
import java.nio.file.StandardWatchEventKinds
import java.nio.file.WatchEvent
import java.nio.file.WatchKey
import kotlin.coroutines.CoroutineContext

object DirectoryWatcher: CoroutineScope {
    private var directory: ExplorerDirectory? = null
    private var watchKey: WatchKey? = null
    private val job = Job()

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO + job

    fun startWatching(newDirectory: ExplorerDirectory) {
        // skip if called on the same dir
        if (directory?.path == newDirectory.path) return
        // cancel the previous key if applicable
        watchKey?.cancel()
        directory = newDirectory

        launch(Dispatchers.IO) {
            val watchService = FileSystems.getDefault().newWatchService()
            watchKey = Paths.get(directory!!.path).register(
                watchService,
                StandardWatchEventKinds.ENTRY_DELETE,
                StandardWatchEventKinds.ENTRY_CREATE,
                StandardWatchEventKinds.ENTRY_MODIFY
            )

            while (isActive) {
                val key = watchService.take()
                for (event in key.pollEvents()) {
                    val watchEvent: WatchEvent<*> = event

                    when (watchEvent.kind()) {
                        StandardWatchEventKinds.ENTRY_DELETE,
                        StandardWatchEventKinds.ENTRY_CREATE,
                        StandardWatchEventKinds.ENTRY_MODIFY -> {
                            // TODO: implement this part
                            println("Change found")
                        }
                    }
                }
            }
        }
    }

    fun stopWatching() {
        job.cancel()
    }
}