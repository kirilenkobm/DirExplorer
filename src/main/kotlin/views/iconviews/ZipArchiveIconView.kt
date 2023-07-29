package views.iconviews

import dataModels.ZipArchive
import utils.Utils
import utils.IconManager


class ZipArchiveIconView(
    entity: ZipArchive,
): AbstractIconEntityView(entity) {
    // private val zipEntity = entity

    override fun setIcon() {
        iconLabel.icon = Utils.resizeIcon(IconManager.folderZipIcon)
    }
}
