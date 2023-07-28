package views.iconviews

import dataModels.UnknownEntity
import state.ColorTheme
import utils.Utils
import utils.IconManager
import views.directoryviews.GridDirectoryView


class UnknownIconView(
    entity: UnknownEntity,
    parentDirView: GridDirectoryView
): AbstractIconEntityView(entity, parentDirView) {
    // private val unknownEntity = entity

    override fun setIcon() {
        iconLabel.icon = Utils.resizeIcon(IconManager.helpCenterIcon)
    }
}
