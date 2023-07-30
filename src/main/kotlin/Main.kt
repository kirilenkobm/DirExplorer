// App's entry point
import state.AppState
import dataModels.ExplorerDirectory
import state.Settings
import javax.swing.SwingUtilities
import views.MainView


fun main(args: Array<String>) {
    // Load settings if possible when the app starts
    Settings.loadSettings()

    // UI entry point
    SwingUtilities.invokeLater {
        // If a command-line argument is provided, use it as the initial directory
        if (args.isNotEmpty()) {
            // TODO: check whether it's a path, otherwise show err message
            AppState.updateDirectory(ExplorerDirectory(args[0]))
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
