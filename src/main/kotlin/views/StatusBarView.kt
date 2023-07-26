package views

import dataModels.DirectoryObserver
import dataModels.ExplorerDirectory
import kotlinx.coroutines.*
import state.AppState
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Dimension
import javax.swing.BorderFactory
import javax.swing.JLabel
import javax.swing.JPanel
import kotlin.coroutines.CoroutineContext

class StatusBarView : JPanel(), CoroutineScope, DirectoryObserver {
    private val statusLabel = JLabel()
    private var job = Job()

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    init {
        AppState.addDirectoryObserver(this)
        layout = BorderLayout()
        preferredSize = Dimension(1280, 20)
        statusLabel.border = BorderFactory.createEmptyBorder(0, 0, 0, 20)  // padding 10 right
        add(statusLabel, BorderLayout.EAST)
        background = Color.LIGHT_GRAY
        onDirectoryChanged(AppState.currentExplorerDirectory)
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
        }
    }
}
