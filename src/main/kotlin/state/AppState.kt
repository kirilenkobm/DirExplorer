// AppState - manages the whole Application state
package state
import dataModels.ExplorerDirectory
import dataModels.ExplorerFile
import java.nio.file.Files
import java.nio.file.Paths

const val HISTORY_SIZE = 40


object AppState {
    var currentExplorerDirectory: ExplorerDirectory = ExplorerDirectory(System.getProperty("user.home"))
    var currentFilter: String = ""
    var currentViewMode: ViewMode = ViewMode.ICONS
    var selectedExplorerFile: ExplorerFile? = null
    var showHiddenFiles: Boolean = false
    var colorTheme: ColorTheme = ColorTheme.SYSTEM
    var backStack: MutableList<ExplorerDirectory> = mutableListOf()
    var forwardStack: MutableList<ExplorerDirectory> = mutableListOf()

    fun updateDirectory(newExplorerDirectory: ExplorerDirectory) {
        if (Files.exists(Paths.get(newExplorerDirectory.path))) {
            if (currentExplorerDirectory.path != newExplorerDirectory.path) {
                if (backStack.size >= HISTORY_SIZE) {
                    // otherwise the history stack can get unlimited size
                    backStack.removeAt(0)
                }
                backStack.add(currentExplorerDirectory)
                forwardStack.clear()
                currentExplorerDirectory = newExplorerDirectory
            }
        } else {
            // let's consider scenario where target directory does not exist
            // for example user jumped forward to already deleted place
            println("Error! Target directory ${newExplorerDirectory.path} does not exist")
            // TODO: show ERR in UI
            currentExplorerDirectory = ExplorerDirectory(System.getProperty("user.home"))
        }
    }

    fun goBack() {  // TODO: block button in UI if backStack.isEmpty()
        if (backStack.isNotEmpty()) {
            val newExplorerDirectory = backStack.removeAt(backStack.size - 1)
            if (forwardStack.size >= HISTORY_SIZE) {
                forwardStack.removeAt(0)
            }
            forwardStack.add(currentExplorerDirectory)
            updateDirectory(newExplorerDirectory)
        }
    }

    fun goForward() {  // TODO: block button in UI if forwardStack.isEmpty()
        if (forwardStack.isNotEmpty()) {
            val newExplorerDirectory = forwardStack.removeAt(forwardStack.size - 1)
            if (backStack.size >= HISTORY_SIZE) {
                backStack.removeAt(0)
            }
            backStack.add(currentExplorerDirectory)
            updateDirectory(newExplorerDirectory)
        }
    }

    fun updateFilter(newFilter: String) {
        // TODO: do not forget to make it more dynamic
        currentFilter = newFilter
    }

    fun eraseFilter() {
        currentFilter = ""
    }

    fun updateViewMode(newViewMode: ViewMode) {
        currentViewMode = newViewMode
    }

    fun updateSelectedFile(newExplorerFile: ExplorerFile) {
        selectedExplorerFile = newExplorerFile
    }

    fun toggleShowHiddenFiles() {
        showHiddenFiles = !showHiddenFiles
    }

    fun changeColorTheme(newColorTheme: ColorTheme) {
        colorTheme = newColorTheme
    }
}