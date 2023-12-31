import java.awt.Color
import javax.swing.UIManager

/**
 * Singleton object holding all the constants used across the project.
 *
 * This object includes constants related to application behavior such as history size,
 * file type and size defaults, and semaphore limits. It also contains constants for UI elements
 * such as color schemes, view constraints, and table and grid view settings.
 */
object Constants {
    const val HISTORY_SIZE: Int = 10  // back and forth stack sizes
    const val UNKNOWN_FILE_TYPE = "unknown"
    const val UNKNOWN_FILE_SIZE = 0L
    const val UNKNOWN_TIME = 0L

    // Address bar related
    const val ELLIPSIS_LABEL = "  ...  "
    const val ADDRESS_BAR_PREFERRED_NUM_START_ELEMS = 6
    const val ADDRESS_BAR_PREFERRED_NUM_END_ELEMS = 6

    // Color-related constants
    val BACKGROUND_COLOR_LIGHT = Color(255, 255, 255, 255)
    val BACKGROUND_COLOR_DARK = Color(42, 42, 44, 255)
    val DEFAULT_SWING_BACKGROUND_COLOR: Color = UIManager.getColor("Panel.background")
    val SELECTION_COLOR = Color(66, 135, 245, 255)
    val TRANSPARENT_COLOR = Color(0, 0, 0, 0)
    val TABLE_EVEN_ROW_LIGHT_THEME_COLOR = Color(237, 237, 237)
    val TABLE_EVEN_ROW_DARK_THEME_COLOR = Color(50, 50, 50)
    val TABLE_ODD_ROW_DARK_THEME_COLOR = Color(30, 30, 30)

    // Semaphore limits
    const val MAX_IMAGE_PREVIEWS = 2
    const val MAX_TEXT_PREVIEWS = 10
    const val MAX_UNZIPPED_DIRS = 1

    // View constraints
    const val PREFERRED_WIDTH = 1240
    const val PREFFERED_HEIGTH = 960

    // Table view
    const val TABLE_VIEW_FIRST_COL_SIZE = 20
    const val TABLE_ICON_SIZE = 16

    // Grid view
    const val GRID_COLUMN_WIDTH = 90
    // const val GRID_COLUMN_MAX_WIDTH = 100

    const val GRID_TEXT_FRAME_HEIGHT = 35
    const val GRID_IMAGE_FRAME_HEIGHT = 85
    const val GRID_ROW_HEIGHT = 120

    const val MAX_SHOWN_NAME_LENGTH = 20
    const val SHOWN_NAME_OPEN_TAG = "<html><div style='text-align: center;'>"
    const val SHOWN_NAME_CLOSE_TAG = "</div></html>"
    const val NONAME_FILE =
        "<html><div style='text-align: center; color: gray; font-style: italic;'>unnamed</div></html>"

    const val LANGUAGE_BUNDLE_PATH = "languages/Messages"

    // Thumbnail related

    // Cache related
    const val THUMBNAIL_CACHE_SIZE = 10000

    //// Text previews
    const val TEXT_PREVIEW_INIT_XY_OFFSET = 4
    const val TEXT_PREVIEW_TEXT_SIZE = 7f
    const val TEXT_PREVIEW_LINES_INTERVAL = 7
    const val TEXT_PREVIEW_NUM_LINES_TO_TAKE = 10

    // File extension and type related
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

    val archiveTypes = setOf(
        "application/x-freearc",
        "application/x-bzip",
        "application/x-bzip2",
        "application/x-tar",
        "application/x-7z-compressed",
        "application/gzip",
        // "application/zip"  // in fact, zip is handled by ZipArchive entity
    )
}
