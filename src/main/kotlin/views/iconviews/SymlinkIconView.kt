package views.iconviews

import dataModels.ExplorerSymLink
import utils.Utils
import utils.IconManager

class SymlinkIconView(
    entity: ExplorerSymLink,
): AbstractIconEntityView(entity) {
    // private val linkEntity = entity

    override fun setIcon() {
        iconLabel.icon = Utils.resizeIcon(IconManager.linkIcon)
    }
}
