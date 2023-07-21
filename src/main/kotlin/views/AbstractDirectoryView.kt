package views

import dataModels.ExplorerDirectory
import dataModels.FileSystemEntity
import kotlinx.coroutines.*
import state.AppState
import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardWatchEventKinds
import java.nio.file.WatchEvent
import java.nio.file.WatchKey
import kotlin.coroutines.CoroutineContext

// Abstract class that implements all the methods needed to show
// a directory's content.
abstract class AbstractDirectoryView : CoroutineScope {
    private val job = Job()
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    protected var currentContents: List<FileSystemEntity> = emptyList()
    protected var watchKey: WatchKey? = null

    init {
        launch {
            currentContents = AppState.currentExplorerDirectory.getContents()
        }
        startWatchingDirectory(AppState.currentExplorerDirectory)
    }

    protected fun startWatchingDirectory(directory: ExplorerDirectory) {
        // cancel the previous watch key if applicable
        watchKey?.cancel()

        launch(Dispatchers.IO) {// TODO: check whether IO fits better
            val watchService = FileSystems.getDefault().newWatchService()
            watchKey = Paths.get(directory.path).register(
                watchService,
                StandardWatchEventKinds.ENTRY_DELETE,
                StandardWatchEventKinds.ENTRY_MODIFY,
                StandardWatchEventKinds.ENTRY_CREATE
            )

            while (isActive) {
                val key = watchService.take()
                for (event in key.pollEvents()) {
                    val watchEvent: WatchEvent<*> = event
                    when (watchEvent.kind()) {
                        StandardWatchEventKinds.ENTRY_DELETE,
                        StandardWatchEventKinds.ENTRY_CREATE,
                        StandardWatchEventKinds.ENTRY_MODIFY -> {
                            updateView()
                        }
                    }
                }
            }
        }
    }

    fun onCurrentDirectoryChanged() {
        startWatchingDirectory(AppState.currentExplorerDirectory)
    }

    abstract fun updateView()

    fun dispose() {
        job.cancel()
    }
}
