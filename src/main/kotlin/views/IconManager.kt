package views

import dataModels.ExplorerDirectory
import state.ColorTheme
import state.Settings
import java.awt.AlphaComposite
import java.awt.Graphics2D
import java.awt.Image
import java.awt.image.BufferedImage
import javax.swing.ImageIcon

//  that handles all the icons downloaded and provides easy access to them
object IconManager {

    val audioFileIcon: ImageIcon = loadImageIcon("/images/baseline_audio_file_black_36dp.png")
    val filterIcon: ImageIcon = loadImageIcon("/images/baseline_filter_alt_black_36dp.png")
    val folderIcon: ImageIcon = loadImageIcon("/images/baseline_folder_black_36dp.png")
    val folderOpenIcon: ImageIcon = loadImageIcon("/images/baseline_folder_open_black_36dp.png")
    val folderZipIcon: ImageIcon = loadImageIcon("/images/baseline_folder_zip_black_36dp.png")
    val folderArchiveIcon: ImageIcon = loadImageIcon("/images/baseline_folder_archive_black_36dp.png")
    val gridViewIcon: ImageIcon = loadImageIcon("/images/baseline_grid_view_black_36dp.png")
    val helpCenterIcon: ImageIcon = loadImageIcon("/images/baseline_help_center_black_36dp.png")
    val imageIcon: ImageIcon = loadImageIcon("/images/baseline_image_black_36dp.png")
    val linkIcon: ImageIcon = loadImageIcon("/images/baseline_link_black_36dp.png")
    val movieIcon: ImageIcon = loadImageIcon("/images/baseline_movie_black_36dp.png")
    val pdfIcon: ImageIcon = loadImageIcon("/images/baseline_picture_as_pdf_black_36dp.png")
    var fileIcon: ImageIcon = loadImageIcon("/images/baseline_text_snippet_black_36dp.png")
    val settingsIcon: ImageIcon = loadImageIcon("/images/baseline_settings_black_36dp.png")
    val tocIcon: ImageIcon = loadImageIcon("/images/baseline_toc_black_36dp.png")
    val viewModuleIcon: ImageIcon = loadImageIcon("/images/baseline_view_module_black_36dp.png")
    val homeIcon: ImageIcon = loadImageIcon("/images/baseline_home_black_36dp.png")
    val backArrowIcon: ImageIcon = loadImageIcon("/images/baseline_arrow_back_black_36dp.png")
    val forwardArrowIcon: ImageIcon = loadImageIcon("/images/baseline_arrow_forward_black_36dp.png")
    val upArrowIcon: ImageIcon = loadImageIcon("/images/baseline_arrow_upward_black_36dp.png")
    var chevronRightIcon: ImageIcon = loadImageIcon("/images/baseline_chevron_right_black_18dp.png")
    var ellipsisIcon: ImageIcon = loadImageIcon("/images/baseline_more_horiz_black_24dp.png")
    var backSpaceIcon: ImageIcon = loadImageIcon("/images/baseline_backspace_black_18dp.png")

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
            0.8F
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
}
