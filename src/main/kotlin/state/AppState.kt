// AppState - manages the whole Application state
package state
import Constants
import dataModels.*
import services.DirectoryWatcher
import services.ZipArchiveService
import views.popupwindows.showErrorDialog
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

/**
 * Application state.
 * Suppose to be the single source of truth for the rest of the components.
 */
object AppState {

    var currentExplorerDirectory: ExplorerDirectory = ExplorerDirectory(System.getProperty("user.home"))
        set(value) {
            field = value
            value.invalidateCache()
            notifyDirectoryObservers(value)
            DirectoryWatcher.startWatching(value)
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
    // Only to replace zipTempDir names to zip Filenames in the address bar
    val tempZipDirToNameMapping = HashMap<String, String>()
    val zipPathToTempDir = HashMap<String, Path>()

    /**
    / * New explorer directory -> where to go
    / * if called from goBack - do not clear forward stack
    */
    fun updateDirectory(newExplorerDirectory: ExplorableEntity,
                        clearForwardStack: Boolean = true,
                        addToBackStack: Boolean = true) {
        // Preserve previous path in case Error occurs
        // to recover the previous state
        println("AppState.updateDirectory triggered with ${newExplorerDirectory.path}")
        val oldDirectoryInCaseOfError = currentExplorerDirectory

        // Check whether the new path points to an existing and readable directory
        val newPath = Paths.get(newExplorerDirectory.path)
        val oldPath = Paths.get(currentExplorerDirectory.path)
        val pathExists = Files.exists(newPath)
        val isDirectory = Files.isDirectory(newPath)
        val isReadable = Files.isReadable(newPath)
        val isZipArchive = newExplorerDirectory is ZipArchive

        if (pathExists && isDirectory && isReadable) {
            if (newPath != oldPath) {
                // This check should not throw an error -> just do nothing
                if (addToBackStack) {
                    if (backStack.size >= Constants.HISTORY_SIZE) {
                        backStack.removeAt(0)
                    }
                    backStack.add(currentExplorerDirectory)
                }
                if (clearForwardStack) forwardStack.clear()
                currentExplorerDirectory = newExplorerDirectory as ExplorerDirectory
            }
        } else if (pathExists && isReadable && isZipArchive) {
            // I selected the following strategy: unpack and create a temp directory
            // and destroy it at some point later.
            // Creating a separate filesystem for zip files could be a bit too much
            // add zip files to back and forward stack instead of tempDirNames
            val zipEntity = newExplorerDirectory as ZipArchive
            val zipArchiveService = ZipArchiveService(zipEntity)
            val zipTempDir = zipPathToTempDir[zipEntity.path] ?: zipArchiveService.extractTo()

            // Update current explorer directory if possible
            zipTempDir?.let {
                zipPathToTempDir[zipEntity.path] = it
                currentExplorerDirectory = ExplorerDirectory(it.toString())
            } ?: run {
                val errorMessage = "Could not enter the ${newExplorerDirectory.path}"
                showErrorDialog(errorMessage)
                currentExplorerDirectory = ExplorerDirectory(oldDirectoryInCaseOfError.path)
            }
        } else {
            // Error occurred: show a message and recover the original state
            val errorMessage = when {
                !pathExists -> "Error! Target directory ${newExplorerDirectory.path} does not exist"
                !isDirectory -> "Error! ${newExplorerDirectory.path} is not a directory"
                else -> "Error! Unable to access directory ${newExplorerDirectory.path}"
            }
            showErrorDialog(errorMessage)  // Using showErrorDialog function defined in ErrorView.kt
            currentExplorerDirectory = ExplorerDirectory(oldDirectoryInCaseOfError.path)
        }
        backStack.forEach { explorableEntity ->
            println(explorableEntity.path)
        }
    }

    fun goHome() {
        updateDirectory(ExplorerDirectory(System.getProperty("user.home")))
    }

    fun goUp() {
        val parentPath = Paths.get(currentExplorerDirectory.path).parent
        if (parentPath != null) {
            updateDirectory(ExplorerDirectory(parentPath.toString()))
        } else {
            // TODO: handle in the UI
            println("Already at root")
        }
    }

    fun goBack() {
        if (backStack.isNotEmpty()) {
            val newExplorerDirectory = backStack.removeAt(backStack.size - 1)
            forwardStack.add(currentExplorerDirectory)
            updateDirectory(
                newExplorerDirectory,
                clearForwardStack = false,
                addToBackStack = false)
        }
    }

    fun goForward() {
        if (forwardStack.isNotEmpty()) {
            val newExplorerDirectory = forwardStack.removeAt(forwardStack.size - 1)
            updateDirectory(newExplorerDirectory, clearForwardStack = false)
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
