package services

import dataModels.*
import state.AppState
import views.popupwindows.showErrorDialog
import java.awt.Desktop
import java.io.File
import java.io.IOException

/**
For each entity, defines the action on mouse click.
@param entity: clicked FileSystemEntity, such as
Directory, Regular File, Archive, etc.

 Hopefully singleton pattern applies here.
 */
object EntityActionsHandler {
    private var visitedSymlinks: MutableSet<String> = mutableSetOf()

    fun performEntityAction(entity: FileSystemEntity) {
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
        AppState.updateDirectory(directory)
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
            val targetPath = link.target
            val targetEntity = FileSystemEntityFactory.createEntity(targetPath)
            visitedSymlinks.add(link.path)

            if (targetPath in visitedSymlinks) {
                showErrorDialog("Circular link detected: $targetPath")
                return
            }

            visitedSymlinks.add(targetPath)
            performEntityAction(targetEntity)
        } catch (e: IOException) {
            showErrorDialog("Error following symlink: ${e.message}")
        }
    }

    private fun handleZipArchive(zipfile: ZipArchive) {
        AppState.updateDirectory(zipfile)
    }

    private fun handleUnknownEntity(unknownEntity: UnknownEntity) {
        showErrorDialog("Not supported file system entity ${unknownEntity.path}")
    }
}