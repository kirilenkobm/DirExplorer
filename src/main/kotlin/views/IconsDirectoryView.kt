package views

import dataModels.*
import kotlinx.coroutines.launch
import state.AppState
import state.SortOrder
import java.awt.BorderLayout
import java.awt.Desktop
import java.awt.GridLayout
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.io.File
import java.io.IOException
import javax.swing.BoxLayout
import javax.swing.ImageIcon
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.SwingUtilities

class IconsDirectoryView(private val topBarView: TopBarView) : AbstractDirectoryView() {
    private val panel = JPanel(GridLayout(0, 5)) // TODO: adaptive number of columns
    private var filteredAndSortedContents: List<FileSystemEntity> = emptyList()

    init {
        updateView()
    }


    fun resizeIcon(icon: ImageIcon, size: Int): ImageIcon {
        val image = icon.image
        val newImage = image.getScaledInstance(size, size, java.awt.Image.SCALE_SMOOTH)
        return ImageIcon(newImage)
    }

    override fun updateView() {
        launch {
            currentContents = AppState.currentExplorerDirectory.getContents()
            filteredAndSortedContents = filterAndSortContents(currentContents)

            SwingUtilities.invokeLater {
                panel.removeAll()
                for (entity in filteredAndSortedContents) {
                    val iconLabel = JLabel()
                    val textLabel = JLabel(entity.name)
                    when (entity) {
                        is ExplorerFile -> iconLabel.icon = resizeIcon(IconManager.getIconForFileType(entity.fileType), 64)
                        is ExplorerDirectory -> iconLabel.icon = resizeIcon(IconManager.folderIcon, 64)
                        is ExplorerSymLink -> iconLabel.icon = resizeIcon(IconManager.linkIcon, 64)
                        is UnknownEntity -> iconLabel.icon = resizeIcon(IconManager.helpCenterIcon, 64)
                        else -> iconLabel.icon = resizeIcon(IconManager.helpCenterIcon, 64)
                    }
                    val entityPanel = JPanel()
                    entityPanel.layout = BoxLayout(entityPanel, BoxLayout.Y_AXIS)
                    entityPanel.add(iconLabel)
                    entityPanel.add(textLabel)


                    // Add a MouseListener to the panel
                    entityPanel.addMouseListener(object : MouseAdapter() {
                        override fun mouseClicked(e: MouseEvent) {
                            if (entity is ExplorerDirectory) {
                                AppState.updateDirectory(entity)
                                updateView()
                                topBarView.updateView()
                            } else if (entity is ExplorerFile) {
                                if (Desktop.isDesktopSupported()) {
                                    try {
                                        Desktop.getDesktop().open(File(entity.path))
                                    } catch (ex: IOException) {
                                        ex.printStackTrace()
                                        println("Error! Cannot open ....")
                                        // TODO: show error message
                                    }
                                }
                            }
                        }
                    })

                    panel.add(entityPanel)
                }
                panel.revalidate()
                panel.repaint()
            }
        }
    }


    fun getPanel(): JPanel {
        return panel
    }
}
