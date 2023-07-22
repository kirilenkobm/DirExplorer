package views.iconviews

import dataModels.UnknownEntity
import views.IconManager

class UnknownIconView(entity: UnknownEntity): AbstractIconEntityView(entity) {
    private val unknownEntity = entity

    override fun setIcon() {
        iconLabel.icon = resizeIcon(IconManager.helpCenterIcon)
    }
}
