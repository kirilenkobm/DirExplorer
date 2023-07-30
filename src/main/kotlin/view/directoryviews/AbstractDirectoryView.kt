package view.directoryviews

import model.*
import kotlinx.coroutines.*
import state.*
import kotlin.coroutines.CoroutineContext

/** Abstract class that implements all the methods needed to show
 * a directory's content.
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

    // TODO: do not forget
    fun dispose() {
        job.cancel()
    }
}
