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

    @BeforeEach
    fun setup() {
        tempDir = DirExplorerTestUtil.setupTestDirectory()
        explorerDirectory = ExplorerDirectory(tempDir.toString())
        emptyDirectory = ExplorerDirectory(tempDir.resolve("emptydir").toString())
    }

    @AfterEach
    fun teardown() {
        DirExplorerTestUtil.teardownTestDirectory()
    }

    @Test
    fun getContents() = runBlocking {
        val contents = explorerDirectory.getContents()
        assertEquals(11, contents.size)
        // TODO
    }

    @Test
    fun isEmpty() {
        assertFalse(explorerDirectory.isEmpty)
        assertTrue(emptyDirectory.isEmpty)
    }

    @Test
    fun getItemsCount() {
    }

    @Test
    fun getTotalSize() {
    }

    @Test
    fun invalidateCache() {
    }

    @Test
    fun getPath() {
    }
}