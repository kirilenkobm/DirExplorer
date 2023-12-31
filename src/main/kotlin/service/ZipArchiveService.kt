package service

import model.DirectoryObserver
import model.ExplorerDirectory
import model.ZipArchive
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import state.AppState
import state.Settings
import util.SystemRelatedValues
import view.popupwindows.showErrorDialog
import java.io.IOException
import java.nio.file.*
import java.nio.file.attribute.BasicFileAttributes
import java.util.*
import java.util.zip.ZipException
import java.util.zip.ZipFile
import javax.swing.SwingUtilities
import kotlin.coroutines.CoroutineContext

/**
 * Service class for managing zip archives in the application.
 *
 * This class is responsible for extracting the contents of a zip archive into a temporary directory
 * and cleaning up the temporary directory when it's no longer needed.
 * The directory considered to be no longer needed if it is not present in any path in the
 * back or forward history stack of the AppState, and is not present in the current path.
 *
 * The extraction process is performed asynchronously.
 *
 * All ZipArchiveService instances are tracked in the AppState's zipServices
 * mutable list to remove all temporary directories when the session is complete.
 */
class ZipArchiveService(private val zipEntity: ZipArchive): CoroutineScope {
    private var bundle = ResourceBundle.getBundle(Constants.LANGUAGE_BUNDLE_PATH, Settings.language.getLocale())
    private val job = Job()
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO + job
    var tempDirName: String? = null
    val extractionStatus = MutableStateFlow(ZipExtractionStatus.NOT_YET_STARTED)
    private var wasCleanedUp = false
    private val cleanupMutex = Mutex()
    /**
     * Remove temp dir if it's no longer needed.
     * If they are not in the back or forward stack.
     */
    private val observer: DirectoryObserver = object : DirectoryObserver {
        override fun onDirectoryChanged(newDirectory: ExplorerDirectory) {
            val tempDir = zipEntity.tempDir ?: return
            // clean the temporary directory up if no longer in back and forth stacks
            val isHereRightNow = AppState.currentExplorerDirectory.path == zipEntity.tempDir.toString()
            val isInBackStack = AppState.backStack.any { it.path.startsWith(tempDir.toString()) }
            val isInForwardStack = AppState.forwardStack.any { it.path.startsWith(tempDir.toString()) }

            // if not in this dir, not in all stakes: cleanup
            if (!isHereRightNow && !isInBackStack && !isInForwardStack && !wasCleanedUp) {
                wasCleanedUp = true
                cleanup()  // and do not forget to exclude them from caches:
                AppState.zipPathToTempDir.remove(zipEntity.path)
                AppState.tempZipDirToNameMapping.remove(tempDirName)
                AppState.tempZipDirToServiceMapping.remove(tempDirName)
            }
        }
    }

    init {
        // Add listener + track of zipArchives in the app state
        AppState.addDirectoryObserver(observer)
        AppState.addZipArchive(this)
    }

    fun startExtraction(): Path? {
        val parentDir = Paths.get(zipEntity.path).parent
        // create hidden temp directory
        tempDirName = ".${Paths.get(zipEntity.path).fileName}_${UUID.randomUUID().toString().take(8)}"
        zipEntity.tempDir = Files.createDirectory(parentDir.resolve(tempDirName!!))
        AppState.tempZipDirToNameMapping[tempDirName!!] = Paths.get(zipEntity.path).fileName.toString()
        AppState.tempZipDirToServiceMapping[tempDirName!!] = this

        if (SystemRelatedValues.isWindows) {
            // On Windows: .name is not enough, need to set the 'hidden' attribute
            Files.setAttribute(zipEntity.tempDir!!, "dos:hidden", true, LinkOption.NOFOLLOW_LINKS)
        }

        // Start coroutine: get semaphore + start spinner
        val zipUnpackSemaphore = SemaphoreManager.zipUnpackSemaphore

        // Extract zip contents in a background thread
        launch(Dispatchers.IO) {
            zipUnpackSemaphore.acquire()
            extractionStatus.value = ZipExtractionStatus.IN_PROGRESS
            var lastRefreshTime = System.currentTimeMillis()
            var refreshDelay = 750L  // initial delay

            try {
                ZipFile(zipEntity.path).use { zip ->
                    zip.entries().asSequence().forEach { entry ->
                        ensureActive()  // to force coroutine to check whether it's cancelled I guess
                        if (entry.isDirectory) {
                            // Create the directory
                            Files.createDirectories(zipEntity.tempDir!!.resolve(entry.name))
                        } else {
                            // Extract the file
                            val inputFileStream = zip.getInputStream(entry)
                            val outputFile = zipEntity.tempDir!!.resolve(entry.name)
                            Files.createDirectories(outputFile.parent)
                            Files.copy(inputFileStream, outputFile, StandardCopyOption.REPLACE_EXISTING)
                            inputFileStream.close()
                        }
                        // Refresh the directory if more than 500ms have passed since the last refresh
                        val currentTime = System.currentTimeMillis()
                        if (currentTime - lastRefreshTime > refreshDelay) {
                            val currentService = AppState.getZipServiceForDirectory()
                            if (currentService == this@ZipArchiveService) {
                                // However, refresh iff the current directory depends on this
                                // particular zipArchiveService
                                AppState.refreshCurrentDirectory()
                                lastRefreshTime = currentTime
                                // increase delay by 20% after each refresh
                                // so that I don't get system overloaded on big archives
                                refreshDelay = (refreshDelay * 1.2).toLong()
                            }
                        }
                    }
                }
                extractionStatus.value = ZipExtractionStatus.DONE
            } catch (e: ZipException) {
                println("Failed to extract zip file due to: ${e.message}")
                extractionStatus.value = ZipExtractionStatus.FAILED
                SwingUtilities.invokeLater {
                    showErrorDialog("${bundle.getString("FailedToExtractDueTo")} ${e.message}")
                }
            } finally {
                AppState.refreshCurrentDirectory()
                zipUnpackSemaphore.release()
            }
        }
        return zipEntity.tempDir
    }

    fun cleanup(): Deferred<Unit> {
        job.cancel()  // cancel the original coroutine

        val cleanupJob = Job()  // create a new job for the cleanup operation
        AppState.markObserverForRemoval(observer)
        println("Cleaning up $tempDirName")
        // Launch a new coroutine on the IO dispatcher
        // Delete the temp directory -> to be called once I left a zip file
        return async(Dispatchers.IO + cleanupJob) {
            zipEntity.tempDir?.let { dir ->
                // in case cleanup was called twice
                // then two processes get almost the same set of files
                cleanupMutex.withLock {
                    try {
                        Files.walkFileTree(dir, object : SimpleFileVisitor<Path>() {
                            override fun visitFile(file: Path, attrs: BasicFileAttributes?): FileVisitResult {
                                Files.deleteIfExists(file)
                                return FileVisitResult.CONTINUE
                            }

                            override fun postVisitDirectory(dir: Path, exc: IOException?): FileVisitResult {
                                Files.deleteIfExists(dir)
                                return FileVisitResult.CONTINUE
                            }
                        })
                    } catch (e: IOException) {
                        println("Failed to delete file: ${e.message}")
                    } finally {
                        // Once the file deletion is done, update the UI on the main thread
                        zipEntity.tempDir = null
                        AppState.zipServices.remove(this@ZipArchiveService)
                        println("Cleanup finished for $tempDirName")
                    }
                }
            }
        }
    }
}
