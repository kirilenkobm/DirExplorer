package model

import org.junit.jupiter.api.Test
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.streams.asSequence
import kotlin.test.assertTrue

class FileSystemEntityFactoryTest {
    private lateinit var tempDir: Path
    private lateinit var explorerDirectory: ExplorerDirectory

    @Test
    fun createEntity() {
        tempDir = DirExplorerTestUtil.setupTestDirectory()
        explorerDirectory = ExplorerDirectory(tempDir.toString())
        val filesInDir = Files.list(Paths.get(tempDir.toString())).asSequence()
        for (elem in filesInDir) {
            val respectiveEntity = FileSystemEntityFactory.createEntity(elem.toString())

            // Check the type of the respectiveEntity
            when (elem.fileName.toString()) {
                "file2.txt", "file1.txt", "file.log", "document1.pdf", "image2.jpeg", "image1.png" -> {
                    assertTrue(respectiveEntity is ExplorerFile)
                }
                "root_to_subfile1" -> {
                    assertTrue(respectiveEntity is ExplorerSymLink)
                }
                "subdir2", "emptydir", "subdir1" -> {
                    assertTrue(respectiveEntity is ExplorerDirectory)
                }
                "archive.zip" -> {
                    assertTrue(respectiveEntity is ZipArchive)
                }
            }
        }
        DirExplorerTestUtil.teardownTestDirectory()
    }
}
