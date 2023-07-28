package views.iconviews

import dataModels.ExplorerSymLink
import state.ColorTheme
import utils.Utils
import utils.IconManager
import views.directoryviews.GridDirectoryView

class SymlinkIconView(
    entity: ExplorerSymLink,
    parentDirView: GridDirectoryView
): AbstractIconEntityView(entity, parentDirView) {
    // private val linkEntity = entity

    override fun setIcon() {
        iconLabel.icon = Utils.resizeIcon(IconManager.linkIcon)
    }
}
