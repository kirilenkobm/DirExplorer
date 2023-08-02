package service

import model.*
import view.iconviews.*
import javax.swing.JPanel

/**
 * Singleton factory for creating JPanel views for different types of file system entities.
 *
 * This factory provides a method to create a JPanel view for a given FileSystemEntity.
 * The type of view created depends on the type of the FileSystemEntity:
 * - ExplorerFile entities are represented with a FileIconView.
 * - ExplorerDirectory entities are represented with a DirectoryIconView.
 * - ExplorerSymLink entities are represented with a SymlinkIconView.
 * - ZipArchive entities are represented with a ZipArchiveIconView.
 * - Any other type of FileSystemEntity is represented with an UnknownIconView.
 *
 * For ExplorerFile entities, the factory also adds the created FileIconView to the ThumbnailsJobsManager,
 * which manages the cancellation of thumbnail generation tasks.
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
        ThumbnailJobController.addFileIconView(view)
        return view.createView()
    }

    private fun createUnknownIconView(entity: FileSystemEntity): JPanel {
        val unknownEntity = UnknownEntity(entity.path)
        return UnknownIconView(unknownEntity).createView()
    }

    // TODO: It is a very dirty workaround
    fun makeZipLoadingSpinner(): JPanel {
        val mockEntity = UnknownEntity("Unpacking zip...")
        return UnpackingZipIconView(mockEntity).createView()
    }
}
