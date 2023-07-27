package views

import dataModels.DirectoryObserver
import dataModels.ExplorerDirectory
import kotlinx.coroutines.*
import state.*
import utils.Utils
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Dimension
import javax.swing.BorderFactory
import javax.swing.JLabel
import javax.swing.JPanel
import kotlin.coroutines.CoroutineContext

class StatusBarView : JPanel(), CoroutineScope, DirectoryObserver, SettingsObserver
{
    private val statusLabel = JLabel()
    private val additionalLabel = JLabel()
    private var job = Job()

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    init {
        AppState.addDirectoryObserver(this)
        Settings.addObserver(this)

        layout = BorderLayout()
        preferredSize = Dimension(1280, 20)
        additionalLabel.border = BorderFactory.createEmptyBorder(0, 20, 0, 0)  // padding 20 left
        statusLabel.border = BorderFactory.createEmptyBorder(0, 0, 0, 20)  // padding 20 right
        additionalLabel.text = ""
        add(additionalLabel, BorderLayout.WEST)
        add(statusLabel, BorderLayout.EAST)

        if (Settings.colorTheme == ColorTheme.LIGHT) {
            background = Color.LIGHT_GRAY
            statusLabel.foreground = Color.BLACK
            additionalLabel.foreground = Color.BLACK
        } else {
            background = Color.DARK_GRAY
            statusLabel.foreground = Color.LIGHT_GRAY
            additionalLabel.foreground = Color.LIGHT_GRAY
        }
        onDirectoryChanged(AppState.currentExplorerDirectory)
    }

    private fun updateAdditionalData() {
        additionalLabel.text = "test"
    }

    private fun updateStatus(itemsCount: Int?, totalSize: Long?) {
        if (itemsCount == null || totalSize == null) {
            statusLabel.text = "Loading ..."
        } else if (itemsCount == 0) {
            statusLabel.text = "Empty"
        } else {
            statusLabel.text = "Items: $itemsCount, Total size: ${Utils.humanReadableSize(totalSize)}"
        }
    }

    override fun onDirectoryChanged(newDirectory: ExplorerDirectory) {
        // Cancel any previous job
        job.cancel()
        job = Job()

        // Calculate the itemsCount and totalSize asynchronously
        launch {
            // Delay before showing the placeholder
            delay(250)

            // Check if the job is still active
            if (isActive) {
                // If still active -> show placeholder values
                updateStatus(null, null)
            }

            val itemsCount = newDirectory.getItemsCount()
            val totalSize = newDirectory.getTotalSize()
            updateStatus(itemsCount, totalSize)
            updateAdditionalData()
        }
    }

    override fun onShowHiddenFilesChanged(newShowHiddenFiles: Boolean) {
        // TODO: add or delete a mark about it?
    }

    override fun onViewModeChanged(newViewMode: ViewMode) {
        // TODO: think whether is needed
    }

    override fun onColorThemeChanged(newColorTheme: ColorTheme) {
        if (newColorTheme == ColorTheme.LIGHT) {
            background = Color.LIGHT_GRAY
            statusLabel.foreground = Color.BLACK
        } else {
            background = Color.DARK_GRAY
            statusLabel.foreground = Color.LIGHT_GRAY
        }
    }
}
