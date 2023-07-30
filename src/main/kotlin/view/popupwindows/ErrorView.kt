package view.popupwindows

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
