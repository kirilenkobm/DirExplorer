package views.iconviews

import dataModels.ExplorerSymLink
import views.IconManager

class SymlinkIconView(entity: ExplorerSymLink): AbstractIconEntityView(entity) {
    private val linkEntity = entity

    override fun setIcon() {
        iconLabel.icon = resizeIcon(IconManager.linkIcon)
    }
}
