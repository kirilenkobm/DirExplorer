package view.iconviews

import model.ZipArchive
import util.Utils
import util.IconManager


class ZipArchiveIconView(
    entity: ZipArchive,
): AbstractIconEntityView(entity) {
    // private val zipEntity = entity

    override fun setIcon() {
        iconLabel.icon = Utils.resizeIcon(IconManager.folderZipIcon)
    }
}
