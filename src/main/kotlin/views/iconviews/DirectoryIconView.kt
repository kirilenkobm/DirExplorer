package views.iconviews

import dataModels.ExplorerDirectory
import utils.Utils
import utils.IconManager

class DirectoryIconView(
    entity: ExplorerDirectory,
): AbstractIconEntityView(entity) {
    private val dirEntity = entity

    override fun setIcon() {
        iconLabel.icon = if (dirEntity.isEmpty) {
            Utils.resizeIcon(IconManager.folderOpenIcon)
        } else {
            Utils.resizeIcon(IconManager.folderIcon)
        }
    }
}
