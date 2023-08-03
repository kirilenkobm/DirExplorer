package view.popupwindows

import state.ColorTheme
import state.Settings
import java.awt.Color
import java.awt.Font
import javax.swing.BorderFactory
import javax.swing.JDialog
import javax.swing.JFrame
import javax.swing.JLabel
import javax.swing.SwingUtilities


/**
 * Popup dialog that shows up after the x button click if removing temporary
 * directories (that are created to show the content of zip archives) takes
 * too long.
 */
fun showClosingDialog(parent: JFrame) {
    SwingUtilities.invokeLater {
        val dialog = JDialog(parent, "Application is closing", true)
        val label = JLabel("Please wait until all temporary directories are deleted...")
        label.foreground = if (Settings.colorTheme == ColorTheme.LIGHT) Color.BLACK  else  Color.WHITE
        label.font = Font("Arial", Font.PLAIN, 18) // Change the font
        label.border = BorderFactory.createEmptyBorder(10, 10, 10, 10) // Add some padding around the text

        // adjust background
        dialog.background = if (Settings.colorTheme == ColorTheme.LIGHT) Color.LIGHT_GRAY  else  Color.DARK_GRAY
        dialog.contentPane = label
        dialog.isUndecorated = true
        dialog.pack()
        dialog.setLocationRelativeTo(parent)
        dialog.isVisible = true
    }
}
