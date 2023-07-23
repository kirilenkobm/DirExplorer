package views.iconviews

import com.drew.imaging.ImageMetadataReader
import com.drew.metadata.exif.ExifThumbnailDirectory
import dataModels.ExplorerFile
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Semaphore
import state.Settings
import views.IconManager
import java.awt.Color
import java.awt.Desktop
import java.awt.Image
import java.awt.RenderingHints
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.io.File
import java.io.IOException
import java.io.RandomAccessFile
import java.util.concurrent.Executors
import javax.imageio.ImageIO
import javax.swing.ImageIcon
import javax.swing.SwingUtilities
import kotlin.coroutines.CoroutineContext
import kotlin.math.max


class FileIconView(entity: ExplorerFile, private val thumbnailSemaphore: Semaphore): AbstractIconEntityView(entity), CoroutineScope {
    private val fileEntity = entity
    private val job = Job()
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    override fun setIcon() {
        // default base image
        iconLabel.icon = resizeIcon(IconManager.getIconForFileType(fileEntity.fileType))

        // if it's image: try to replace icon with thumbnail
        // also check whether is supported by ImageIO
        val fileType = fileEntity.fileType
        val fileExtension = fileEntity.extension
        if (fileType.startsWith("image/") && ImageIO.getReaderFileSuffixes().contains(fileExtension)) {
            //Start loading the thumbnail if possible
            launch(Dispatchers.IO) {
                try {
                    thumbnailSemaphore.acquire()
                    val thumbnail = createThumbnail(fileEntity.path)
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
                    thumbnailSemaphore.release() // Release the permit
                }
            }
        } else if (fileType.startsWith("text/") || fileExtension == "log") {
            // It is a text file -> also required to provide a preview
            launch(Dispatchers.IO) {
                val previewText = createTextPreview(fileEntity.path)
                if (!previewText.isNullOrEmpty()) {
                    // if empty -> no reason to show a preview
                    SwingUtilities.invokeLater {
                        iconLabel.icon = createTextIcon(previewText)
                    }
                }
            }
        }
        // TODO else if (fileType == "application/pdf") {
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

    private suspend fun createThumbnail(path: String): ImageIcon? {
        return withContext(Dispatchers.IO) {
            try {
                // TODO: try to extract already existing thumbnail if possible
                // TODO: preserve in scale like 128x128 (to allow rescaling UI)
                // TODO: preserve aspect ratio
                val file = File(path)
                val imageInputStream = ImageIO.createImageInputStream(file)
                val readers = ImageIO.getImageReaders(imageInputStream)

                if (readers.hasNext()) {
                    val reader = readers.next()

                    try {
                        reader.input = imageInputStream

                        val width = reader.getWidth(reader.minIndex)
                        val height = reader.getHeight(reader.minIndex)
                        val maxDim = max(width, height)

                        // Skip huge images
                        if (maxDim > Settings.maxImageSizeToShowThumbnail)
                        {
                            return@withContext null
                        }

                        // Read the image
                        val image = reader.read(reader.minIndex)

                        // Calculate the new width and height
                        val scaleFactor = Settings.iconSize.toDouble() / max(width, height)
                        val newWidth = (width * scaleFactor).toInt()
                        val newHeight = (height * scaleFactor).toInt()

                        // Scale the image with a higher-quality algorithm
                        val thumbnailImage = BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB)
                        val graphics = thumbnailImage.createGraphics()
                        graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC)
                        graphics.drawImage(image, 0, 0, newWidth, newHeight, null)
                        graphics.dispose()

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


    init {
        entityPanel.addMouseListener(object : MouseAdapter() {
            override fun mouseClicked(e: MouseEvent) {
                if (Desktop.isDesktopSupported()) {
                    try {
                        Desktop.getDesktop().open(File(entity.path))
                    } catch (ex: IOException) {
                        ex.printStackTrace()
                        println("TODO: come up with error ")
                    }
                }
            }
        })
    }

    fun dispose() {
        job.cancel()
    }
}
