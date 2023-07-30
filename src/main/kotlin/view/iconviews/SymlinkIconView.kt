package view.iconviews

import model.ExplorerSymLink
import util.Utils
import util.IconManager

class SymlinkIconView(
    entity: ExplorerSymLink,
): AbstractIconEntityView(entity) {
    // private val linkEntity = entity

    override fun setIcon() {
        iconLabel.icon = Utils.resizeIcon(IconManager.linkIcon)
    }
}
