package view.iconviews

import model.ZipArchive
import util.Utils
import util.IconManager

/**
 * Icon view for displaying a zip archive icon in the DirExplorer in the grid view mode.
 */
class ZipArchiveIconView(
    entity: ZipArchive,
): AbstractIconEntityView(entity) {
    // private val zipEntity = entity

    override fun setIcon() {
        iconLabel.icon = Utils.resizeIcon(IconManager.folderZipIcon)
    }
}
