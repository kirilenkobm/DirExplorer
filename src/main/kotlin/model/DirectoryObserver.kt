package model

/**
 * Interface for observing changes in the current directory.
 *
 * This interface implements the observer pattern, allowing objects to monitor changes in the current
 * directory and respond accordingly. Classes implementing this interface will be notified when
 * the current directory changes, allowing them to update their state or the user interface.
 */
interface DirectoryObserver {
    fun onDirectoryChanged(newDirectory: ExplorerDirectory)
}
