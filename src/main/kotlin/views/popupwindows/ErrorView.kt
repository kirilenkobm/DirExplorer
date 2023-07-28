package views.popupwindows

import state.ColorTheme
import state.Settings
import java.awt.Color
import javax.swing.*

// Standalone function to show a popup error view
fun showErrorDialog(errorMessage: String) {
    JOptionPane.showMessageDialog(
        null,
        errorMessage,
        "Error!",
        JOptionPane.ERROR_MESSAGE
    )
}
