package views.directoryviews

import dataModels.*
import kotlinx.coroutines.*
import state.AppState
import state.Settings
import state.SortOrder
import views.TopBarView
import views.showErrorDialog
import java.awt.Desktop
import java.io.File
import java.io.IOException
import java.nio.file.FileSystems
import java.nio.file.Paths
import java.nio.file.StandardWatchEventKinds
import java.nio.file.WatchEvent
import java.nio.file.WatchKey
import java.text.SimpleDateFormat
import kotlin.coroutines.CoroutineContext

// Abstract class that implements all the methods needed to show
// a directory's content.
abstract class AbstractDirectoryView(private val topBarView: TopBarView) : CoroutineScope {
    private val job = Job()
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job
    protected var currentContents: List<FileSystemEntity> = emptyList()
    private var watchKey: WatchKey? = null

    init {
        launch {
            currentContents = AppState.currentExplorerDirectory.getContents()
        }
        startWatchingDirectory(AppState.currentExplorerDirectory)
    }

    fun performEntityAction(entity: FileSystemEntity) {
        when(entity) {
            is ExplorerDirectory -> {
                AppState.updateDirectory(entity)
                updateView()
                topBarView.updateView()
            }
            is ExplorerFile -> {
                openFile(entity)
            }
            is UnknownEntity -> {
                showErrorDialog("Not supported file system entity")
            }
        }
    }

    private fun openFile(fileEntity: ExplorerFile) {
        if (Desktop.isDesktopSupported()) {
            try {
                Desktop.getDesktop().open(File(fileEntity.path))
            } catch (ex: IOException) {
                ex.printStackTrace()
                showErrorDialog("Error: Unable to open the file at ${fileEntity.path}.")
            }
        } else {
            showErrorDialog("Error: Desktop operations are not supported on this system.")
        }
    }

    private fun matchesExtension(entityExtension: String, filterExtension: String): Boolean {
        // Check if filter starts with ~ and should invert the result
        val invertResult = filterExtension.startsWith("~")
        val cleanedFilter = if (invertResult) filterExtension.drop(1) else filterExtension

        // if there is a full match, return true (or false if inverted)
        if(entityExtension == cleanedFilter) return !invertResult

        // check for partial match in case of complex extension
        val entitySubExtensions = entityExtension.split('.')
        if (!invertResult) {
            if (entitySubExtensions.any { it == filterExtension }) return true
        } else {
            if (entitySubExtensions.none { it == filterExtension }) return true
        }

        // check for match with regex pattern
        val regexPattern = filterExtension
            .replace("*", ".*")

        val regex = regexPattern.toRegex()
        val matches = regex.containsMatchIn(entityExtension)
        return if (invertResult) !matches else matches
    }

    protected fun filterAndSortContents(contents: List<FileSystemEntity>): List<FileSystemEntity> {
        var sortedContents = when (AppState.currentExplorerDirectory.sortOrder) {
            SortOrder.NAME -> contents.sortedBy { it.name }
            SortOrder.TYPE -> contents.sortedWith(
                compareBy<FileSystemEntity> {
                    when (it) {
                        is ExplorerDirectory -> 0
                        is ExplorerFile -> 1
                        is ExplorerSymLink -> 2
                        else -> 3
                    }
                }.thenBy { it.name }
            )
            SortOrder.SIZE -> contents.sortedWith(
                compareBy<FileSystemEntity> {
                    when (it) {
                        is ExplorerDirectory -> 0  // directories on top
                        else -> 1                   // everything else after
                    }
                }.thenComparingLong(FileSystemEntity::size)  // sort by size
                    .thenBy(FileSystemEntity::name)              // then by name
            )
            SortOrder.LAST_MODIFIED -> contents.sortedWith(
                compareBy { it.lastModified }
            )
        }

        if (AppState.currentExtensionFilter.isNotEmpty()) {
            sortedContents = sortedContents.filter { entity ->
                entity is ExplorerFile && matchesExtension(entity.extension, AppState.currentExtensionFilter)
            }
        }

        sortedContents = sortedContents.filter { entity ->
            !(entity.isHidden && !Settings.showHiddenFiles)
        }

        return sortedContents
    }

    private fun startWatchingDirectory(directory: ExplorerDirectory) {
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

    // abstract fun setupTableMouseListener()

    fun dispose() {
        job.cancel()
    }

    val dateFormat = SimpleDateFormat("yyyy.MM.dd 'at' HH:mm:ss")

    fun humanReadableSize(bytes: Long): String {
        val kilobyte = 1024.0
        val megabyte = kilobyte * 1024
        val gigabyte = megabyte * 1024
        val terabyte = gigabyte * 1024

        return when {
            bytes < kilobyte -> "$bytes B"
            bytes < megabyte -> String.format("%.1f KB", bytes / kilobyte)
            bytes < gigabyte -> String.format("%.1f MB", bytes / megabyte)
            bytes < terabyte -> String.format("%.1f GB", bytes / gigabyte)
            else -> String.format("%.1f TB", bytes / terabyte)
        }
    }
}
