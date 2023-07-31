package service

import model.*
import state.AppState
import state.AppStateUpdater
import view.popupwindows.showErrorDialog
import java.awt.Desktop
import java.io.File
import java.io.IOException
import java.nio.file.Paths

/**
 * Singleton object that handles actions performed on different types of FileSystemEntity instances.
 *
 * This object defines the behavior for mouse click actions on various types of FileSystemEntity instances,
 * such as directories, files, symbolic links, zip archives, and unknown entities.
 * The action performed depends on the type of the FileSystemEntity instance.
 *
 * The Singleton pattern is used here to ensure that there is only one instance of
 * EntityActionsHandler in the application, maintaining a single point of access.
 *
 * visitedSymlinks: A mutable set used to keep track of the symbolic links that have been visited
 * during the execution of the application. This is used to detect and prevent circular links.
 */
object EntityActionsHandler {
    private var visitedSymlinks: MutableSet<String> = mutableSetOf()

    fun performEntityAction(entity: FileSystemEntity) {
        print("Perform entiry action")
        when(entity) {
            is ExplorerDirectory -> handleDirectory(entity)
            is ExplorerFile -> openFile(entity)
            is ExplorerSymLink -> handleSymLink(entity)
            is ZipArchive -> handleZipArchive(entity)
            is UnknownEntity -> handleUnknownEntity(entity)
        }
        visitedSymlinks.clear()
    }

    private fun handleDirectory(directory: ExplorerDirectory) {
        AppStateUpdater.updateDirectory(directory)
    }

    private fun openFile(fileEntity: ExplorerFile) {
        if (Desktop.isDesktopSupported()) {
            try {
                Desktop.getDesktop().open(File(fileEntity.path))
            } catch (ex: IOException) {
                ex.printStackTrace()
                showErrorDialog("Error: Unable to open the file at ${fileEntity.path}.")
            }
        } else {
            showErrorDialog("Error: Desktop operations are not supported on this system.")
        }
    }

    private fun handleSymLink(link: ExplorerSymLink) {
        try {
            // Sometimes links have relative paths instead of absolute
            val targetPath = Paths.get(link.path)
                .parent.resolve(link.target)
                .toAbsolutePath()
                .normalize()
                .toString()
            val targetEntity = FileSystemEntityFactory.createEntity(targetPath)
            if (link.path in visitedSymlinks) {
                showErrorDialog("Circular link detected: $targetPath")
                return
            }
            visitedSymlinks.add(link.path)
            performEntityAction(targetEntity)
        } catch (e: IOException) {
            showErrorDialog("Error following symlink: ${e.message}")
        }
    }

    private fun handleZipArchive(zipfile: ZipArchive) {
        AppStateUpdater.updateDirectory(zipfile)
    }

    private fun handleUnknownEntity(unknownEntity: UnknownEntity) {
        showErrorDialog("Not supported file system entity ${unknownEntity.path}")
    }
}