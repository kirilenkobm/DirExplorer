package views.iconviews

import com.drew.imaging.ImageMetadataReader
import com.drew.metadata.exif.ExifThumbnailDirectory
import dataModels.ExplorerFile
import kotlinx.coroutines.*
import state.Settings
import views.IconManager
import java.awt.Color
import java.awt.Desktop
import java.awt.Image
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.io.File
import java.io.IOException
import java.io.RandomAccessFile
import javax.imageio.ImageIO
import javax.swing.ImageIcon
import javax.swing.SwingUtilities
import kotlin.coroutines.CoroutineContext


class FileIconView(entity: ExplorerFile): AbstractIconEntityView(entity), CoroutineScope {
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
                val thumbnail = createThumbnail(fileEntity.path)
                if (thumbnail != null) {
                    // If thumbnail created successfully -> apply it
                    SwingUtilities.invokeLater {
                        iconLabel.icon = resizeIcon(thumbnail)
                    }
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
        g.font = g.font.deriveFont(10f)

        val lines = previewText.split("\n")
        for ((index, line) in lines.withIndex()) {
            // I found only a way to draw the text line by line
            g.drawString(line, 5, 20 + index * 15)
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
                // TODO: potentially skip huge images?
                // TODO: preserve in scale like 128x128 (to allow rescaling UI)
                // TODO: preserve aspect ratio
                // TODO: maybe limit number of simultaneous threads here?
                val thumbnailImage = ImageIO.read(File(path))
                thumbnailImage.getScaledInstance(Settings.iconSize, Settings.iconSize, Image.SCALE_SMOOTH)
                // return imageIcon
                ImageIcon(thumbnailImage)
            } catch (e: Exception) {
                // if any error: return null
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
