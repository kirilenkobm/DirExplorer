package view.popupwindows

import state.Settings
import java.awt.BorderLayout
import java.util.*
import javax.swing.JDialog
import javax.swing.JLabel
import javax.swing.JProgressBar
import javax.swing.SwingConstants
import javax.swing.border.EmptyBorder

/**
 * Controls the visibility of a zip unpacking progress spinner.
 */
object ZipUnpackSpinner {
    private var bundle = ResourceBundle.getBundle(
        Constants.LANGUAGE_BUNDLE_PATH,
        Settings.language.getLocale()
    )

    private val progressBar = JProgressBar().apply {
        isIndeterminate = true
        border = EmptyBorder(20, 20, 20, 20)
    }

    private val label = JLabel(bundle.getString("UnpackingZip")).apply {
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
