package model

/**
 * Implements observer pattern to catch changes
 * of the current directory and notify the
 * respective views.
 */
interface DirectoryObserver {
    fun onDirectoryChanged(newDirectory: ExplorerDirectory)
}
