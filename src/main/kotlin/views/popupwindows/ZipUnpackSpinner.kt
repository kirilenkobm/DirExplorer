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
    private val dialog = JDialog().apply {
        setSize(200, 150)
        setLocationRelativeTo(null) // Center on screen
        layout = BorderLayout()
    }

    // Function to show the spinner
    fun showSpinner(){
        // Create a JDialog


        // Create a JProgressBar
        val progressBar = JProgressBar().apply {
            isIndeterminate = true // Set to indeterminate mode
            border = EmptyBorder(20, 20, 20, 20) // Add some padding
        }

        // Create a JLabel for the message
        val label = JLabel("Unpacking zip...").apply {
            border = EmptyBorder(10, 10, 10, 10) // Add some padding
            horizontalAlignment = SwingConstants.CENTER // Center the text
        }

        // Add the progress bar and label to the dialog
        dialog.add(progressBar, BorderLayout.CENTER)
        dialog.add(label, BorderLayout.NORTH) // Add the label above the progress bar

        // Show the dialog
        dialog.isVisible = true
    }

    fun hideSpinner() {
        dialog.isVisible = false
    }
}
