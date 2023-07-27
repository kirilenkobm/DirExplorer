package views.iconviews

import dataModels.ExplorerSymLink
import state.ColorTheme
import views.IconManager
import views.directoryviews.IconsDirectoryView

class SymlinkIconView(
    entity: ExplorerSymLink,
    parentDirView: IconsDirectoryView,
    colorTheme: ColorTheme
): AbstractIconEntityView(entity, parentDirView, colorTheme) {
    private val linkEntity = entity

    override fun setIcon() {
        iconLabel.icon = resizeIcon(IconManager.linkIcon)
    }
}
