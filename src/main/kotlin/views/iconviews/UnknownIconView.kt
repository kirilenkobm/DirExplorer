package views.iconviews

import dataModels.UnknownEntity
import utils.Utils
import utils.IconManager


class UnknownIconView(
    entity: UnknownEntity,
): AbstractIconEntityView(entity) {
    // private val unknownEntity = entity

    override fun setIcon() {
        iconLabel.icon = Utils.resizeIcon(IconManager.helpCenterIcon)
    }
}
