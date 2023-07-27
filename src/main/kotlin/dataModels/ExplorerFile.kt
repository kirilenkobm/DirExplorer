package dataModels
import Constants
import kotlinx.coroutines.*
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.rendering.PDFRenderer
import state.AppState
import state.Settings
import views.iconviews.IconsCache
import java.awt.Color
import java.awt.Image
import java.awt.image.BufferedImage
import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Paths
import javax.imageio.ImageIO
import javax.imageio.metadata.IIOMetadataFormatImpl
import javax.imageio.metadata.IIOMetadataNode
import javax.swing.Icon
import javax.swing.ImageIcon
import kotlin.coroutines.CoroutineContext
import kotlin.math.max

/**
 * Class that manages operations related to Files (text, binary, etc)
 * Named ExplorerFile to avoid collision with java.io.File
 */
class ExplorerFile(override val path: String): FileSystemEntity, CoroutineScope {
    private val job = Job()
    private val iconCache = IconsCache

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    override val name: String
        get() = Paths.get(path).fileName.toString()

    val extension: String
        get() = Paths.get(path).fileName.toString().substringAfter(".", "")

    val fileType: String
        // Java built in method to determine file type
        get() = Files.probeContentType(Paths.get(path)) ?: Constants.UNKNOWN_FILE_TYPE

    /**
     * Generate thumbnail image if applicable.
     * - if this is an image file:
     *   - try to extract the image from cache
     *   - try to extract thumbnail from a file if it's already included
     *   - if not - open the image, rescale it
     *   - save to cache afterward
     * - if text file: read first N lines and generate an image
     * - if pdf - generate thumbnail from the 1st page
     */
    fun startThumbnailGeneration(): Deferred<Icon?> {

        val imagePreviewsSemaphore = AppState.imagePreviewsSemaphore
        val textPreviewsSemaphore = AppState.textPreviewsSemaphore

        val correctedExtension = extension.split(".").last().lowercase()
        val fileTypeStartsWithImage = fileType.startsWith("image/")
        val fileTypeStartsWithText = fileType.startsWith("text/")
        val fileTypeIsPFD = fileType == "application/pdf"
        val fileHasSupportedImageIOExtension = ImageIO.getReaderFileSuffixes().contains(correctedExtension)
        val fileExtensionIsText = Constants.textFileExtensionsNotInMime.contains(correctedExtension)

        return when {
            fileTypeStartsWithImage && fileHasSupportedImageIOExtension -> {
                async(Dispatchers.IO) {
                    imagePreviewsSemaphore.acquire()
                    try {
                        var thumbnail: Icon? = iconCache[path]
                        if (thumbnail == null) {
                            thumbnail = createThumbnail(path)
                            if (thumbnail != null) {
                                iconCache[path] = thumbnail
                            }
                        }
                        thumbnail
                    } finally {
                        imagePreviewsSemaphore.release()
                    }
                }
            } fileTypeStartsWithText || fileExtensionIsText -> {
                async(Dispatchers.IO) {
                    textPreviewsSemaphore.acquire()
                    try {
                        val previewText = createTextPreview(path)
                        if (!previewText.isNullOrEmpty()) {
                            createTextIcon(previewText)
                        } else {
                            null
                        }
                    } finally {
                        textPreviewsSemaphore.release()
                    }
                }
            } fileTypeIsPFD -> {
                async(Dispatchers.IO) {
                    // TODO: encapsulate in another method
                    imagePreviewsSemaphore.acquire()
                    try {
                        var thumbnail: Icon? = iconCache[path]
                        if (thumbnail == null) {
                            val document = PDDocument.load(File(path))
                            val pdfRenderer = PDFRenderer(document)
                            val image: BufferedImage = pdfRenderer.renderImageWithDPI(0, 40f)
                            document.close()
                            thumbnail = resizeThumbnail(image)
                        }
                        val safeThumbnail = thumbnail!!
                        iconCache[path] = safeThumbnail
                        safeThumbnail
                    } catch (e: IOException) {
                        // Handle the exception
                        println("Failed to render PDF image: ${e.message}")
                        null
                    } finally {
                        imagePreviewsSemaphore.release()
                    }
                }
            } else -> {
                async(Dispatchers.IO) {
                    null
                }
            }
        }
    }

    private fun createTextIcon(previewText: String): Icon {
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

    private suspend fun createThumbnail(path: String): Icon? {
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
                        val standardTree = metadata.getAsTree(
                            IIOMetadataFormatImpl.standardMetadataFormatName
                        ) as IIOMetadataNode
                        val childNodes = standardTree.getElementsByTagName("Thumbnail")

                        if (childNodes.length > 0) {
                            // Theoretically, several thumbnails can be included in the image
                            val thumbnail = reader.readThumbnail(0, 0)
                            return@withContext resizeThumbnail(thumbnail)
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
                        thumbnailImage
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

    private fun resizeThumbnail(image: BufferedImage): Icon {
        val width = image.width
        val height = image.height
        // Do not rescale what is already scaled
        if (width == Settings.iconSize || height == Settings.iconSize) {
            return ImageIcon(image)
        }
        val scaleFactor = Settings.iconSize.toDouble() / max(width, height)
        val newWidth = (width * scaleFactor).toInt()
        val newHeight = (height * scaleFactor).toInt()

        val resizedImage = image.getScaledInstance(
            newWidth,
            newHeight,
            Image.SCALE_DEFAULT
        )

        return ImageIcon(resizedImage)
    }

    fun dispose() {
        job.cancel()
    }
}
