package dataModels

// Implement observer design pattern
// Usage:
// ZipArchive - to observe changes in AppState
// if temporary directory is not used -> remove it
interface DirectoryObserver {
    fun onDirectoryChanged(newDirectory: ExplorerDirectory)
}
