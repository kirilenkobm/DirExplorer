package util

import state.AppState
import java.nio.file.Path
import java.nio.file.Paths

/**
 * Values related to the current OS and environment.
 */
object SystemRelatedValues {
    val isWindows = (System.getProperty("os.name").startsWith("Windows"))
    // val rootDir = FileSystems.getDefault().rootDirectories.iterator().next()
    val rootDir: Path = Paths.get(AppState.currentExplorerDirectory.path).root
}
