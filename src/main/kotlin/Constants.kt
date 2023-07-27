import java.awt.Color
import javax.swing.UIManager

/**
 * All project related constants.
 */
object Constants {
    // TOOD: move all more or less meaningful constants here
    const val ZIP_EXTENSION = ".zip"
    const val HISTORY_SIZE: Int = 40  // back and forth stack sizes

    const val ELLIPSIS_LABEL = "  ...  "

    const val UNKNOWN_FILE_TYPE = "unknown"
    const val UNKNOWN_FILE_SIZE = 0L
    const val UNKNOWN_TIME = 0L

    val BACKGROUND_COLOR_LIGHT = Color(255, 255, 255, 255)
    val BACKGROUND_COLOR_DARK = Color(42, 42, 44, 255)
    val DEFAULT_SWING_BACKGROUND_COLOR = UIManager.getColor("Panel.background")

    const val MAX_IMAGE_PREVIEWS = 2
    const val MAX_TEXT_PREVIEWS = 10

    val textFileExtensionsNotInMime = setOf(
        "java", "py", "kt", "js",
        "html", "css", "c", "cpp",
        "h", "hpp", "go", "rs",
        "rb", "log", "iml", "bat",
        "kts", "csh", "sh", "fasta",
        "fa", "fastq", "nf", "json",
        "swift", ".s"
    )
}
