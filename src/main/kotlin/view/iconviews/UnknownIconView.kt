package view.iconviews

import model.UnknownEntity
import util.Utils
import util.IconManager


class UnknownIconView(
    entity: UnknownEntity,
): AbstractIconEntityView(entity) {
    // private val unknownEntity = entity

    override fun setIcon() {
        iconLabel.icon = Utils.resizeIcon(IconManager.helpCenterIcon)
    }
}
