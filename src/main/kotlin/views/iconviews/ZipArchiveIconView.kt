package views.iconviews

import dataModels.ZipArchive
import state.ColorTheme
import utils.Utils
import utils.IconManager
import views.directoryviews.GridDirectoryView


class ZipArchiveIconView(
    entity: ZipArchive,
    parentDirView: GridDirectoryView,
    colorTheme: ColorTheme
): AbstractIconEntityView(entity, parentDirView, colorTheme) {
    private val zipEntity = entity

    override fun setIcon() {
        iconLabel.icon = Utils.resizeIcon(IconManager.folderZipIcon)
    }
}
