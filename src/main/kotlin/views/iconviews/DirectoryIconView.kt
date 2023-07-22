package views.iconviews

import dataModels.ExplorerDirectory
import state.AppState
import views.IconManager
import views.directoryviews.DirectoryViewUpdater
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent

class DirectoryIconView(
    entity: ExplorerDirectory,
    private val updater: DirectoryViewUpdater
): AbstractIconEntityView(entity) {
    private val dirEntity = entity

    override fun setIcon() {
        iconLabel.icon = resizeIcon(IconManager.folderIcon)
    }

    init {
        entityPanel.addMouseListener(object : MouseAdapter() {
            override fun mouseClicked(e: MouseEvent) {
                AppState.updateDirectory(dirEntity)
                updater.updateView()
                updater.updateTobBarView()
            }
        })
    }
}