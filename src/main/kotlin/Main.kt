// App's entry point
import state.AppState
import dataModels.ExplorerDirectory
import javax.swing.SwingUtilities
import views.MainView

fun main() {
    SwingUtilities.invokeLater {
        AppState.updateDirectory(ExplorerDirectory(System.getProperty("user.home")))
        MainView().createAndShowGUI()
    }
}
