package views.iconviews

import dataModels.ZipArchive
import views.IconManager
import views.directoryviews.IconsDirectoryView


class ZipArchiveIconView(
    entity: ZipArchive,
    parentDirView: IconsDirectoryView):
    AbstractIconEntityView(entity, parentDirView) {
    private val zipEntity = entity

    override fun setIcon() {
        iconLabel.icon = resizeIcon(IconManager.folderZipIcon)
    }
}
