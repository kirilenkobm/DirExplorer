package views.directoryviews

import dataModels.*
import kotlinx.coroutines.launch
import state.AppState
import views.*
import views.iconviews.DirectoryIconView
import views.iconviews.FileIconView
import views.iconviews.SymlinkIconView
import views.iconviews.UnknownIconView
import java.awt.GridLayout
import javax.swing.ImageIcon
import javax.swing.JPanel
import javax.swing.SwingUtilities


class IconsDirectoryView(private val topBarView: TopBarView) : AbstractDirectoryView(), DirectoryViewUpdater {
    private val panel = JPanel(GridLayout(0, 5)) // TODO: adaptive number of columns
    private var filteredAndSortedContents: List<FileSystemEntity> = emptyList()

    init {
        updateView()
    }

    override fun updateTobBarView() {
        topBarView.updateView()
    }

    private fun resizeIcon(icon: ImageIcon, size: Int): ImageIcon {
        val image = icon.image
        val newImage = image.getScaledInstance(size, size, java.awt.Image.SCALE_FAST)
        return ImageIcon(newImage)
    }

    private fun createEntityView(entity: FileSystemEntity): JPanel {
        return when (entity) {
            is ExplorerFile -> FileIconView(entity).createView()
            is ExplorerDirectory -> DirectoryIconView(entity, this).createView()
            is ExplorerSymLink -> SymlinkIconView(entity).createView()
            else -> {
                // To handle type mismatch in else branch:
                // UnknownEntity handles all other possible cases

                val unknownEntity = UnknownEntity(entity.path)
                UnknownIconView(unknownEntity).createView()
            }
        }
    }

    override fun updateView() {
        launch {
            currentContents = AppState.currentExplorerDirectory.getContents()
            filteredAndSortedContents = filterAndSortContents(currentContents)

            SwingUtilities.invokeLater {
                panel.removeAll()
                for (entity in filteredAndSortedContents) {
                    panel.add(createEntityView(entity))
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
