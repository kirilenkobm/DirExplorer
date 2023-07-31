package view.popupwindows

import javax.swing.*

/**
 * Standalone function responsible for showing error messages.
 */
fun showErrorDialog(errorMessage: String) {
    JOptionPane.showMessageDialog(
        null,
        errorMessage,
        "Error!",
        JOptionPane.ERROR_MESSAGE
    )
}
