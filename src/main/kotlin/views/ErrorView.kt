package views

import state.AppState
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Dimension
import javax.swing.*
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener

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
