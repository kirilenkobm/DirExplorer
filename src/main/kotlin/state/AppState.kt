// AppState - manages the whole Application state
package state
import dataModels.ExplorerDirectory
import dataModels.ExplorerFile
import java.nio.file.Files
import java.nio.file.Paths

const val HISTORY_SIZE = 40


object AppState {
    var currentExplorerDirectory: ExplorerDirectory = ExplorerDirectory(System.getProperty("user.home"))
    var currentExtensionFilter: String = ""
    private var selectedExplorerFile: ExplorerFile? = null  // TODO: maybe UI layer?
    private var backStack: MutableList<ExplorerDirectory> = mutableListOf()
    private var forwardStack: MutableList<ExplorerDirectory> = mutableListOf()

    // TODO: Make it return Bool -> success or not
    // New explorer directory -> where to go
    // if called from goBack - do not clear forward stack
    fun updateDirectory(newExplorerDirectory: ExplorerDirectory,
                        clearForwardStack: Boolean = true,
                        addToBackStack: Boolean = true) {
        if (Files.exists(Paths.get(newExplorerDirectory.path))) {
            if (currentExplorerDirectory.path != newExplorerDirectory.path) {

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
            println("Error! Target directory ${newExplorerDirectory.path} does not exist")
            currentExplorerDirectory = ExplorerDirectory(System.getProperty("user.home"))
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
}
