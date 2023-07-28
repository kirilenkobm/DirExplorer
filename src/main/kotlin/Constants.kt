import java.awt.Color
import javax.swing.UIManager

/**
 * All project related constants.
 */
object Constants {
    // TODO: move all more or less meaningful constants here
    const val HISTORY_SIZE: Int = 4  // back and forth stack sizes
    const val ELLIPSIS_LABEL = "  ...  "

    const val UNKNOWN_FILE_TYPE = "unknown"
    const val UNKNOWN_FILE_SIZE = 0L
    const val UNKNOWN_TIME = 0L

    // Color-related constants
    val BACKGROUND_COLOR_LIGHT = Color(255, 255, 255, 255)
    val BACKGROUND_COLOR_DARK = Color(42, 42, 44, 255)
    val DEFAULT_SWING_BACKGROUND_COLOR: Color = UIManager.getColor("Panel.background")
    val SELECTION_COLOR = Color(66, 135, 245, 255)
    val TRANSPARENT_COLOR = Color(0, 0, 0, 0)
    val TABLE_EVEN_ROW_LIGHT_THEME_COLOR = Color(237, 237, 237)
    val TABLE_EVEN_ROW_DARK_THEME_COLOR = Color(50, 50, 50)
    val TABLE_ODD_ROW_DARK_THEME_COLOR = Color(30, 30, 30)

    // View constraints
    const val TABLE_VIEW_FIRST_COL_SIZE = 20

    const val GRID_COLUMN_WIDTH = 90
    const val GRID_COLUMN_MAX_WIDTH = 90
    const val GRID_TEXT_FRAME_HEIGHT = 35
    const val GRID_IMAGE_FRAME_HEIGHT = 85
    const val GRID_ROW_HEIGHT = 120

    const val MAX_SHOWN_NAME_LENGTH = 20

    // Semaphore limits
    const val MAX_IMAGE_PREVIEWS = 2
    const val MAX_TEXT_PREVIEWS = 10
    const val MAX_UNZIPPED_DIRS = 1

    // File extension related
    const val ZIP_EXTENSION = ".zip"
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
