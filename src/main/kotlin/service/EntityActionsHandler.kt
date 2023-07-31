package service

import model.*
import state.AppStateUpdater
import state.Settings
import view.popupwindows.showErrorDialog
import java.awt.Desktop
import java.io.File
import java.io.IOException
import java.nio.file.Paths
import java.util.*

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
    private var bundle = ResourceBundle.getBundle(Constants.LANGUAGE_BUNDLE_PATH, Settings.language.getLocale())
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
        AppStateUpdater.updateDirectory(directory)
    }

    private fun openFile(fileEntity: ExplorerFile) {
        if (Desktop.isDesktopSupported()) {
            try {
                Desktop.getDesktop().open(File(fileEntity.path))
            } catch (ex: IOException) {
                ex.printStackTrace()
                showErrorDialog("${bundle.getString("UnableToOpen")} ${fileEntity.path}.")
            }
        } else {
            showErrorDialog(bundle.getString("DesktopNotSupported"))
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
                showErrorDialog("${bundle.getString("CircularLink")} $targetPath")
                return
            }
            visitedSymlinks.add(link.path)
            performEntityAction(targetEntity)
        } catch (e: IOException) {
            showErrorDialog("${bundle.getString("ErrorFollowingLink")} ${e.message}")
        }
    }

    private fun handleZipArchive(zipfile: ZipArchive) {
        AppStateUpdater.updateDirectory(zipfile)
    }

    private fun handleUnknownEntity(unknownEntity: UnknownEntity) {
        showErrorDialog("${bundle.getString("NotSupportedEntity")} ${unknownEntity.path}")
    }
}