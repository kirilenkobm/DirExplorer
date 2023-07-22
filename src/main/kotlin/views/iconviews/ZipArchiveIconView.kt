package views.iconviews

import dataModels.ZipArchive
import views.IconManager

class ZipArchiveIconView(entity: ZipArchive): AbstractIconEntityView(entity) {
    private val zipEntity = entity

    override fun setIcon() {
        iconLabel.icon = resizeIcon(IconManager.folderZipIcon)
    }
}
