package views.iconviews

import dataModels.ExplorerDirectory
import state.ColorTheme
import state.ViewMode
import views.IconManager
import views.directoryviews.IconsDirectoryView

class DirectoryIconView(
    entity: ExplorerDirectory,
    parentDirView: IconsDirectoryView,
    colorTheme: ColorTheme
): AbstractIconEntityView(entity, parentDirView, colorTheme) {
    private val dirEntity = entity

    override fun setIcon() {
        iconLabel.icon = if (dirEntity.isEmpty) {
            resizeIcon(IconManager.folderOpenIcon)
        } else {
            resizeIcon(IconManager.folderIcon)
        }
    }
}
