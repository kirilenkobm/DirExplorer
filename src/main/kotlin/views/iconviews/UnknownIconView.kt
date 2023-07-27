package views.iconviews

import dataModels.UnknownEntity
import state.ColorTheme
import views.IconManager
import views.directoryviews.IconsDirectoryView


class UnknownIconView(
    entity: UnknownEntity,
    parentDirView: IconsDirectoryView,
    colorTheme: ColorTheme
): AbstractIconEntityView(entity, parentDirView, colorTheme) {
    private val unknownEntity = entity

    override fun setIcon() {
        iconLabel.icon = resizeIcon(IconManager.helpCenterIcon)
    }
}
