package service

import model.FileSystemEntity
import model.*
import util.IconManager
import util.Utils
import javax.swing.ImageIcon

/**
 * Class responsible for mapping file system entities to a format suitable for table display mode.
 *
 * This class takes a DirectoryContentService as input, which it uses to generate a list of FileSystemEntity objects.
 * These entities are then mapped to an array of objects, each representing a row in the table.
 * The mapping process varies depending on the type of the entity.
 * Each entity is mapped to an array containing its icon (resized to fit the table),
 * name, size and last modified date, if applicable.
 *
 * The resulting pair of lists (one of the original entities and one of the corresponding table rows)
 * is then returned by the getContentForView method.
 */
class TableItemsMapper(private val contentService: DirectoryContentService) {
    // Has to be run in the coroutine scope because of the generateContentForView()
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
