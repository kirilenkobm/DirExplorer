package views.iconviews

import views.directoryviews.IconsDirectoryView
import dataModels.ExplorerDirectory
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import state.ColorTheme

class AbstractIconEntityViewTest {
    private val testDirectory = ExplorerDirectory(System.getProperty("user.home"))
    private val testParentView = IconsDirectoryView()
    private val iconEntityView = DirectoryIconView(testDirectory, testParentView, ColorTheme.LIGHT)

    @Test
    fun resizeIcon() {

    }

    @Test
    fun `setText$DirExplorer`() {
        val result = iconEntityView.setText("someVeryLongFileNameThatDoesNotFit.txt")
        val expectedResult1 = "<html>someVeryLong<br>FileNa...txt</html>"
        assertTrue(result == expectedResult1, "Failed to shorten the filename")

        // TODO: additional tests
    }
}