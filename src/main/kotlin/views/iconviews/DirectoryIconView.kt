package views.iconviews

import dataModels.ExplorerDirectory
import state.ColorTheme
import views.IconManager
import views.directoryviews.GridDirectoryView

class DirectoryIconView(
    entity: ExplorerDirectory,
    parentDirView: GridDirectoryView,
    colorTheme: ColorTheme
): AbstractIconEntityView(entity, parentDirView, colorTheme) {
    private val dirEntity = entity

    override fun setIcon() {
        iconLabel.icon = if (dirEntity.isEmpty) {
            resizeIcon(IconManager.folderOpenIcon)
        } else {
            resizeIcon(IconManager.folderIcon)
        }
    }
}
