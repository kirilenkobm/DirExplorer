package service

import Constants
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
import java.util.logging.Level
import java.util.logging.Logger
import javax.imageio.ImageIO
import javax.swing.Icon
import javax.swing.ImageIcon
import kotlin.coroutines.CoroutineContext
import kotlin.math.max

/**
 * Service class responsible for generating thumbnail images for file entities.
 *
 * This class uses coroutines to generate thumbnails asynchronously.
 * It supports generating thumbnails for image files, text files, and PDF files.
 * For image and pdf files, it first checks if a thumbnail is available in the cache.
 * If not, it  opens the image or the first page of pdf, rescales it, and saves it to the cache.
 * Then, it applies the thumbnail to the respective fileIcon (not sure whether it's the best idea).
 *
 * Planned for JPEG and TIFF images: first, try to extract the embedded thumbnail, it is present.
 *
 *  For text files, it reads the first N lines and generates an image from them.
 *
 * fileEntity: The file entity for which a thumbnail is to be generated.
 * fileIcon: The view where the generated thumbnail will be displayed.
 */
class ThumbnailGenerationService(
    private val fileEntity: ExplorerFile,
    private val fileIcon: FileIconView
): CoroutineScope {
    private val job = Job()
    private val iconCache = ThumbnailsCache

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO + job

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
        // Enable anti-aliasing and subpixel rendering
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON)
        g.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON)

        val lines = previewText.split("\n")
        for ((index, textLine) in lines.withIndex()) {
            g.drawString(
                textLine,
                Constants.TEXT_PREVIEW_INIT_XY_OFFSET,
                Constants.TEXT_PREVIEW_INIT_XY_OFFSET + index * Constants.TEXT_PREVIEW_LINES_INTERVAL)
        }

        // Clean up
        g.dispose()
        return ImageIcon(image)
    }

    /**
     * Extract few lines from the text file to create the preview.
     */
    private fun getTextForPreview(path: String): String? {
        return try {
            File(path).bufferedReader().useLines { lines ->
                lines.take(Constants.TEXT_PREVIEW_NUM_LINES_TO_TAKE).joinToString("\n")
            }
        } catch (e: Exception) {
            // must be something wrong with this text file
            // for example, it's not a text file at all
            null
        }
    }

    /**
     * Loading massive images into RAM to produce thumbnails affects the performance.
     * Given the longest image dimension, this function returns a down-sampling coefficient.
     * Therefore, the program creates previews for massive images quickly.
     * Given that the thumbnail size is no bigger than 72x72 in the current version,
     * such a down-sampling is quite appropriate.
     */
    private fun getSubsamplingValue(maxDimension: Int): Int {
        return when {
            maxDimension <= 1200 -> 1
            maxDimension <= 2200 -> 2
            maxDimension <= 3400 -> 3
            maxDimension <= 4200 -> 6
            else -> 10
        }
    }

    /**
     * Generates thumbnail for an image file.
     * Creates a reader, (if needed) down-samples the image, and then
     * loads it and resizes appropriately.
     */
    internal fun createImageThumbnail(): Icon? {
        try {
            val file = File(fileEntity.path)

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
     * Generating PDF previews: just get 1st page and
     * read as Buffered image using PDDocument library
     */
    private fun generatePDFThumbnail(): Icon? {
        return try {
            // Suppress PDFBox warnings, keeping only the most serious
            Logger.getLogger("org.apache.pdfbox").level = Level.SEVERE
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
        g.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY)

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