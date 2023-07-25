package views.iconviews

import dataModels.UnknownEntity
import views.IconManager
import views.directoryviews.IconsDirectoryView
import views.showErrorDialog
import java.awt.Desktop
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.io.File
import java.io.IOException

class UnknownIconView(entity: UnknownEntity, parentDirView: IconsDirectoryView): AbstractIconEntityView(entity, parentDirView) {
    private val unknownEntity = entity

    init {
        entityPanel.addMouseListener(object : MouseAdapter() {
            override fun mouseClicked(e: MouseEvent) {
                showErrorDialog("Not supported file system entity")
            }
        })
    }

    override fun setIcon() {
        iconLabel.icon = resizeIcon(IconManager.helpCenterIcon)
    }
}
