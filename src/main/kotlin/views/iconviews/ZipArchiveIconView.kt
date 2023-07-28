package views.iconviews

import dataModels.ZipArchive
import state.ColorTheme
import views.IconManager
import views.directoryviews.GridDirectoryView


class ZipArchiveIconView(
    entity: ZipArchive,
    parentDirView: GridDirectoryView,
    colorTheme: ColorTheme
): AbstractIconEntityView(entity, parentDirView, colorTheme) {
    private val zipEntity = entity

    override fun setIcon() {
        iconLabel.icon = resizeIcon(IconManager.folderZipIcon)
    }
}
