package service

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import model.ExplorerFile
import org.junit.jupiter.api.Test
import view.iconviews.FileIconView


class ThumbnailJobControllerTest {
    private fun makeMockFileIconView(): FileIconView {
        val mockFile = ExplorerFile("path")
        return FileIconView(mockFile)
    }

    /**
     * Tests the thread safety of the ThumbnailJobController.
     *
     * This test launches 10,000 coroutines, each of which adds and removes a FileIconView 100 times.
     * This results in a total of 1,000,000 add and remove operations.
     *
     * The purpose of this test is to try to trigger a ConcurrentModificationException, which would indicate
     * a race condition in the ThumbnailJobController. If the ThumbnailJobController is thread-safe, this test
     * should pass without throwing any exceptions.
     *
     * Note: This test could take a long time to run due to the large number of operations.
     */
    @Test
    fun testThreadSafetyForThumbnailController() = runBlocking {
        val jobController = ThumbnailJobController
        val jobs = List(10000) {
            launch(Dispatchers.Default) {
                repeat(100) {
                    jobController.addFileIconView(makeMockFileIconView())
                    jobController.cancelThumbnailGenerationTasks()
                }
            }
        }
        jobs.forEach { it.join() }
    }
}
