package views

import java.awt.BorderLayout
import java.awt.Color
import java.awt.Dimension
import javax.swing.BorderFactory
import javax.swing.JLabel
import javax.swing.JPanel

class StatusBarView : JPanel() {
    private val statusLabel = JLabel()

    init {
        layout = BorderLayout()
        preferredSize = Dimension(1280, 20)
        statusLabel.border = BorderFactory.createEmptyBorder(0, 0, 0, 10)  // padding 10 right
        add(statusLabel, BorderLayout.EAST)
        background = Color.LIGHT_GRAY
    }

    // TODO: avoid code duplication, this method is defined in abstract directory view
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

    fun updateStatus(itemsCount: Int, totalSize: Long) {
        statusLabel.text = "Items: $itemsCount, Total size: ${humanReadableSize(totalSize)}"
    }
}
