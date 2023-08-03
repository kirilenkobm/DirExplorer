import state.AppState
import model.ExplorerDirectory
import state.AppStateUpdater
import state.Settings
import javax.swing.SwingUtilities
import view.MainView

/**
 * The main entry point of the application.
 *
 * It initializes the application settings, sets up the initial directory
 * based on command-line arguments or defaults to the home directory, and launches the UI.
 */
fun main(args: Array<String>) {
    // Load settings if possible when the app starts
    Settings.loadSettings()

    // UI entry point
    SwingUtilities.invokeLater {
        // If a command-line argument is provided, use it as the initial directory
        if (args.isNotEmpty()) {
            // TODO: check whether it's a path, otherwise show err message
            AppStateUpdater.updateDirectory(ExplorerDirectory(args[0]))
        } else {
            // Otherwise, use the home directory as the initial directory
            AppState.goHome()
        }
        MainView().createAndShowGUI()
    }
    // save Settings each time I close the app
    Runtime.getRuntime().addShutdownHook(Thread {
        Settings.saveSettings()  // dump settings for the next session
        AppState.cleanupAllZipArchives()  // to make sure all temp dirs are deleted
    })
}
