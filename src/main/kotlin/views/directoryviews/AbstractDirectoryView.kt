package views.directoryviews

import dataModels.*
import kotlinx.coroutines.*
import state.*
import views.showErrorDialog
import java.awt.Desktop
import java.io.File
import java.io.IOException
import kotlin.coroutines.CoroutineContext

/** Abstract class that implements all the methods needed to show
 * a directory's content.
 */
abstract class AbstractDirectoryView:
    CoroutineScope,
    DirectoryObserver,
    SettingsObserver {
    private val job = Job()
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job
    protected var currentContents: List<FileSystemEntity> = emptyList()
    // To avo
    private var visitedSymlinks: MutableSet<String> = mutableSetOf()

    init {
        AppState.addDirectoryObserver(this)
        Settings.addObserver(this)
    }

    /**
     For each entity, defines the action on mouse click.
     @param entity: clicked FileSystemEntity, such as
     Directory, Regular File, Archive, etc.
     */
    fun performEntityAction(entity: FileSystemEntity) {
        when(entity) {
            is ExplorerDirectory -> {
                AppState.updateDirectory(entity)
                updateView()
            }
            is ExplorerFile -> {
                openFile(entity)
            }
            is ExplorerSymLink -> {
                try {
                    val targetPath = entity.target
                    val targetEntity = FileSystemEntityFactory.createEntity(targetPath)

                    if (targetPath in visitedSymlinks) {
                        showErrorDialog("Circular link detected: $targetPath")
                        return
                    }

                    visitedSymlinks.add(targetPath)
                    performEntityAction(targetEntity)
                } catch (e: IOException) {
                    showErrorDialog("Error following symlink: ${e.message}")
                }
            }
            is ZipArchive -> {
                AppState.updateDirectory(entity)
                updateView()
            }
            is UnknownEntity -> {
                showErrorDialog("Not supported file system entity ${entity.path}")
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
                        // TODO: add filter by extension
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

    abstract fun updateView()

    override fun onDirectoryChanged(newDirectory: ExplorerDirectory) {
        updateView()
    }

    override fun onShowHiddenFilesChanged(newShowHiddenFiles: Boolean) {
        updateView()
    }

    override fun onViewModeChanged(newViewMode: ViewMode) {
        // Irrelevant -> handled by MainView
    }

    override fun onColorThemeChanged(newColorTheme: ColorTheme) {
        // Only if I implement theme changes
    }

    // TODO: do not forget
    fun dispose() {
        job.cancel()
    }
}
