package views

import Constants
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
        setupView()
        setupColors()
        onDirectoryChanged(AppState.currentExplorerDirectory)
    }

    private fun setupView() {
        AppState.addDirectoryObserver(this)
        Settings.addObserver(this)

        layout = BorderLayout()
        preferredSize = Dimension(Constants.PREFERRED_WIDTH, 20)
        setupLabel(additionalLabel, BorderLayout.WEST, 20, 0)
        setupLabel(statusLabel, BorderLayout.EAST, 0, 20)
    }

    private fun setupLabel(label: JLabel, position: String, leftPadding: Int, rightPadding: Int) {
        label.border = BorderFactory.createEmptyBorder(0, leftPadding, 0, rightPadding)
        label.text = ""
        add(label, position)
    }

    private fun updateAdditionalData() {
        additionalLabel.text = "${getHiddenFilesLabel()} ${getZipLabel()}"
    }

    private fun getHiddenFilesLabel() = if (Settings.showHiddenFiles) "showing hidden files" else ""

    private fun getZipLabel() = if (AppState.insideZip()) "inside a zip archive" else ""

    private fun updateStatus(itemsCount: Int?, totalSize: Long?) {
        if (itemsCount == null || totalSize == null) {
            statusLabel.text = "Loading ..."
        } else if (itemsCount == 0) {
            statusLabel.text = "Empty"
        } else {
            statusLabel.text = "Items: $itemsCount, Total size: ${Utils.humanReadableSize(totalSize)}"
        }
    }

    private fun setupColors() {
        val themeColor = if (Settings.colorTheme == ColorTheme.LIGHT) Color.LIGHT_GRAY else Color.DARK_GRAY
        val textColor = if (Settings.colorTheme == ColorTheme.LIGHT) Color.BLACK else Color.LIGHT_GRAY

        background = themeColor
        statusLabel.foreground = textColor
        additionalLabel.foreground = textColor
    }

    /**
     * If directory changes -> recalculate values to be shown
     */
    override fun onDirectoryChanged(newDirectory: ExplorerDirectory) {
        resetJob()
        calculateAndUpdateStatus(newDirectory)
    }

    private fun resetJob() {
        job.cancel()
        job = Job()
    }

    private fun calculateAndUpdateStatus(newDirectory: ExplorerDirectory) = launch {
        delay(250)
        if (isActive) updateStatus(null, null)

        val itemsCount = newDirectory.getItemsCount()
        val totalSize = newDirectory.getTotalSize()
        updateStatus(itemsCount, totalSize)
        updateAdditionalData()
    }

    override fun onShowHiddenFilesChanged(newShowHiddenFiles: Boolean) {
        onDirectoryChanged(AppState.currentExplorerDirectory)
    }

    override fun onColorThemeChanged(newColorTheme: ColorTheme) {
        setupColors()
    }

    override fun onViewModeChanged(newViewMode: ViewMode) { }
}
