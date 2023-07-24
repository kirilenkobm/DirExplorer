package dataModels

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import state.SortOrder
import java.io.IOException
import java.nio.file.*
import java.nio.file.attribute.BasicFileAttributes
import java.util.UUID
import java.util.zip.ZipInputStream
import kotlin.coroutines.CoroutineContext


class ZipArchive(override val path: String) : ExplorableEntity, CoroutineScope {
    override var sortOrder: SortOrder = SortOrder.TYPE
    private val job = Job()
    private var tempDir: Path? = null

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job


    fun extractTo(): Path {
        val parentDir = Paths.get(path).parent
        val tempDirName = Paths.get(path).fileName.toString() + "_" + UUID.randomUUID().toString().take(6)
        tempDir = Files.createDirectory(parentDir.resolve(tempDirName))
        // TODO: handle the case where I cannot create the dir: show ERROR instead

        // Extract zip contents in a background thread
        launch(Dispatchers.IO) {
            ZipInputStream(Files.newInputStream(Paths.get(path))).use { zis ->
                generateSequence { zis.nextEntry }.forEach { entry ->
                    if (!entry.isDirectory) {
                        val outputFile = tempDir!!.resolve(entry.name)
                        Files.createDirectories(outputFile.parent)
                        Files.copy(zis, outputFile, StandardCopyOption.REPLACE_EXISTING)
                    }
                }
            }
        }
        // Return temp dir before the zip is unzipped
        return tempDir!!  // I hope it's safe...
    }

    fun cleanup() {
        job.cancel()  // cancel the coroutine
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
    }
}
