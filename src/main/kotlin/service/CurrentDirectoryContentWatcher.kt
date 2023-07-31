package service

import model.ExplorerDirectory
import kotlinx.coroutines.*
import state.AppState
import java.nio.file.FileSystems
import java.nio.file.Paths
import java.nio.file.StandardWatchEventKinds
import java.nio.file.WatchEvent
import java.nio.file.WatchKey
import kotlin.coroutines.CoroutineContext

/**
 * Singleton object responsible for monitoring changes in the currently watched directory.
 *
 * This object uses the Java NIO WatchService API to track changes in the current directory,
 * such as file creation, deletion, and modification. It operates in a separate coroutine context.
 * When a change is detected, it triggers a refresh of the current directory state in the AppState class.
 */
object CurrentDirectoryContentWatcher: CoroutineScope {
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
                            println("Event kind: ${watchEvent.kind()}")
                            println("Event count: ${watchEvent.count()}")
                            println("Event context: ${watchEvent.context()}")
                            // Don't want to make notifyDirectoryObservers() public
                            // probably it's the best way to notify the UI
                            AppState.refreshCurrentDirectory()
                        }
                    }
                }
                // reset key after processing events
                val valid = key.reset()
                if (!valid) {
                    break
                }
            }
        }
    }

    // Probably is never needed?
    fun stopWatching() {
        job.cancel()
    }
}
