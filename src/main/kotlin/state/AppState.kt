// AppState - manages the whole Application state
package state
import dataModels.*
import views.showErrorDialog
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.UUID


object AppState {
    var currentExplorerDirectory: ExplorerDirectory = ExplorerDirectory(System.getProperty("user.home"))
        set(value) {
            field = value
            notifyDirectoryObservers(value)
        }
    var currentExtensionFilter: String = ""
    private var selectedExplorerFile: ExplorerFile? = null  // TODO: maybe UI layer?
    private var backStack: MutableList<ExplorableEntity> = mutableListOf()
    private var forwardStack: MutableList<ExplorableEntity> = mutableListOf()
    private const val HISTORY_SIZE = 40
    private val directoryObservers: MutableList<DirectoryObserver> = mutableListOf()
    private val observersToRemove: MutableList<DirectoryObserver> = mutableListOf()
    // Track all zipArchives that were present during the session to remove
    // all temp directories which could be forgotten when the app closes
    // Or if the app was closed in a zip Archive
    val zipArchives: MutableList<ZipArchive> = mutableListOf()
    // Only to replace zipTempDir names to zip Filenames in the address bar
    val zipDirMapping = HashMap<String, String>()

    // New explorer directory -> where to go
    // if called from goBack - do not clear forward stack
    fun updateDirectory(newExplorerDirectory: ExplorableEntity,
                        clearForwardStack: Boolean = true,
                        addToBackStack: Boolean = true) {
        // Preserve previous path in case Error occurs
        // to recover the previous state
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
                    if (backStack.size >= HISTORY_SIZE) {
                        backStack.removeAt(0)
                    }
                    backStack.add(currentExplorerDirectory)
                }
                if (clearForwardStack) forwardStack.clear()
                currentExplorerDirectory = newExplorerDirectory as ExplorerDirectory
            }
        } else if (pathExists && isReadable && isZipArchive){
            // I selected the following strategy: unpack and create a temp directory
            // then destroy it once we left it. Creating a separate filesystem for zip
            // files could be a bit too much
            // TODO: idea works poorly with "back" and "forward" functions
            val tempDir = (newExplorerDirectory as ZipArchive).extractTo()
            currentExplorerDirectory = ExplorerDirectory(tempDir.toString())
            // TODO: remove the unused zip temp dir
        } else {
            // Error occurred: show a message and recover the original state
            val errorMessage = when {
                !pathExists -> "Error! Target directory ${newExplorerDirectory.path} does not exist"
                !isDirectory -> "Error! ${newExplorerDirectory.path} is not a directory"
                !isReadable -> "Error! Unable to access directory ${newExplorerDirectory.path}"
                else -> "Unknown error"
            }
            showErrorDialog(errorMessage)  // Using showErrorDialog function defined in ErrorView.kt
            currentExplorerDirectory = ExplorerDirectory(oldDirectoryInCaseOfError.path)
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

    fun addZipArchive(zipArchive: ZipArchive) {
        zipArchives.add(zipArchive)
    }

    fun cleanupAllZipArchives() {
        // to avoid ConcurrentModificationException
        val zipArchivesCopy = ArrayList(zipArchives)
        zipArchivesCopy.forEach { it.cleanup() }
    }
}
