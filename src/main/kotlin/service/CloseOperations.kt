package service

import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import state.AppState
import state.Settings
import kotlin.system.exitProcess


/**
 * Operations that are performed at the very end of the app's lifecycle.
 */
@OptIn(DelicateCoroutinesApi::class)
fun performClosingOperations() {
    AppState.sendToVoid()
    // Stop current directory watcher so that UI is not triggered by next step
    CurrentDirectoryContentWatcher.stopWatching()
    // dump settings for the next session
    Settings.saveSettings()
    // call blocking cleanup to a separate coroutine context that doesn't block the EDT
    // so that the popup "wait please" window is shown; since this is used only to
    // close the app, I believe GlobalScore.launch is appropriate here
    GlobalScope.launch {
        // cleanup temp directories created to look at the zip archives content (if any left)
        AppState.cleanupAllZipArchives()
        // Finally, exit the application.
        exitProcess(0)
    }
}
