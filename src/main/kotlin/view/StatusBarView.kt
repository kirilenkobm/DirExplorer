package view

import Constants
import model.DirectoryObserver
import model.ExplorerDirectory
import kotlinx.coroutines.*
import service.ZipExtractionStatus
import state.*
import util.Utils
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Dimension
import java.util.ResourceBundle
import javax.swing.BorderFactory
import javax.swing.JLabel
import javax.swing.JPanel
import kotlin.coroutines.CoroutineContext

/**
 * Class representing the status bar view at the bottom of the window.
 *
 * It displays the status of the current directory, including the number of items and total size,
 * and updates this information whenever the directory changes.
 * It also displays additional information such as whether hidden files are being shown
 * and whether the user is inside a zip archive.
 */
class StatusBarView : JPanel(), CoroutineScope, DirectoryObserver, SettingsObserver
{
    private var bundle = ResourceBundle.getBundle(Constants.LANGUAGE_BUNDLE_PATH, Settings.language.getLocale())
    private val statusLabel = JLabel()
    private val additionalLabel = JLabel()
    private var job = Job()

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO + job

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
        val zipLabel = getZipLabel()
        val hiddenFilesLabel = getHiddenFilesLabel()

        // Add a | separator if both labels are defined
        val labels = listOf(zipLabel, hiddenFilesLabel).filter { it.isNotBlank() }
        additionalLabel.text = labels.joinToString(" | ")
    }

    private fun getHiddenFilesLabel() = if (Settings.showHiddenFiles) bundle.getString("ShowHidden") else ""

    private fun getZipLabel(): String {
        // Check whether inside a zip archive
        val currentZipService = AppState.getZipServiceForDirectory() ?: return ""
        return when (currentZipService.extractionStatus.value) {
            ZipExtractionStatus.IN_PROGRESS -> bundle.getString("UnpackingZip")
            ZipExtractionStatus.DONE -> bundle.getString("InsideZip")
            ZipExtractionStatus.FAILED -> bundle.getString("ErrorUnpacking")
            ZipExtractionStatus.UNDEFINED -> bundle.getString("UnknownUnpacking")
            ZipExtractionStatus.NOT_YET_STARTED -> bundle.getString("UnpackingNotStarted")
        }
    }

    private fun updateStatus(itemsCount: Int?, totalSize: Long?) {
        if (itemsCount == null || totalSize == null) {
            statusLabel.text = "${bundle.getString("Loading")}..."
        } else if (itemsCount == 0) {
            statusLabel.text = bundle.getString("Empty")
        } else if ((itemsCount < 0) || (totalSize < 0)){
            statusLabel.text = bundle.getString("Unknown")
        } else {
            val itemsLabel = bundle.getString("Items")
            val totalSizeLabel = bundle.getString("TotalSize")
            statusLabel.text = "$itemsLabel: $itemsCount, $totalSizeLabel: ${Utils.humanReadableSize(totalSize)}"
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

    override fun onLanguageChanged(newLanguage: Language) {
        // Update the resource bundle
        bundle = ResourceBundle.getBundle(Constants.LANGUAGE_BUNDLE_PATH, newLanguage.getLocale())
        onDirectoryChanged(AppState.currentExplorerDirectory)
    }
}
