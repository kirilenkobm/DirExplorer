package views.iconviews

import dataModels.UnknownEntity
import views.IconManager
import views.directoryviews.IconsDirectoryView


class UnknownIconView(entity: UnknownEntity, parentDirView: IconsDirectoryView): AbstractIconEntityView(entity, parentDirView) {
    private val unknownEntity = entity

    override fun setIcon() {
        iconLabel.icon = resizeIcon(IconManager.helpCenterIcon)
    }
}
