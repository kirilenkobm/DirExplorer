package util

import state.AppState
import java.nio.file.Path
import java.nio.file.Paths

/**
 * Values related to the current OS and environment.
 */
object SystemRelatedValues {
    val isWindows: Boolean = (System.getProperty("os.name").startsWith("Windows"))
    val homeDirectory: String = System.getProperty("user.home")
    fun rootDir(): Path {
        return Paths.get(AppState.currentExplorerDirectory.path).root
    }
}
