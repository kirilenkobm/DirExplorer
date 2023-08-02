// App's entry point
import state.AppState
import model.ExplorerDirectory
import state.AppStateUpdater
import state.Settings
import javax.swing.SwingUtilities
import view.MainView
import java.nio.file.Files
import java.nio.file.Paths

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
        if (args.isNotEmpty()) {
            // If a command-line argument is provided, use it as the initial directory
            val path = Paths.get(args[0])
            // But first, check whether it's a valid path
            if (Files.exists(path)) {
                AppStateUpdater.updateDirectory(ExplorerDirectory(args[0]))
            } else {
                // if no -> show err message and redirect to the home directory
                System.err.println("The provided path does not exist: $args[0]")
                AppState.goHome()
            }
        } else {
            // Otherwise, use the home directory as the initial directory
            AppState.goHome()
        }
        MainView().createAndShowGUI()
    }
    // Operations to be performed after closing the app
    Runtime.getRuntime().addShutdownHook(Thread {
        Settings.saveSettings()  // dump settings for the next session
        AppState.cleanupAllZipArchives()  // to make sure all temp dirs are deleted
    })
}
