// App's entry point
import state.AppState
import java.nio.file.Paths
import java.nio.file.Files
import dataModels.ExplorerDirectory
import dataModels.ExplorerFile

fun main(args: Array<String>) {
    println("Starting the DirExplorer...")
    AppState.updateDirectory(ExplorerDirectory(System.getProperty("user.home")))

    while (true) {
        println("Current directory: ${AppState.currentExplorerDirectory.path}")
        // Print files in current directory
        AppState.currentExplorerDirectory.showAllContents()
        print("Enter path to navigate to (or 'exit' to quit): ")
        val input = readlnOrNull()
        if (input == "exit") {
            break
        } else if (input != null) {
            val newPath = Paths.get(AppState.currentExplorerDirectory.path, input)
            if (Files.exists(newPath) && Files.isDirectory(newPath)) {
                AppState.updateDirectory(ExplorerDirectory(newPath.toString()))
            } else {
                println("Invalid path. Please try again.")
            }
        } else {
            break
        }
    }

    println("Exiting the DirExplorer")
}
