package views.iconviews

import dataModels.ExplorerSymLink
import state.ColorTheme
import views.IconManager
import views.directoryviews.GridDirectoryView

class SymlinkIconView(
    entity: ExplorerSymLink,
    parentDirView: GridDirectoryView,
    colorTheme: ColorTheme
): AbstractIconEntityView(entity, parentDirView, colorTheme) {
    private val linkEntity = entity

    override fun setIcon() {
        iconLabel.icon = resizeIcon(IconManager.linkIcon)
    }
}
