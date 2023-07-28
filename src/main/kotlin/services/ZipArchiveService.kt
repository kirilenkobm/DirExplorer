package services

import dataModels.DirectoryObserver
import dataModels.ExplorerDirectory
import dataModels.ZipArchive
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import state.AppState
import views.popupwindows.ZipUnpackSpinner
import java.io.IOException
import java.nio.file.*
import java.nio.file.attribute.BasicFileAttributes
import java.util.*
import java.util.zip.ZipFile
import kotlin.coroutines.CoroutineContext


class ZipArchiveService(private val zipEntity: ZipArchive): CoroutineScope {
    private val job = Job()
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    /**
     * Remove temp dir if it's no longer needed
     * by checking whether it's a part of the updated path.
     */
    private val observer: DirectoryObserver = object : DirectoryObserver {
        override fun onDirectoryChanged(newDirectory: ExplorerDirectory) {
//            val tempDir = zipEntity.tempDir
//            if (tempDir != null && !newDirectory.path.startsWith(tempDir.toString())) {
//                cleanup()
//            }
        }
    }

    init {
        // Add listener + track of zipArchives in the app state
        AppState.addDirectoryObserver(observer)
        AppState.addZipArchive(this)
    }

    fun extractTo(): Path? {
        val parentDir = Paths.get(zipEntity.path).parent
        // create hidden temp directory
        val tempDirName = "." + Paths.get(zipEntity.path).fileName.toString() + "_" + UUID.randomUUID().toString().take(6)
        zipEntity.tempDir = Files.createDirectory(parentDir.resolve(tempDirName))
        AppState.tempZipDirToNameMapping[tempDirName] = Paths.get(zipEntity.path).fileName.toString()

        if (System.getProperty("os.name").startsWith("Windows")) {
            // On Windows: .name is not enough, need to set the 'hidden' attribute
            Files.setAttribute(zipEntity.tempDir!!, "dos:hidden", true, LinkOption.NOFOLLOW_LINKS)
        }
        ZipUnpackSpinner.showSpinner()

        val zipUnpackSemaphore = AppState.zipUnpackSemaphore

        // Extract zip contents in a background thread
        launch(Dispatchers.IO) {
            zipUnpackSemaphore.acquire()
            try {
                ZipFile(zipEntity.path).use { zip ->
                    zip.entries().asSequence().forEach { entry ->
                        if (!entry.isDirectory) {
                            val inputFileStream = zip.getInputStream(entry)
                            val outputFile = zipEntity.tempDir!!.resolve(entry.name)
                            Files.createDirectories(outputFile.parent)
                            Files.copy(inputFileStream, outputFile, StandardCopyOption.REPLACE_EXISTING)
                            inputFileStream.close()
                        }
                    }
                    ZipUnpackSpinner.hideSpinner()
                }
            } finally {
                zipUnpackSemaphore.release()
            }
        }
        return zipEntity.tempDir
    }

    fun cleanup() {
        job.cancel()  // cancel the coroutine
        ZipUnpackSpinner.hideSpinner()
        AppState.markObserverForRemoval(observer)
        // Delete the temp directory -> to be called once I left a zip file
        zipEntity.tempDir?.let { dir ->
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
        zipEntity.tempDir = null
        AppState.zipServices.remove(this)
    }
}
