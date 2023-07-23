package views

import javax.swing.JOptionPane

// Standalone function to show a popup
// error view
fun showErrorDialog(errorMessage: String) {
    JOptionPane.showMessageDialog(
        null,
        errorMessage,
        "Error!",
        JOptionPane.ERROR_MESSAGE
    )
}
