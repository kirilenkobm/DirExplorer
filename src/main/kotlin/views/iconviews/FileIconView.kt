package views.iconviews

import dataModels.ExplorerFile
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Semaphore
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.rendering.PDFRenderer
import state.Settings
import views.IconManager
import views.directoryviews.IconsDirectoryView
import java.awt.Color
import java.awt.RenderingHints
import java.awt.image.BufferedImage
import java.io.File
import java.io.IOException
import javax.imageio.ImageIO
import javax.imageio.metadata.IIOMetadataFormatImpl
import javax.imageio.metadata.IIOMetadataNode
import javax.swing.Icon
import javax.swing.ImageIcon
import javax.swing.SwingUtilities
import kotlin.coroutines.CoroutineContext
import kotlin.math.max


class FileIconView(
    entity: ExplorerFile,
    parentDirView: IconsDirectoryView,
    private val imagePreviewsSemaphore: Semaphore,
    private val textPreviewsSemaphore: Semaphore
): AbstractIconEntityView(entity, parentDirView), CoroutineScope {
    private val fileEntity = entity
    private val job = Job()
    private val iconCache = IconsCache
    private val textFileExtensionsNotInMime = setOf(
        "java", "py", "kt", "js",
        "html", "css", "c", "cpp",
        "h", "hpp", "go", "rs",
        "rb", "log", "iml", "bat",
        "kts", "csh", "sh", "fasta",
        "fa", "fastq", "nf", "json",
        "swift"
    )


    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    override fun setIcon() {
        // default base image
        iconLabel.icon = resizeIcon(IconManager.getIconForFileType(fileEntity.fileType))
        startThumbnailGeneration()
    }

    private fun startThumbnailGeneration() {
        // if it's image: try to replace icon with thumbnail
        // also check whether is supported by ImageIO
        val fileType = fileEntity.fileType
        val fileExtension = fileEntity.extension
        // to handle cases like file.424.122.JPG
        val correctedExtension = fileExtension.split(".").last().lowercase()

        if (fileType.startsWith("image/") && ImageIO.getReaderFileSuffixes().contains(correctedExtension)) {
            launch(Dispatchers.IO) {
                imagePreviewsSemaphore.acquire()
                try {
                    var thumbnail: Icon? = iconCache[fileEntity.path]
                    if (thumbnail == null) {
                        thumbnail = createThumbnail(fileEntity.path)
                        if (thumbnail != null) {
                            iconCache[fileEntity.path] = thumbnail
                        }
                    }
                    if (thumbnail != null) {
                        // If thumbnail created successfully -> apply it
                        SwingUtilities.invokeLater {
                            // iconLabel.icon = resizeIcon(thumbnail)
                            // it is already resized, aspect ratio preserved
                            iconLabel.icon = thumbnail
                        }
                    }
                } finally {
                    // to ensure that semaphore is always released
                    imagePreviewsSemaphore.release() // Release the permit
                }
            }
        } else if (fileType.startsWith("text/") || textFileExtensionsNotInMime.contains(fileExtension)) {
            launch(Dispatchers.IO) {
                textPreviewsSemaphore.acquire()
                try {
                    val previewText = createTextPreview(fileEntity.path)
                    if (!previewText.isNullOrEmpty()) {
                        // if empty -> no reason to show a preview
                        val iconText = createTextIcon(previewText)
                        SwingUtilities.invokeLater {
                            iconLabel.icon = iconText
                        }
                    }
                } finally {
                    textPreviewsSemaphore.release()
                }
            }
        }  else if (fileType == "application/pdf") {
            // TODO: encapsulate in another method
            launch(Dispatchers.IO) {
                imagePreviewsSemaphore.acquire()
                try {
                    var thumbnail: Icon? = iconCache[fileEntity.path]
                    if (thumbnail == null) {
                        val document = PDDocument.load(File(fileEntity.path))
                        val pdfRenderer = PDFRenderer(document)
                        val image: BufferedImage = pdfRenderer.renderImageWithDPI(0, 40f)
                        document.close()
                        thumbnail = resizeIcon(ImageIcon(image))
                    }
                    val safeThumbnail: Icon = thumbnail!!
                    iconCache[fileEntity.path] = safeThumbnail

                    SwingUtilities.invokeLater {
                        iconLabel.icon = thumbnail
                    }
                } finally {
                    imagePreviewsSemaphore.release()
                }
            }
        }
    }

    private fun createTextIcon(previewText: String): ImageIcon {
        // Create an image containing text
        val image = BufferedImage(
            Settings.iconSize,
            Settings.iconSize,
            BufferedImage.TYPE_INT_ARGB
        )
        val g = image.createGraphics()

        // Draw a white rectangle
        g.color = Color.WHITE
        g.fillRect(0, 0, Settings.iconSize, Settings.iconSize)

        // Draw the text
        g.color = Color.BLACK
        g.font = g.font.deriveFont(6f)

        val lines = previewText.split("\n")
        for ((index, line) in lines.withIndex()) {
            // I found only a way to draw the text line by line
            g.drawString(line, 4, 12 + index * 8)
        }

        // Clean up
        g.dispose()
        return ImageIcon(image)
    }

    private suspend fun createTextPreview(path: String): String? {
        return withContext(Dispatchers.IO) {
            try {
                File(path).bufferedReader().useLines { lines ->
                    lines.take(10).joinToString("\n")
                }
            } catch (e: Exception) {
                // TODO: if could not read a text file: must be something wrong with it
                // show some alert icon instead?
                null
            }
        }
    }

    private fun resizeThumbnail(image: BufferedImage): BufferedImage {
        val width = image.width
        val height = image.height
        val scaleFactor = Settings.iconSize.toDouble() / max(width, height)
        val newWidth = (width * scaleFactor).toInt()
        val newHeight = (height * scaleFactor).toInt()

        val resizedImage = BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_ARGB)
        val graphics = resizedImage.createGraphics()
        graphics.setRenderingHint(
            RenderingHints.KEY_INTERPOLATION,
            RenderingHints.VALUE_INTERPOLATION_BILINEAR
        )
        graphics.drawImage(image, 0, 0, newWidth, newHeight, null)
        graphics.dispose()

        return resizedImage
    }

    private suspend fun createThumbnail(path: String): ImageIcon? {
        // TODO: decomposition
        return withContext(Dispatchers.IO) {
            try {
                val file = File(path)
                val imageInputStream = ImageIO.createImageInputStream(file)
                val readers = ImageIO.getImageReaders(imageInputStream)

                if (readers.hasNext()) {
                    val reader = readers.next()

                    try {
                        reader.input = imageInputStream

                        // try to extract the already existing thumbnail
                        val metadata = reader.getImageMetadata(0)
                        val standardTree = metadata.getAsTree(IIOMetadataFormatImpl.standardMetadataFormatName) as IIOMetadataNode
                        val childNodes = standardTree.getElementsByTagName("Thumbnail")

                        if (childNodes.length > 0) {
                            // Theoretically, several thumbnails can be included in the image
                            val thumbnail = reader.readThumbnail(0, 0)
                            val resizedThumbnail = resizeThumbnail(thumbnail)
                            return@withContext ImageIcon(resizedThumbnail)
                        }

                        // No thumbnail found -> just read the whole image
                        val width = reader.getWidth(reader.minIndex)
                        val height = reader.getHeight(reader.minIndex)
                        val maxDim = max(width, height)

                        // Skip huge images
                        if (maxDim > Settings.maxImageSizeToShowThumbnail)
                        {
                            return@withContext null
                        }

                        // Read the image
                        val fullImage = reader.read(reader.minIndex)
                        val thumbnailImage = resizeThumbnail(fullImage)
                        ImageIcon(thumbnailImage)
                    } finally {
                        reader.dispose()
                        imageInputStream.close()
                    }
                } else {
                    null
                }
            } catch (e: IOException) {
                // Handle IO errors
                null
            } catch (e: IllegalArgumentException) {
                // Handle invalid arguments
                null
            }
        }
    }

    fun dispose() {
        job.cancel()
    }
}
