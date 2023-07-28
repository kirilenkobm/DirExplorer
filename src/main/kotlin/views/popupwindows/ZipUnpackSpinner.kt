package views.popupwindows

import java.awt.BorderLayout
import javax.swing.JDialog
import javax.swing.JLabel
import javax.swing.JProgressBar
import javax.swing.SwingConstants
import javax.swing.border.EmptyBorder

/**
 * Object that controls whether a zip unpacking bar is shown or not.
 */
object ZipUnpackSpinner {
    private val progressBar = JProgressBar().apply {
        isIndeterminate = true
        border = EmptyBorder(20, 20, 20, 20)
    }

    private val label = JLabel("Unpacking zip...").apply {
        border = EmptyBorder(10, 10, 10, 10)
        horizontalAlignment = SwingConstants.CENTER
    }

    private val dialog = JDialog().apply {
        setSize(200, 150)
        setLocationRelativeTo(null)
        layout = BorderLayout()
        add(progressBar, BorderLayout.CENTER)
        add(label, BorderLayout.NORTH)
    }

    fun showSpinner() {
        dialog.isVisible = true
    }

    fun hideSpinner() {
        dialog.isVisible = false
    }
}
