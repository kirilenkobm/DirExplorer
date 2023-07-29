package services

import dataModels.FileSystemEntity
import dataModels.*
import utils.IconManager
import utils.Utils
import javax.swing.ImageIcon


/**
 * Class that handles content for table.
 * Extracts entities from current content Service and maps
 * them to table model.
 */
class TableDirectoryController(private val contentService: DirectoryContentService) {
    suspend fun getContentForView(): Pair<List<FileSystemEntity>, List<Array<Any>>> {
        val contents = contentService.generateContentForView()
        return Pair(contents, contents.map { mapEntityToData(it) }.toList())
    }

    private fun mapEntityToData(entity: FileSystemEntity): Array<Any> {
        return when (entity) {
            is ExplorerFile -> mapExplorerFile(entity)
            is ExplorerDirectory -> mapExplorerDirectory(entity)
            is ExplorerSymLink -> mapExplorerSymLink(entity)
            is ZipArchive -> mapZipArchive(entity)
            else -> mapUnknownEntity(entity)
        }
    }

    private fun mapExplorerFile(entity: ExplorerFile) = arrayOf<Any>(
        resizeTableIcon(IconManager.getIconForFileType(entity.fileType)),
        entity.name,
        Utils.humanReadableSize(entity.size),
        Utils.formatDate(entity.lastModified)
    )

    private fun mapExplorerDirectory(entity: ExplorerDirectory) = arrayOf<Any>(
        resizeTableIcon(IconManager.getIconForDir(entity)),
        entity.name,
        "-",
        Utils.formatDate(entity.lastModified)
    )

    private fun mapExplorerSymLink(entity: ExplorerSymLink) = arrayOf<Any>(
        resizeTableIcon(IconManager.linkIcon),
        entity.name,
        "-",
        Utils.formatDate(entity.lastModified)
    )

    private fun mapZipArchive(entity: ZipArchive) = arrayOf<Any>(
        resizeTableIcon(IconManager.folderZipIcon),
        entity.name,
        Utils.humanReadableSize(entity.size),
        Utils.formatDate(entity.lastModified)
    )

    private fun mapUnknownEntity(entity: FileSystemEntity) = arrayOf<Any>(
        resizeTableIcon(IconManager.helpCenterIcon),
        entity.name,
        Utils.humanReadableSize(entity.size),
        Utils.formatDate(entity.lastModified)
    )

    private fun resizeTableIcon(icon: ImageIcon, size: Int = Constants.TABLE_ICON_SIZE): ImageIcon {
        val image = icon.image
        val newImage = image.getScaledInstance(size, size, java.awt.Image.SCALE_SMOOTH)
        return ImageIcon(newImage)
    }
}
