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

    fun updateStatus(itemsCount: Int, totalSize: Long) {
        statusLabel.text = "Items: $itemsCount, Total size: ${Utils.humanReadableSize(totalSize)}"
    }
}
