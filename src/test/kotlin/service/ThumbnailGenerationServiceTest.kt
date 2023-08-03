package service

import model.ExplorerFile
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import state.Settings
import view.iconviews.FileIconView
import java.awt.image.BufferedImage
import javax.swing.Icon
import kotlin.math.max


class ThumbnailGenerationServiceTest {
    @Test
    fun createImageThumbnailCreatesThumbnail() {
        val pathToImageWithEmbeddedThumbnail = "src/test/resources/imageWithThumbnail.jpg"
        val dummyFileEntity = ExplorerFile(pathToImageWithEmbeddedThumbnail)
        val dummyIcon = FileIconView(dummyFileEntity)
        val generatorInstance = ThumbnailGenerationService(dummyFileEntity, dummyIcon)
        var thumbnail: Icon?

        // For this image, a thumbnail must be generated
        runBlocking {
            thumbnail = generatorInstance.createImageThumbnail()
            assertNotNull(thumbnail)
            // Get the dimensions of the thumbnail.
        }
        if (thumbnail != null) {
            val width = thumbnail!!.iconWidth
            val height = thumbnail!!.iconHeight
            val maxDimension = max(width, height)
            assertEquals(Settings.iconSize, maxDimension)
        }
    }

    @Test
    fun createImageThumbnailSkipsHugeImages() {
        // Image with long-side > 6000 must be skipped, expected null
        val pathToVeryBigImage = "src/test/resources/tooWideImage.jpg"
        val dummyFileEntity = ExplorerFile(pathToVeryBigImage)
        val dummyIcon = FileIconView(dummyFileEntity)
        val generatorInstance = ThumbnailGenerationService(dummyFileEntity, dummyIcon)
        var thumbnail: Icon?
        runBlocking {
            thumbnail = generatorInstance.createImageThumbnail()
            assertNull(thumbnail)
        }
    }

    @Test
    fun testResizeThumbnail() {
        val pathToVeryBigImage = "src/test/resources/tooWideImage.jpg"
        val dummyFileEntity = ExplorerFile(pathToVeryBigImage)
        val dummyIcon = FileIconView(dummyFileEntity)
        val generatorInstance = ThumbnailGenerationService(dummyFileEntity, dummyIcon)

        // Create squared image, check whether it gets rescaled
        val squareImage = BufferedImage(100, 100, BufferedImage.TYPE_INT_ARGB)
        val resizedImage = generatorInstance.resizeThumbnail(squareImage)
        assertEquals(72, resizedImage.iconWidth)
        assertEquals(72, resizedImage.iconHeight)

        // Check whether works well with images smaller than needed
        val smallImage = BufferedImage(33, 33, BufferedImage.TYPE_INT_ARGB)
        val resizedSmallImage = generatorInstance.resizeThumbnail(smallImage)
        assertEquals(72, resizedSmallImage.iconWidth)
        assertEquals(72, resizedSmallImage.iconHeight)

        // Check whether ratio is preserved
        val disproportionalImage = BufferedImage(500, 720, BufferedImage.TYPE_INT_RGB)
        val resizedDisproportionalImage = generatorInstance.resizeThumbnail(disproportionalImage)
        assertEquals(50, resizedDisproportionalImage.iconWidth)
        assertEquals(72, resizedDisproportionalImage.iconHeight)
    }
}