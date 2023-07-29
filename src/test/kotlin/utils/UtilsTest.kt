package utils

import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import java.text.SimpleDateFormat
import java.util.*

class UtilsTest {

    @Test
    fun humanReadableSize() {
        assertEquals("empty", Utils.humanReadableSize(0L))
        assertEquals("1023 B", Utils.humanReadableSize(1023L))
        assertEquals("1.0 KB", Utils.humanReadableSize(1024L))
        assertEquals("1.0 MB", Utils.humanReadableSize(1048576L))
        assertEquals("325.3 MB", Utils.humanReadableSize(341049999L))
        assertEquals("1.0 GB", Utils.humanReadableSize(1073741824L))
        assertEquals("1.4 GB", Utils.humanReadableSize(1555741824L))
        assertEquals("1.0 TB", Utils.humanReadableSize(1099511627776L))
        assertEquals("undefined", Utils.humanReadableSize(-10000L))
    }
    @Test
    fun formatDate() {
        val dateFormat = SimpleDateFormat("yyyy.MM.dd 'at' HH:mm:ss")
        val unixTime = System.currentTimeMillis()
        assertEquals("unknown", Utils.formatDate(0))
        assertEquals("unknown", Utils.formatDate(-500000L))
        assertEquals(dateFormat.format(Date(unixTime)), Utils.formatDate(unixTime))
    }

    @Test
    fun matchesExtension() {
        assertTrue(Utils.matchesExtension("txt", "txt"))
        assertTrue(Utils.matchesExtension("TXT", "txt"))
        assertTrue(Utils.matchesExtension("txt", "TXT"))
        assertFalse(Utils.matchesExtension("txt", "~txt"))
        assertTrue(Utils.matchesExtension("txt", "*"))
        assertTrue(Utils.matchesExtension("txt", "t*t"))
        assertTrue(Utils.matchesExtension("pdf", "p*"))
        assertFalse(Utils.matchesExtension("txt", "doc"))
    }

    @Test
    fun getFilenameForIcon() {
        assertEquals("<html>txt</html>", Utils.getFilenameForIcon("txt"))
        assertEquals("<html>short.txt</html>", Utils.getFilenameForIcon("short.txt"))
        assertEquals("<html>verylongfi<br>lename.txt</html>", Utils.getFilenameForIcon("verylongfilename.txt"))
        assertEquals("<html>verylongfi<br>lena...txt</html>", Utils.getFilenameForIcon("verylongfilenamethatdoesnotfit.anywhere.txt"))
        assertEquals("<html></html>", Utils.getFilenameForIcon(""))
    }

    @Test
    fun resizeIcon() {
    }
}
