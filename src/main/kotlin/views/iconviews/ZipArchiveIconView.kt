package views.iconviews

import dataModels.ZipArchive
import state.ColorTheme
import views.IconManager
import views.directoryviews.IconsDirectoryView


class ZipArchiveIconView(
    entity: ZipArchive,
    parentDirView: IconsDirectoryView,
    colorTheme: ColorTheme
): AbstractIconEntityView(entity, parentDirView, colorTheme) {
    private val zipEntity = entity

    override fun setIcon() {
        iconLabel.icon = resizeIcon(IconManager.folderZipIcon)
    }
}
