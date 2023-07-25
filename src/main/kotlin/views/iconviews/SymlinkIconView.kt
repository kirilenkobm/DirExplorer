package views.iconviews

import dataModels.ExplorerSymLink
import views.IconManager
import views.directoryviews.IconsDirectoryView

class SymlinkIconView(entity: ExplorerSymLink, parentDirView: IconsDirectoryView): AbstractIconEntityView(entity, parentDirView) {
    private val linkEntity = entity

    override fun setIcon() {
        iconLabel.icon = resizeIcon(IconManager.linkIcon)
    }
}
