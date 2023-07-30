package service

import Constants
import com.drew.imaging.ImageMetadataReader
import com.drew.imaging.ImageProcessingException
import com.drew.metadata.exif.ExifThumbnailDirectory
import model.ExplorerFile
import kotlinx.coroutines.*
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.rendering.PDFRenderer
import state.Settings
import view.iconviews.FileIconView
import java.awt.Color
import java.awt.RenderingHints
import java.awt.image.BufferedImage
import java.io.File
import java.io.IOException
import java.io.RandomAccessFile
import javax.imageio.ImageIO
import javax.swing.Icon
import javax.swing.ImageIcon
import kotlin.coroutines.CoroutineContext
import kotlin.math.max

/**
 * Generate thumbnail image for a file if applicable.
 * - if this is an image file:
 *   - try to extract the image from cache
 *   - try to extract thumbnail from a file if it's already included
 *   - if not - open the image, rescale it
 *   - save to cache afterward
 * - if text file: read first N lines and generate an image
 * - if pdf - generate thumbnail from the 1st page
 */
class ThumbnailGenerationService(
    private val fileEntity: ExplorerFile,
    private val fileIcon: FileIconView
): CoroutineScope {
    private val job = Job()
    private val iconCache = ThumbnailsCache

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    fun startThumbnailGeneration() {
        val imagePreviewsSemaphore = SemaphoreManager.imagePreviewsSemaphore
        val textPreviewsSemaphore = SemaphoreManager.textPreviewsSemaphore

        val fileTypeStartsWithImage = fileEntity.fileType.startsWith("image/")
        val fileTypeStartsWithText = fileEntity.fileType.startsWith("text/")
        val fileTypeIsPFD = fileEntity.fileType == "application/pdf"
        // if name contains many dots, the ImageIO.getReaderFileSuffixes() can be confused
        // so correctedExtension contains the last possible extension
        val correctedExtension = fileEntity.extension.split(".").last().lowercase()
        val fileHasSupportedImageIOExtension = ImageIO.getReaderFileSuffixes().contains(correctedExtension)
        val fileExtensionIsText = Constants.textFileExtensionsNotInMime.contains(correctedExtension)

        if (fileTypeStartsWithImage && fileHasSupportedImageIOExtension) {
            // Generate image's preview
            launch(Dispatchers.IO) {
                imagePreviewsSemaphore.acquire()
                try {
                    getFromCacheOrGenerateThumbnail("image")
                } finally {
                    imagePreviewsSemaphore.release()
                }
            }
        } else if (fileTypeIsPFD)  {
            // Generate preview for PDF
            launch(Dispatchers.IO) {
                imagePreviewsSemaphore.acquire()
                try {
                    getFromCacheOrGenerateThumbnail("PDF")
                } finally {
                    imagePreviewsSemaphore.release()
                }
            }
        } else if (fileTypeStartsWithText || fileExtensionIsText) {
            // Generate preview for text file
            launch(Dispatchers.IO) {
                textPreviewsSemaphore.acquire()
                try {
                    val previewText = getTextForPreview(fileEntity.path)
                    if (!previewText.isNullOrEmpty()) {
                        fileIcon.iconLabel.icon = createTextIcon(previewText)
                    }
                } finally {
                    textPreviewsSemaphore.release()
                }
            }
        }
    }

    /**
     * The same strategy for image and pdf.
     * If it's already in the cache -> use cached
     * If not -> try to extract
     * if case of success -> add to cache
     */
    private suspend fun getFromCacheOrGenerateThumbnail(mode: String) {
        val thumbnail: Icon? = iconCache[fileEntity.path]
        val usedFunction: (suspend () -> Icon?)? = when (mode) {
            "PDF" -> { -> generatePDFThumbnail() }
            "image" -> { -> createImageThumbnail() }
            else -> null
        }
        if (thumbnail == null && usedFunction != null) {
            val newThumbnail = usedFunction.invoke()
            if (newThumbnail != null) {
                iconCache[fileEntity.path] = newThumbnail
                fileIcon.iconLabel.icon = newThumbnail
            }
            delay(250)  // rude way to decrease the priority of the task
        } else if (thumbnail != null) {
            fileIcon.iconLabel.icon = thumbnail
        }
    }

    /**
     * Given a piece of text, create an image.
     */
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
        g.color = Color.BLACK
        g.font = g.font.deriveFont(Constants.TEXT_PREVIEW_TEXT_SIZE)

        val lines = previewText.split("\n")
        for ((index, line) in lines.withIndex()) {
            g.drawString(
                line,
                Constants.TEXT_PREVIEW_INIT_XY_OFFSET,
                Constants.TEXT_PREVIEW_INIT_XY_OFFSET + index * Constants.TEXT_PREVIEW_LINES_INTERVAL)
        }

        // Clean up
        g.dispose()
        return ImageIcon(image)
    }

    private fun getTextForPreview(path: String): String? {
        try {
            return File(path).bufferedReader().useLines { lines ->
                lines.take(Constants.TEXT_PREVIEW_NUM_LINES_TO_TAKE).joinToString("\n")
            }
        } catch (e: Exception) {
            // TODO: if could not read a text file: must be something wrong with it
            // show some alert icon instead?
            return null
        }
    }

    private fun getSubsamplingValue(maxDimension: Int): Int {
        return  when {
            maxDimension <= 1200 -> 1
            maxDimension <= 2200 -> 2
            maxDimension <= 3400 -> 3
            maxDimension <= 4200 -> 6
            else -> 10
        }
    }

    internal fun createImageThumbnail(): Icon? {
        try {
            val file = File(fileEntity.path)
            // TODO: probably, this idea did not work out
            // but worth trying to implement for overall improvement
            // For metadata extractor, it's easier to provide file
//                val includedThumbnail = extractImageThumbnailIfExists(file)
//                if (includedThumbnail != null) {
//                    println("")
//                    return resizeThumbnail(includedThumbnail)
//                }

            // This way of extracting data from image suppose to be quicker
            val imageInputStream = ImageIO.createImageInputStream(file)
            val readers = ImageIO.getImageReaders(imageInputStream)

            if (readers.hasNext()) {
                val reader = readers.next()

                try {
                    reader.input = imageInputStream
                    // First, try to acquire thumbnail if it's already
                    // included in the image file (can work for jpeg and tiff)


                    // No thumbnail found -> just read the whole image
                    val width = reader.getWidth(reader.minIndex)
                    val height = reader.getHeight(reader.minIndex)
                    val maxDimension = max(width, height)
                    // Skip huge images
                    if (maxDimension > Settings.maxImageSizeToShowThumbnail) {
                        return null
                    }

                    // Read the image
                    val param = reader.defaultReadParam
                    // apply subsampling to load smaller image into RAM
                    // and increase processing speed
                    val subsamplingVal = getSubsamplingValue(maxDimension)
                    param.setSourceSubsampling(subsamplingVal, subsamplingVal, 0, 0)
                    val fullImage = reader.read(reader.minIndex, param)
                    return resizeThumbnail(fullImage)
                } finally {
                    reader.dispose()
                    imageInputStream.close()
                }
            } else {
                return null
            }
        } catch (e: IOException) {
            // Handle IO errors
            return null
        } catch (e: IllegalArgumentException) {
            // Handle invalid arguments
            return null
        }
    }

    /**
     * Check if metadata tree contains a thumbnail. If yes -> just return it.
     * The function does not work as expected with JPEG images created
     * using Lightroom from Fuji and Canon camera RAWs.
     */
    internal fun extractImageThumbnailIfExists(imageFile: File): BufferedImage? {
        try {
            val metadata = ImageMetadataReader.readMetadata(imageFile)
            val directory = metadata.getFirstDirectoryOfType(ExifThumbnailDirectory::class.java)

            if (directory != null && directory.containsTag(ExifThumbnailDirectory.TAG_THUMBNAIL_OFFSET)) {
                val offset = directory.getInteger(ExifThumbnailDirectory.TAG_THUMBNAIL_OFFSET)
                val length = directory.getInteger(ExifThumbnailDirectory.TAG_THUMBNAIL_LENGTH)

                if (length > 0) {
                    RandomAccessFile(imageFile, "r").use { raf ->
                        raf.seek(offset.toLong())
                        val thumbnailData = ByteArray(length)
                        raf.read(thumbnailData)
                        return ImageIO.read(thumbnailData.inputStream())
                    }
                } else {
                    println("Length is 0")
                    return null
                }
            }
            println("Cannot find directory")
        } catch (e: ImageProcessingException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return null
    }

    /**
     * Generating PDF previews: just get 1st page and
     * read as Buffered image using PDDocument library
     */
    private fun generatePDFThumbnail(): Icon? {
        return try {
            val document = PDDocument.load(File(fileEntity.path))
            val pdfRenderer = PDFRenderer(document)
            val image: BufferedImage = pdfRenderer.renderImageWithDPI(0, 40f)
            document.close()
            resizeThumbnail(image)
        } catch (e: Exception) {
            null
        }
    }

    internal fun resizeThumbnail(image: BufferedImage): Icon {
        val width = image.width
        val height = image.height
        // Do not rescale what is already scaled
        if (width == Settings.iconSize || height == Settings.iconSize) {
            return ImageIcon(image)
        }
        val scaleFactor = Settings.iconSize.toDouble() / max(width, height)
        val newWidth = (width * scaleFactor).toInt()
        val newHeight = (height * scaleFactor).toInt()

        // Sometimes TIFF causes the Unknown image type 0 exception
        val imageType =
            if (image.type != BufferedImage.TYPE_CUSTOM) image.type
            else BufferedImage.TYPE_INT_ARGB
        val resizedImage = BufferedImage(newWidth, newHeight, imageType)
        val g = resizedImage.createGraphics()
        // ideally, with these keys quality will be a bit better
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC)
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY)
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)

        g.drawImage(image, 0, 0, newWidth, newHeight, null)
        g.dispose()

        return ImageIcon(resizedImage)
    }

    fun dispose() {
        job.cancel()
    }
}

// Probably delete later
// Implementation to create awaited instead
/*
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
     */