package view.iconviews

import model.ExplorerSymLink
import util.Utils
import util.IconManager

/**
 * Icon view for displaying a symlink icon in the DirExplorer in the grid view mode.
 */
class SymlinkIconView(
    entity: ExplorerSymLink,
): AbstractIconEntityView(entity) {
    // private val linkEntity = entity

    override fun setIcon() {
        iconLabel.icon = Utils.resizeIcon(IconManager.linkIcon)
    }
}
