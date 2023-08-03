package service

import state.AppState
import state.Settings
import kotlin.system.exitProcess


/**
 * Operations that are performed at the very end of the app's lifecycle.
 */
fun performClosingOperations()  {
    // Stop current directory watcher so that UI is not triggered by next step
    CurrentDirectoryContentWatcher.stopWatching()
    // dump settings for the next session
    Settings.saveSettings()
    // cleanup temp directories created to look at the zip archives content (if any left)
    AppState.cleanupAllZipArchives()
    // Finally, exit the application.
    exitProcess(0)
}
