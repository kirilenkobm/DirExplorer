package dataModels

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import state.AppState
import java.io.IOException
import java.nio.file.*
import java.nio.file.attribute.BasicFileAttributes
import java.util.UUID
import java.util.zip.ZipFile
import kotlin.coroutines.CoroutineContext


class ZipArchive(override val path: String) : ExplorableEntity, CoroutineScope {
    private val job = Job()
    var tempDir: Path? = null

    private val observer: DirectoryObserver = object : DirectoryObserver {
        override fun onDirectoryChanged(newDirectory: ExplorerDirectory) {
            val tempDir = this@ZipArchive.tempDir
            if (tempDir != null && !newDirectory.path.startsWith(tempDir.toString())) {
                cleanup()
            }
        }
    }

    init {
        // Add listener + track of zipArchives in the app state
        AppState.addDirectoryObserver(observer)
        AppState.addZipArchive(this)
    }

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job


    fun extractTo(): Path {
        val parentDir = Paths.get(path).parent
        // create hidden temp directory
        val tempDirName = "." + Paths.get(path).fileName.toString() + "_" + UUID.randomUUID().toString().take(6)
        tempDir = Files.createDirectory(parentDir.resolve(tempDirName))
        AppState.zipDirMapping[tempDirName] = Paths.get(path).fileName.toString()
        // TODO: handle the case where I cannot create the dir: show ERROR instead

        if (System.getProperty("os.name").startsWith("Windows")) {
            // On Windows: .name is not enough, need to set the 'hidden' attribute
            Files.setAttribute(tempDir!!, "dos:hidden", true, LinkOption.NOFOLLOW_LINKS)
        }

        // Extract zip contents in a background thread
        launch(Dispatchers.IO) {
            ZipFile(path).use { zip ->
                zip.entries().asSequence().forEach { entry ->
                    if (!entry.isDirectory) {
                        val inputFileStream = zip.getInputStream(entry)
                        val outputFile = tempDir!!.resolve(entry.name)
                        Files.createDirectories(outputFile.parent)
                        Files.copy(inputFileStream, outputFile, StandardCopyOption.REPLACE_EXISTING)
                        inputFileStream.close()
                    }
                }
            }
        }

        // Return temp dir before the zip is unzipped
        // TODO: return optional, show error if null
        return tempDir!!  // I hope it's safe...
    }

    fun cleanup() {
        job.cancel()  // cancel the coroutine
        AppState.markObserverForRemoval(observer)
        // Delete the temp directory -> to be called once I left a zip file
        tempDir?.let { dir ->
            Files.walkFileTree(dir, object : SimpleFileVisitor<Path>() {
                // TODO: check safety of this solution
                override fun visitFile(file: Path, attrs: BasicFileAttributes?): FileVisitResult {
                    Files.delete(file)
                    return FileVisitResult.CONTINUE
                }

                override fun postVisitDirectory(dir: Path, exc: IOException?): FileVisitResult {
                    Files.delete(dir)
                    return FileVisitResult.CONTINUE
                }
            })
        }
        tempDir = null
        AppState.zipArchives.remove(this)
    }
}
