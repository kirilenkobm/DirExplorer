package util

import Constants
import state.Settings
import java.awt.Image
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.PatternSyntaxException
import javax.swing.ImageIcon

/**
 * Singleton object providing utility functions for the application.
 *
 * Provides helper methods that facilitate operations within the application.
 *
 * These operations include:
 *
 * 1. Converting file sizes from bytes to a human-readable format.
 * 2. Formatting Unix time to a human-readable date string.
 * 3. Checking whether a file extension matches a given filter, supporting both exact and regex matches.
 * 4. Shortening long filenames for display in the icon view, if needed.
 * 5. Resizing icons to a standard size for uniform display.
 */
object Utils {
    /**
     * Converts a size in bytes to a human-readable string.
     */
    fun humanReadableSize(bytes: Long): String {
        if (bytes == 0L) {
            return "empty"
        } else if (bytes < 0L) {
            return "undefined"
        }
        val kilobyte = 1024.0
        val megabyte = kilobyte * 1024
        val gigabyte = megabyte * 1024
        val terabyte = gigabyte * 1024

        return when {
            bytes < kilobyte -> "$bytes B"
            bytes < megabyte -> String.format("%.1f KB", bytes / kilobyte)
            bytes < gigabyte -> String.format("%.1f MB", bytes / megabyte)
            bytes < terabyte -> String.format("%.1f GB", bytes / gigabyte)
            else -> String.format("%.1f TB", bytes / terabyte)
        }
    }

    private val dateFormat = SimpleDateFormat("yyyy.MM.dd 'at' HH:mm:ss")

    /**
     * Formats unix time number as a string.
     */
    fun formatDate(unixTime: Long): String {
        if (unixTime <= 0L) {
            return "unknown"
        }
        return dateFormat.format(Date(unixTime))
    }

    /**
     * Function to check whether file extension matches the extension filter.
     */
    fun matchesExtension(entityExtensionRaw: String, filterExtensionRaw: String): Boolean {
        // Maybe case-insensitive is better?
        val entityExtension = entityExtensionRaw.lowercase(Locale.getDefault())
        val filterExtension = filterExtensionRaw.lowercase()
        // Check if filter starts with ~ and should invert the result
        val invertResult = filterExtension.startsWith("~")
        val cleanedFilter = if (invertResult) filterExtension.drop(1) else filterExtension

        // if there is a full match, return true (or false if inverted)
        if(entityExtension == cleanedFilter) return !invertResult

        // check for partial match in case of complex extension
        val entitySubExtensions = entityExtension.split('.')
        if (!invertResult) {
            if (entitySubExtensions.any { it == filterExtension }) return true
        } else {
            if (entitySubExtensions.none { it == filterExtension }) return true
        }

        // check for match with regex pattern
        return try {
            val regexPattern = filterExtension
                .replace("*", ".*")

            val regex = regexPattern.toRegex()
            val matches = regex.containsMatchIn(entityExtension)
            if (invertResult) !matches else matches
        } catch (e: PatternSyntaxException) {
            println(e)
            false
        }
    }

    /**
     * In case filename is too long, I'd like to shorten
     * it in the icon view, replacing part of the name
     * with ellipsis.
     *
     * Function still can be improved in terms of the ... and
     * the line break positioning.
    */
    fun getFilenameForIcon(filename: String): String {
        if (filename.isEmpty()) {
            return Constants.NONAME_FILE
        }
        val maxOneLine = Constants.MAX_SHOWN_NAME_LENGTH / 2

        fun findBestSplitIndex(filename: String, nameInLimits: Boolean): Int {
            // find the best place to split the filename between the lines
            // from minFirst index to maximal one line length -> such that
            // first and second half do not exceed the limit for one line

            // nameInLimits -> is filename <= MAX NAME LIMIT OR NOT
            val minFirstIndex = if (!nameInLimits) {
                filename.length - maxOneLine
            } else {
                // if not -> we will shrink the 2nd half anyway using ...
                maxOneLine / 2
            }
            // prefer to split before special characters, capital letters and numbers
            val splitIndex = (minFirstIndex..maxOneLine).find { i ->
                (filename[i].isUpperCase() && filename[i - 1].isLowerCase())
                        || filename[i].isWhitespace()
                        || filename[i] == '.'
                        || filename[i] == '_'
                        || filename[i] == '-'
                        || filename[i].isDigit()
            } ?: maxOneLine  // just split 50:50 if we cannot find the best place
            return splitIndex
        }

        fun shortenSecondHalfLongFilename(secondLineRaw: String): String {
            // first, check how many characters to replace with ...
            val firstChar = secondLineRaw[0]
            val lastPart = secondLineRaw.substring(secondLineRaw.length - maxOneLine + 1)
            return "$firstChar...$lastPart"
        }

        // If filename fits on one line: just return it
        if (filename.length <= maxOneLine) {
            return filename
        }

        // Filename fits two lines, without need to introduce ...
        if (filename.length <= Constants.MAX_SHOWN_NAME_LENGTH) {
            // here, need to find a better place to split it
            val splitIndex = findBestSplitIndex(filename, true)
            val firstLine = filename.substring(0, splitIndex)
            val secondLine = filename.substring(splitIndex)
            return "${Constants.SHOWN_NAME_OPEN_TAG}$firstLine<br>$secondLine${Constants.SHOWN_NAME_CLOSE_TAG}"
        }

        // this branch -> filename is very long, does not fit two lines
        val splitIndex = findBestSplitIndex(filename, false)
        val firstLine = filename.substring(0, splitIndex)
        val secondLineRaw = filename.substring(splitIndex)
        val secondLine = shortenSecondHalfLongFilename(secondLineRaw)
        return "${Constants.SHOWN_NAME_OPEN_TAG}$firstLine<br>$secondLine${Constants.SHOWN_NAME_CLOSE_TAG}"
        // I had to apply HTML tags so that swing renders multiline text correctly
    }

    fun resizeIcon(icon: ImageIcon): ImageIcon {
        val image = icon.image
        val imageWidth = image.getWidth(null)
        val imageHeight = image.getHeight(null)

        // If the image's width or height is already equal to Settings.iconSize,
        // return the original icon - do not waste resources on rescaling
        return if (imageWidth == Settings.iconSize || imageHeight == Settings.iconSize) {
            icon
        } else {
            ImageIcon(image.getScaledInstance(Settings.iconSize, Settings.iconSize, Image.SCALE_DEFAULT))
        }
    }
}
