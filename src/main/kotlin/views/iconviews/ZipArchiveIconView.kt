package views.iconviews

import dataModels.ZipArchive
import state.AppState
import views.IconManager
import views.directoryviews.IconsDirectoryView
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent

class ZipArchiveIconView(
    entity: ZipArchive,
    parentDirView: IconsDirectoryView):
    AbstractIconEntityView(entity, parentDirView) {
    private val zipEntity = entity

    override fun setIcon() {
        iconLabel.icon = resizeIcon(IconManager.folderZipIcon)
    }

    init {
        entityPanel.addMouseListener(object : MouseAdapter() {
            override fun mouseClicked(e: MouseEvent) {
                AppState.updateDirectory(zipEntity)
            }
        })
    }
}
