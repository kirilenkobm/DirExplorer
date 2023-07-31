package view.iconviews

import model.UnknownEntity
import util.Utils
import util.IconManager

/**
 * Icon view for displaying an unknown entity icon in the DirExplorer in the grid view mode.
 */
class UnknownIconView(
    entity: UnknownEntity,
): AbstractIconEntityView(entity) {
    // private val unknownEntity = entity

    override fun setIcon() {
        iconLabel.icon = Utils.resizeIcon(IconManager.helpCenterIcon)
    }
}
