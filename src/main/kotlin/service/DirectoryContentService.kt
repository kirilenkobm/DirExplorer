package service

import model.*
import state.AppState
import state.Settings
import state.SortOrder
import util.Utils

/**
 * Class responsible for managing the directory content displayed in the user interface.
 *
 * This class acts as a mediator between the UI and the underlying model,
 * handling tasks such as filtering and sorting the directory contents based on various criteria.
 * It uses the AppState to get the current directory and its contents,
 * and applies the necessary transformations before passing the data to the UI.
 *
 * The class provides functionality to filter contents by file extension and visibility (hidden or not),
 * according to the current application settings. It also provides sorting functionality,
 * allowing contents to be sorted by name, type, size, or last modification date.
 *
 * The transformed directory contents are generated asynchronously.
 */
class DirectoryContentService {
    private var sortOrder: SortOrder = SortOrder.TYPE
    private var sortInverse: Boolean = false

    /**
     * Filter contents of a directory according to filters applied.
     * Such as - showing hidden files or not, and
     * showing files of the specified extension or not.
     */
    private fun filterContents(contents: List<FileSystemEntity>): List<FileSystemEntity> {
        var filteredContents = if (AppState.getFilter().isNotEmpty()) {
            contents.filter { entity ->
                val filters = AppState.getFilterList()

                val isExplorerFileAndMatches =
                    entity is ExplorerFile && filters.any { filter -> Utils.matchesExtension(entity.extension, filter) }
                val isZipArchiveAndMatches =
                    entity is ZipArchive && filters.any { filter -> Utils.matchesExtension(entity.extension, filter) }

                isExplorerFileAndMatches || isZipArchiveAndMatches
            }
        } else {
            contents
        }

        // Exclude hidden files if they are not needed
        filteredContents = filteredContents.filter { entity ->
            !(entity.isHidden && !Settings.showHiddenFiles)
        }
        return filteredContents
    }

    private fun sortContents(contents: List<FileSystemEntity>) : List<FileSystemEntity> {
        // Sort according to sorting function selected
        var sortedContents = when (sortOrder) {
            SortOrder.NAME -> contents.sortedBy { it.name }
            SortOrder.TYPE -> contents.sortedWith(
                compareBy<FileSystemEntity> {
                    when (it) {
                        is ExplorerDirectory -> 0
                        is ExplorerFile -> 1
                        is ZipArchive -> 2
                        is ExplorerSymLink -> 3
                        else -> 4
                    }
                }.thenBy {
                    if (it is ExplorerFile) it.extension else ""
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
        if (sortInverse) { sortedContents = sortedContents.asReversed() }
        return sortedContents
    }

    private fun filterAndSortContents(contents: List<FileSystemEntity>): List<FileSystemEntity> {
        val filteredContents = filterContents(contents)
        return sortContents(filteredContents)
    }

    suspend fun generateContentForView(): List<FileSystemEntity> {
        val currentContents = AppState.currentExplorerDirectory.getContents()
        return filterAndSortContents(currentContents)
    }

    // Double click inverts sorting order
    fun updateSortOrder(order: SortOrder) {
        if (sortOrder == order) {
            sortInverse = !sortInverse
        } else {
            sortOrder = order
            sortInverse = false
        }
    }
}
