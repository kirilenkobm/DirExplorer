package views.directoryviews

import dataModels.*
import kotlinx.coroutines.*
import state.*
import views.popupwindows.showErrorDialog
import java.awt.Desktop
import java.io.File
import java.io.IOException
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
    // To avoid circular links
    private var visitedSymlinks: MutableSet<String> = mutableSetOf()

    init {
        AppState.addDirectoryObserver(this)
        Settings.addObserver(this)
    }

    /**
     For each entity, defines the action on mouse click.
     @param entity: clicked FileSystemEntity, such as
     Directory, Regular File, Archive, etc.
     */
    fun performEntityAction(entity: FileSystemEntity) {
        when(entity) {
            is ExplorerDirectory -> {
                AppState.updateDirectory(entity)
                updateView()
            }
            is ExplorerFile -> {
                openFile(entity)
            }
            is ExplorerSymLink -> {
                try {
                    val targetPath = entity.target
                    val targetEntity = FileSystemEntityFactory.createEntity(targetPath)

                    if (targetPath in visitedSymlinks) {
                        showErrorDialog("Circular link detected: $targetPath")
                        return
                    }

                    visitedSymlinks.add(targetPath)
                    performEntityAction(targetEntity)
                } catch (e: IOException) {
                    showErrorDialog("Error following symlink: ${e.message}")
                }
            }
            is ZipArchive -> {
                AppState.updateDirectory(entity)
                updateView()
            }
            is UnknownEntity -> {
                showErrorDialog("Not supported file system entity ${entity.path}")
            }
        }
    }

    private fun openFile(fileEntity: ExplorerFile) {
        if (Desktop.isDesktopSupported()) {
            try {
                Desktop.getDesktop().open(File(fileEntity.path))
            } catch (ex: IOException) {
                ex.printStackTrace()
                showErrorDialog("Error: Unable to open the file at ${fileEntity.path}.")
            }
        } else {
            showErrorDialog("Error: Desktop operations are not supported on this system.")
        }
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
