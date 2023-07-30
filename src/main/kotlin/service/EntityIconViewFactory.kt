package service

import model.*
import view.iconviews.*
import javax.swing.JPanel

/**
 * Factory to create views for different
 * kinds of file system entities.
 */
object EntityIconViewFactory {
    fun createEntityView(entity: FileSystemEntity): JPanel {
        return when (entity) {
            is ExplorerFile -> createFileIconView(entity)
            is ExplorerDirectory -> DirectoryIconView(entity).createView()
            is ExplorerSymLink -> SymlinkIconView(entity).createView()
            is ZipArchive -> ZipArchiveIconView(entity).createView()
            else -> createUnknownIconView(entity)
        }
    }

    private fun createFileIconView(entity: ExplorerFile): JPanel {
        val view = FileIconView(entity)
        ThumbnailsJobsManager.addFileIconView(view)
        return view.createView()
    }

    private fun createUnknownIconView(entity: FileSystemEntity): JPanel {
        val unknownEntity = UnknownEntity(entity.path)
        return UnknownIconView(unknownEntity).createView()
    }
}
