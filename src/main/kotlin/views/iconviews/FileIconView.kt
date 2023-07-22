package views.iconviews

import dataModels.ExplorerFile
import views.IconManager
import java.awt.Desktop
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.io.File
import java.io.IOException


class FileIconView(entity: ExplorerFile): AbstractIconEntityView(entity) {
    private val fileEntity = entity

    override fun setIcon() {
        // TODO: control the same size in the abstract class
        iconLabel.icon = resizeIcon(IconManager.getIconForFileType(fileEntity.fileType))
    }

    init {
        entityPanel.addMouseListener(object : MouseAdapter() {
            override fun mouseClicked(e: MouseEvent) {
                if (Desktop.isDesktopSupported()) {
                    try {
                        Desktop.getDesktop().open(File(entity.path))
                    } catch (ex: IOException) {
                        ex.printStackTrace()
                        println("TODO: come up with error ")
                    }
                }
            }
        })
    }
}