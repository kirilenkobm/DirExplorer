package model

import DirExplorerTestUtil
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.BeforeEach
import java.nio.file.Path
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue


class ExplorerDirectoryTest {
    private lateinit var tempDir: Path
    private lateinit var explorerDirectory: ExplorerDirectory
    private lateinit var emptyDirectory: ExplorerDirectory

    @Test
    fun getContents() = runBlocking {
        tempDir = DirExplorerTestUtil.setupTestDirectory()
        explorerDirectory = ExplorerDirectory(tempDir.toString())
        emptyDirectory = ExplorerDirectory(tempDir.resolve("emptydir").toString())
        val contents = explorerDirectory.getContents()
        assertEquals(11, contents.size)
        assertFalse(explorerDirectory.isEmpty)
        assertTrue(emptyDirectory.isEmpty)
        DirExplorerTestUtil.teardownTestDirectory()
    }
//    @Test
//    fun getItemsCount() {
//    }
//
//    @Test
//    fun getTotalSize() {
//    }
//
//    @Test
//    fun invalidateCache() {
//    }
//
//    @Test
//    fun getPath() {
//    }
}