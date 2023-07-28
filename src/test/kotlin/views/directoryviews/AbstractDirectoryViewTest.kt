package views.directoryviews

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class AbstractDirectoryViewTest {
    private val abstractDirectoryView = GridDirectoryView()

    @Test
    fun `matchesExtension$DirExplorer`() {
        assertTrue(abstractDirectoryView.matchesExtension("jpg", "jpg"), "Failed to match the same extensions")

        assertFalse(abstractDirectoryView.matchesExtension("jpg", "png"), "Incorrectly matched different extensions")

        assertTrue(abstractDirectoryView.matchesExtension("jpg", "*"), "Failed to match a wildcard filter")

        assertTrue(abstractDirectoryView.matchesExtension("png.jpg", "jpg"), "Failed to match partial extension")

        assertTrue(abstractDirectoryView.matchesExtension("png.jpg", "~png"), "Failed to invert matching result")

        assertTrue(abstractDirectoryView.matchesExtension("jpg", "~png"), "Failed to invert matching result")

        assertFalse(abstractDirectoryView.matchesExtension("png", "~png"), "Incorrectly matched inverted extensions")

        assertTrue(abstractDirectoryView.matchesExtension("png", "p.*"), "Failed to match regex pattern in filter")
    }

    @Test
    fun filterAndSortContents() {
    }
}
