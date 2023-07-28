package utils

import java.text.SimpleDateFormat
import java.util.*

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
        val regexPattern = filterExtension
            .replace("*", ".*")

        val regex = regexPattern.toRegex()
        val matches = regex.containsMatchIn(entityExtension)
        return if (invertResult) !matches else matches
    }
}
