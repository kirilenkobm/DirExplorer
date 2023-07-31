package view.directoryviews

import model.*
import kotlinx.coroutines.*
import state.*
import kotlin.coroutines.CoroutineContext

/**
 * Abstract class for displaying the content of a directory.
 *
 * This abstract class provides common functionality and implements necessary methods
 * to display the content of a directory. It serves as the base class for table and grid views
 * and handles directory changes, settings updates, and coroutine management.
 */
abstract class AbstractDirectoryView:
    CoroutineScope,
    DirectoryObserver,
    SettingsObserver
{
    private val job = Job()
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    init {
        AppState.addDirectoryObserver(this)
        Settings.addObserver(this)
    }

    abstract fun updateView()

    override fun onDirectoryChanged(newDirectory: ExplorerDirectory) {
        updateView()
    }

    override fun onShowHiddenFilesChanged(newShowHiddenFiles: Boolean) {
        updateView()
    }

    override fun onColorThemeChanged(newColorTheme: ColorTheme) {
        // Only if I implement theme changes
        updateView()
    }

    fun dispose() {
        job.cancel()
    }
}
