package services

import dataModels.*
import state.AppState
import state.Settings
import state.SortOrder
import utils.Utils

/**
 * Layer between UI and Model to handle what directory content
 * to show.
 */
class DirectoryContentService {
    private var sortOrder: SortOrder = SortOrder.TYPE

    private fun filterAndSortContents(contents: List<FileSystemEntity>): List<FileSystemEntity> {
        // First, filter the contents by extension (if filter applied)
        var filteredContents = if (AppState.getFilterList().isNotEmpty()) {
            contents.filter { entity ->
                val filters = AppState.getFilterList()

                val isExplorerFileAndMatches = entity is ExplorerFile && filters.any { filter -> Utils.matchesExtension(entity.extension, filter) }
                val isZipArchiveAndMatches = entity is ZipArchive && filters.any { filter -> Utils.matchesExtension(entity.extension, filter) }

                isExplorerFileAndMatches || isZipArchiveAndMatches
            }
        } else {
            contents
        }

        // Exclude hidden files if they are not needed
        filteredContents = filteredContents.filter { entity ->
            !(entity.isHidden && !Settings.showHiddenFiles)
        }

        // Sort according to sorting function selected
        val sortedContents = when (sortOrder) {
            SortOrder.NAME -> filteredContents.sortedBy { it.name }
            SortOrder.TYPE -> filteredContents.sortedWith(
                compareBy<FileSystemEntity> {
                    when (it) {
                        is ExplorerDirectory -> 0
                        is ExplorerFile -> 1
                        // TODO: add sort by extension
                        is ExplorerSymLink -> 2
                        else -> 3
                    }
                }.thenBy { it.name }
            )
            SortOrder.SIZE -> filteredContents.sortedWith(
                compareBy<FileSystemEntity> {
                    when (it) {
                        is ExplorerDirectory -> 0  // directories on top
                        else -> 1                   // everything else after
                    }
                }.thenComparingLong(FileSystemEntity::size)  // sort by size
                    .thenBy(FileSystemEntity::name)              // then by name
            )
            SortOrder.LAST_MODIFIED -> filteredContents.sortedWith(
                compareBy { it.lastModified }
            )
        }

        return sortedContents
    }

    suspend fun generateContentForView(): List<FileSystemEntity> {
        val currentContents = AppState.currentExplorerDirectory.getContents()
        return filterAndSortContents(currentContents)
    }
}
