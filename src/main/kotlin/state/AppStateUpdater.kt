package state

import model.ExplorableEntity
import model.ExplorerDirectory
import model.ZipArchive
import service.ZipArchiveService
import view.popupwindows.showErrorDialog
import java.nio.file.Files
import java.nio.file.Paths
import java.util.*

/**
 * Singleton object responsible for updating the application state.
 *
 * This object encapsulates the logic for updating the current directory
 * being explored in the application, including the special handling of zip archives.
 * It also manages the navigation history (back and forward stacks) based on the updates.
 */
object AppStateUpdater {
    private var bundle = ResourceBundle.getBundle(Constants.LANGUAGE_BUNDLE_PATH, Settings.language.getLocale())
    //New explorer directory -> where to go
    // if we are going from goBack operation: do not clean up the forward stack
    fun updateDirectory(newExplorerDirectory: ExplorableEntity,
                        clearForwardStack: Boolean = true,
                        addingToBackStack: Boolean = true) {
        // Preserve previous path in case Error occurs
        // to recover the previous state
        val oldDirectoryInCaseOfError = AppState.currentExplorerDirectory

        // Check whether it's a directory or zip
        // if none of the above (for example, go forward to non-existent directory)
        // then show error message and restore the initial state
        if (isValidDirectory(newExplorerDirectory)) {
            handleDirectory(newExplorerDirectory, clearForwardStack, addingToBackStack)
        } else if (isValidZipArchive(newExplorerDirectory)) {
            // handle zip archives in a special way by creating a temp hidden directory
            handleZipArchive(newExplorerDirectory, oldDirectoryInCaseOfError, addingToBackStack)
        } else {
            // Error occurred: show a message and recover the original state
            handleInvalidPath(newExplorerDirectory, oldDirectoryInCaseOfError)
        }
        AppState.backStack.forEach { explorableEntity ->
            println(explorableEntity.path)
        }
    }

    private fun isValidDirectory(entity: ExplorableEntity): Boolean {
        val newPath = Paths.get(entity.path)
        return Files.exists(newPath) && Files.isDirectory(newPath) && Files.isReadable(newPath)
    }

    private fun isValidZipArchive(entity: ExplorableEntity): Boolean {
        val newPath = Paths.get(entity.path)
        return Files.exists(newPath) && Files.isReadable(newPath) && entity is ZipArchive
    }

    private fun handleDirectory(
        newExplorerDirectory: ExplorableEntity,
        clearForwardStack: Boolean,
        addToBackStack: Boolean)
    {
        val newPath = Paths.get(newExplorerDirectory.path)
        val oldPath = Paths.get(AppState.currentExplorerDirectory.path)

        if (newPath != oldPath) {
            if (addToBackStack) {
                addCurrentDirToBackStack()
            }
            if (clearForwardStack) AppState.forwardStack.clear()
            AppState.currentExplorerDirectory = newExplorerDirectory as ExplorerDirectory
        }
    }

    /**
     * I selected the following strategy to handle zip archives:
     *  - create a temporary hidden directory
     *  - start unpacking the archive in a coroutine into this dir using ZipArchiveService
     *  - save the respective ZipArchiveService instance to AppState
     *    so that it can be deleted later when the directory is no longer needed
     */
    private fun handleZipArchive(
        entity: ExplorableEntity,
        oldDirectoryInCaseOfError: ExplorerDirectory,
        addingToBackStack: Boolean)
    {
        val zipEntity = entity as ZipArchive
        val zipArchiveService = ZipArchiveService(zipEntity)
        val zipTempDir = AppState.zipPathToTempDir[zipEntity.path] ?: zipArchiveService.startExtraction()

        zipTempDir?.let {
            // if got zip temp dir -> go there as it was a directory
            AppState.zipPathToTempDir[zipEntity.path] = it
            if (addingToBackStack) {
                addCurrentDirToBackStack()
            }
            AppState.currentExplorerDirectory = ExplorerDirectory(it.toString())
        } ?: run {
            // handle error otherwise, if zipTempDir is null
            val errorMessage = "${bundle.getString("Error")} ${bundle.getString("CouldNotEnterThe")} ${entity.path}"
            showErrorDialog(errorMessage)
            AppState.currentExplorerDirectory = ExplorerDirectory(oldDirectoryInCaseOfError.path)
        }
    }

    private fun handleInvalidPath(entity: ExplorableEntity, oldDirectoryInCaseOfError: ExplorerDirectory) {
        val errorMessage = when {
            !isValidDirectory(entity) ->
                bundle.getString("ErrorTargetDirectory") +
                        " ${entity.path} " +
                        bundle.getString("UnavailableNonexistent")
            !isValidZipArchive(entity) -> "${bundle.getString("CannotProcessZip")} ${entity.path}"
            else -> "${bundle.getString("Error")} ${bundle.getString("CouldNotEnterThe")} ${entity.path}"
        }
        showErrorDialog(errorMessage)
        AppState.currentExplorerDirectory = ExplorerDirectory(oldDirectoryInCaseOfError.path)
    }

    private fun addCurrentDirToBackStack() {
        if (AppState.backStack.size >= Constants.HISTORY_SIZE) {
            AppState.backStack.removeAt(0)
        }
        AppState.backStack.add(AppState.currentExplorerDirectory)
    }
}