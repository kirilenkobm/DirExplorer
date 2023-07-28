package utils

import dataModels.ExplorerDirectory
import state.ColorTheme
import state.Settings
import state.SettingsObserver
import state.ViewMode
import java.awt.AlphaComposite
import java.awt.Graphics2D
import java.awt.Image
import java.awt.image.BufferedImage
import javax.swing.ImageIcon

//  that handles all the icons downloaded and provides easy access to them
object IconManager: SettingsObserver {

    init {
        Settings.addObserver(this)
        loadAllIcons()
    }

    lateinit var audioFileIcon: ImageIcon
    lateinit var filterIcon: ImageIcon
    lateinit var folderIcon: ImageIcon
    lateinit var folderOpenIcon: ImageIcon
    lateinit var folderZipIcon: ImageIcon
    lateinit var folderArchiveIcon: ImageIcon
    lateinit var gridViewIcon: ImageIcon
    lateinit var helpCenterIcon: ImageIcon
    lateinit var imageIcon: ImageIcon
    lateinit var linkIcon: ImageIcon
    lateinit var movieIcon: ImageIcon
    lateinit var pdfIcon: ImageIcon
    lateinit var fileIcon: ImageIcon
    lateinit var settingsIcon: ImageIcon
    lateinit var tocIcon: ImageIcon
    lateinit var viewModuleIcon: ImageIcon
    lateinit var homeIcon: ImageIcon
    lateinit var backArrowIcon: ImageIcon
    lateinit var forwardArrowIcon: ImageIcon
    lateinit var upArrowIcon: ImageIcon
    lateinit var chevronRightIcon: ImageIcon
    lateinit var ellipsisIcon: ImageIcon
    lateinit var backSpaceIcon: ImageIcon

    private fun loadImageIcon(path: String): ImageIcon {
        val originalIcon = ImageIcon(javaClass.getResource(path))
        val originalImage: Image = originalIcon.image
        val width = originalImage.getWidth(null)
        val height = originalImage.getHeight(null)
        val bufferedImage = BufferedImage(
            width,
            height,
            BufferedImage.TYPE_INT_ARGB
        )

        val transparency: Float = if (Settings.colorTheme == ColorTheme.DARK) {
            0.65F
        } else {
            0.85F
        }
        val g2d: Graphics2D = bufferedImage.createGraphics()
        g2d.composite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, transparency)
        g2d.drawImage(originalImage, 0, 0, null)
        g2d.dispose()

        if (Settings.colorTheme == ColorTheme.DARK) {
            for (i in 0..<width) {
                for (j in 0..<height) {
                    val rgba = bufferedImage.getRGB(i, j)
                    bufferedImage.setRGB(i, j, rgba xor 0x00ffffff)
                }
            }
        }
        return ImageIcon(bufferedImage)
    }

    private fun loadAllIcons() {
        audioFileIcon = loadImageIcon("/images/baseline_audio_file_black_36dp.png")
        filterIcon = loadImageIcon("/images/baseline_filter_alt_black_36dp.png")
        folderIcon = loadImageIcon("/images/baseline_folder_black_36dp.png")
        folderOpenIcon = loadImageIcon("/images/baseline_folder_open_black_36dp.png")
        folderZipIcon = loadImageIcon("/images/baseline_folder_zip_black_36dp.png")
        folderArchiveIcon = loadImageIcon("/images/baseline_folder_archive_black_36dp.png")
        gridViewIcon = loadImageIcon("/images/baseline_grid_view_black_36dp.png")
        helpCenterIcon = loadImageIcon("/images/baseline_help_center_black_36dp.png")
        imageIcon = loadImageIcon("/images/baseline_image_black_36dp.png")
        linkIcon = loadImageIcon("/images/baseline_link_black_36dp.png")
        movieIcon = loadImageIcon("/images/baseline_movie_black_36dp.png")
        pdfIcon = loadImageIcon("/images/baseline_picture_as_pdf_black_36dp.png")
        fileIcon = loadImageIcon("/images/baseline_text_snippet_black_36dp.png")
        settingsIcon = loadImageIcon("/images/baseline_settings_black_36dp.png")
        tocIcon = loadImageIcon("/images/baseline_toc_black_36dp.png")
        viewModuleIcon = loadImageIcon("/images/baseline_view_module_black_36dp.png")
        homeIcon = loadImageIcon("/images/baseline_home_black_36dp.png")
        backArrowIcon = loadImageIcon("/images/baseline_arrow_back_black_36dp.png")
        forwardArrowIcon = loadImageIcon("/images/baseline_arrow_forward_black_36dp.png")
        upArrowIcon = loadImageIcon("/images/baseline_arrow_upward_black_36dp.png")
        chevronRightIcon = loadImageIcon("/images/baseline_chevron_right_black_18dp.png")
        ellipsisIcon = loadImageIcon("/images/baseline_more_horiz_black_24dp.png")
        backSpaceIcon = loadImageIcon("/images/baseline_backspace_black_18dp.png")
    }

    fun getIconForFileType(fileType: String): ImageIcon {
        return when {
            fileType.startsWith("image/") -> imageIcon
            fileType.startsWith("video/") -> movieIcon
            fileType == "application/pdf" -> pdfIcon
            fileType.startsWith("audio/") -> audioFileIcon
            fileType.startsWith("application/x-") -> folderArchiveIcon
            fileType == "application/gzip" -> folderArchiveIcon
            else -> fileIcon
        }
    }

    fun getIconForDir(dir: ExplorerDirectory): ImageIcon {
        return when {
            dir.isEmpty -> folderOpenIcon
            else -> folderIcon
        }
    }

    override fun onShowHiddenFilesChanged(newShowHiddenFiles: Boolean) {
        // not applicable
    }

    override fun onViewModeChanged(newViewMode: ViewMode) {
        // not applicable
    }

    override fun onColorThemeChanged(newColorTheme: ColorTheme) {
        loadAllIcons()
    }
}
