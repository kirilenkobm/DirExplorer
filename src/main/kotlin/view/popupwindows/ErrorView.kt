package view.popupwindows

import state.Settings
import java.util.*
import javax.swing.*

/**
 * Standalone function responsible for showing error messages.
 */
fun showErrorDialog(errorMessage: String) {
    val bundle = ResourceBundle.getBundle(Constants.LANGUAGE_BUNDLE_PATH, Settings.language.getLocale())

    JOptionPane.showMessageDialog(
        null,
        errorMessage,
        "${bundle.getString("Error")}!",
        JOptionPane.ERROR_MESSAGE
    )
}
