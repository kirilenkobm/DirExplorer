package views.iconviews

import dataModels.ExplorerDirectory
import state.ColorTheme
import utils.Utils
import utils.IconManager
import views.directoryviews.GridDirectoryView

class DirectoryIconView(
    entity: ExplorerDirectory,
    parentDirView: GridDirectoryView
): AbstractIconEntityView(entity, parentDirView) {
    private val dirEntity = entity

    override fun setIcon() {
        iconLabel.icon = if (dirEntity.isEmpty) {
            Utils.resizeIcon(IconManager.folderOpenIcon)
        } else {
            Utils.resizeIcon(IconManager.folderIcon)
        }
    }
}
