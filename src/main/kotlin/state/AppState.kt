package state

import model.*
import service.ZipArchiveService
import java.nio.file.Path
import java.nio.file.Paths

/**
 * Singleton object representing the application state.
 *
 * This object serves as the single source of truth for the rest of the components in the application.
 * It maintains the state of the current directory being explored, the navigation history (back and forward stacks),
 * the current file extension filter, and the observers of directory changes.
 *
 * It also manages the handling of zip archives, including the mapping of temporary directories to zip file names,
 * and the cleanup of temporary directories when the application closes.
 *
 * The AppState object also implements LRU thread-safe cache for storing generated thumbnails,
 * improving the performance of the application by avoiding unnecessary regeneration of thumbnails.
 *
 * The navigation logic has been moved to a separate class, AppStateUpdater.
 *
 */
object AppState {

    var currentExplorerDirectory: ExplorerDirectory = ExplorerDirectory(System.getProperty("user.home"))
        set(value) {
            field = value
            value.invalidateCache()
            notifyDirectoryObservers(value)
        }

    private var currentExtensionFilter: String = ""

    var backStack: MutableList<ExplorableEntity> = mutableListOf()
    var forwardStack: MutableList<ExplorableEntity> = mutableListOf()

    private val directoryObservers: MutableList<DirectoryObserver> = mutableListOf()
    private val observersToRemove: MutableList<DirectoryObserver> = mutableListOf()

    // Track all zipArchives that were present during the session to remove
    // all temp directories which could be forgotten when the app closes
    // Or if the app was closed in a zip Archive
    val zipServices: MutableList<ZipArchiveService> = mutableListOf()
    // Mappings needed to replace zipTempDir names to zip Filenames in the address bar
    // Or to get the respective zipService instance if need be
    // TODO: might be better to add something like "shown name" for ExplorableEntity
    // for better incapsulation, and add something like actualPath value so that app can enter
    // ZipEntity directly, without need to create a temporary ExplorerDirectory instance.
    val tempZipDirToNameMapping = HashMap<String, String>()
    val tempZipDirToServiceMapping = HashMap<String, ZipArchiveService>()
    val zipPathToTempDir = HashMap<String, Path>()

    fun goHome() {
        AppStateUpdater.updateDirectory(ExplorerDirectory(System.getProperty("user.home")))
    }

    fun goUp() {
        val parentPath = Paths.get(currentExplorerDirectory.path).parent
        if (parentPath != null) {
            AppStateUpdater.updateDirectory(ExplorerDirectory(parentPath.toString()))
        } else {
            // just do nothing I guess
            println("Already at root")
        }
    }

    fun goBack() {
        if (backStack.isNotEmpty()) {
            val newExplorerDirectory = backStack.removeAt(backStack.size - 1)
            // the only function from which the forward stack can be populated
            addToForwardStack(currentExplorerDirectory)
            // Update current state accordingly
            AppStateUpdater.updateDirectory(
                newExplorerDirectory,
                clearForwardStack = false,
                addingToBackStack = false)
        }
    }

    private fun addToForwardStack(entity: ExplorableEntity) {
        if (forwardStack.size >= Constants.HISTORY_SIZE) {
            forwardStack.removeAt(0)
        }
        forwardStack.add(entity)
    }

    fun goForward() {
        if (forwardStack.isNotEmpty()) {
            val newExplorerDirectory = forwardStack.removeAt(forwardStack.size - 1)
            AppStateUpdater.updateDirectory(newExplorerDirectory, clearForwardStack = false)
        }
    }

    fun getFilter(): String {
        return currentExtensionFilter
    }

    fun getFilterList(): List<String> {
        return currentExtensionFilter.split(",")
    }

    /**
     * Go through the current path and check whether it includes zip files or not.
     */
    fun insideZip(): Boolean {
        val path = Paths.get(currentExplorerDirectory.path)
        return path.any { tempZipDirToNameMapping[it.toString()] != null }
    }

    fun getZipServiceForDirectory(): ZipArchiveService? {
        val path = Paths.get(currentExplorerDirectory.path)
        // Find the temp directory name in the path
        val tempDirName = path.firstOrNull { tempZipDirToNameMapping.containsKey(it.toString()) }
        // Return the corresponding ZipArchiveService, or null if not found
        return tempDirName?.let { tempZipDirToServiceMapping[it.toString()] }
    }

    fun updateFilter(newFilter: String) {
        currentExtensionFilter = newFilter
        notifyDirectoryObservers(currentExplorerDirectory)
    }

    fun addDirectoryObserver(observer: DirectoryObserver) {
        directoryObservers.add(observer)
    }

    fun markObserverForRemoval(observer: DirectoryObserver) {
        observersToRemove.add(observer)
    }

    private fun removeMarkedObservers() {
        directoryObservers.removeAll(observersToRemove)
        observersToRemove.clear()
    }

    private fun notifyDirectoryObservers(newDirectory: ExplorerDirectory) {
        directoryObservers.forEach { it.onDirectoryChanged(newDirectory) }
        removeMarkedObservers()
    }

    fun addZipArchive(zipArchiveService: ZipArchiveService) {
        zipServices.add(zipArchiveService)
    }

    fun cleanupAllZipArchives() {
        // to avoid ConcurrentModificationException
        val zipArchivesCopy = ArrayList(zipServices)
        zipArchivesCopy.forEach { it.cleanup() }
    }

    fun refreshCurrentDirectory() {
        currentExplorerDirectory.invalidateCache()
        notifyDirectoryObservers(currentExplorerDirectory)
    }
}
