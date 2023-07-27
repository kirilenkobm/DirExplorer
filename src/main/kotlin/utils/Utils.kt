package utils

import java.text.SimpleDateFormat
import java.util.Date

/**
 * Utility object that provides helper functions.
 *
 * E.g. provide functions to generate human-readable representations
 * for time and sizes.
 */
object Utils {
    /**
     * Converts a size in bytes to a human-readable string.
     */
    fun humanReadableSize(bytes: Long): String {
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
        return dateFormat.format(Date(unixTime))
    }
}