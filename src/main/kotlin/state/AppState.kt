// AppState - manages the whole Application state
package state
import dataModels.ExplorerDirectory
import dataModels.ExplorerFile
import views.showErrorDialog
import java.nio.file.Files
import java.nio.file.Paths

const val HISTORY_SIZE = 40


object AppState {
    var currentExplorerDirectory: ExplorerDirectory = ExplorerDirectory(System.getProperty("user.home"))
    var currentExtensionFilter: String = ""
    private var selectedExplorerFile: ExplorerFile? = null  // TODO: maybe UI layer?
    private var backStack: MutableList<ExplorerDirectory> = mutableListOf()
    private var forwardStack: MutableList<ExplorerDirectory> = mutableListOf()

    // New explorer directory -> where to go
    // if called from goBack - do not clear forward stack
    fun updateDirectory(newExplorerDirectory: ExplorerDirectory,
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
                currentExplorerDirectory = newExplorerDirectory
            }
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

    // TODO: block button in UI if backStack.isEmpty()
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

    fun updateFilter(newFilter: String) {
        // TODO: do not forget to make it more dynamic
        currentExtensionFilter = newFilter
    }

    fun eraseFilter() {
        currentExtensionFilter = ""
    }

    fun updateSelectedFile(newExplorerFile: ExplorerFile) {
        selectedExplorerFile = newExplorerFile
    }

    fun isBackStackEmpty(): Boolean {
        return backStack.isEmpty()
    }

    fun isForwardStackEmpty(): Boolean {
        return forwardStack.isEmpty()
    }
}
