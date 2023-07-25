package views.iconviews

import dataModels.ExplorerDirectory
import views.IconManager
import views.directoryviews.IconsDirectoryView

class DirectoryIconView(
    entity: ExplorerDirectory,
    parentDirView: IconsDirectoryView
): AbstractIconEntityView(entity, parentDirView) {
    private val dirEntity = entity

    override fun setIcon() {
        iconLabel.icon = if (dirEntity.isEmpty) {
            resizeIcon(IconManager.folderOpenIcon)
        } else {
            resizeIcon(IconManager.folderIcon)
        }
    }
}
