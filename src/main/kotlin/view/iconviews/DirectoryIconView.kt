package view.iconviews

import model.ExplorerDirectory
import util.Utils
import util.IconManager

/**
 * Icon view for displaying a directory icon in the DirExplorer.
 */
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
