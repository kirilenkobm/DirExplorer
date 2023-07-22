package views

import dataModels.ExplorerDirectory
import javax.swing.ImageIcon

//  that handles all the icons downloaded and provides an easy access to them
object IconManager {
    val audioFileIcon: ImageIcon = loadImageIcon("/images/baseline_audio_file_black_36dp.png")
    val filterIcon: ImageIcon = loadImageIcon("/images/baseline_filter_alt_black_36dp.png")
    val folderIcon: ImageIcon = loadImageIcon("/images/baseline_folder_black_36dp.png")
    val folderOpenIcon: ImageIcon = loadImageIcon("/images/baseline_folder_open_black_36dp.png")
    val folderZipIcon: ImageIcon = loadImageIcon("/images/baseline_folder_zip_black_36dp.png")
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


    private fun loadImageIcon(path: String): ImageIcon {
        return ImageIcon(javaClass.getResource(path))
    }

    fun getIconForFileType(fileType: String): ImageIcon {
        return when {
            fileType.startsWith("image/") -> imageIcon
            fileType.startsWith("video/") -> movieIcon
            fileType == "application/pdf" -> pdfIcon
            fileType.startsWith("audio/") -> audioFileIcon
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
