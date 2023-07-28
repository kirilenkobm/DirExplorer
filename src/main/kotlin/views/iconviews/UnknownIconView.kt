package views.iconviews

import dataModels.UnknownEntity
import state.ColorTheme
import utils.Utils
import views.IconManager
import views.directoryviews.GridDirectoryView


class UnknownIconView(
    entity: UnknownEntity,
    parentDirView: GridDirectoryView,
    colorTheme: ColorTheme
): AbstractIconEntityView(entity, parentDirView, colorTheme) {
    private val unknownEntity = entity

    override fun setIcon() {
        iconLabel.icon = Utils.resizeIcon(IconManager.helpCenterIcon)
    }
}
