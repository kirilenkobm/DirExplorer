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
import javax.swing.JLabel
import kotlin.coroutines.CoroutineContext
import kotlin.math.max

/**
 * Class that manages operations related to Files (text, binary, etc)
 * Named ExplorerFile to avoid collision with java.io.File
 */
class ExplorerFile(override val path: String): FileSystemEntity {

    override val name: String
        get() = Paths.get(path).fileName.toString()

    val extension: String
        get() = Paths.get(path).fileName.toString().substringAfter(".", "")

    val fileType: String
        // Java built in method to determine file type
        get() = Files.probeContentType(Paths.get(path)) ?: Constants.UNKNOWN_FILE_TYPE
}
