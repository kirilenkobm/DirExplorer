package utils

import Constants
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
        val currentUnixTime = System.currentTimeMillis()
        assertEquals("unknown", Utils.formatDate(0))
        assertEquals("unknown", Utils.formatDate(-500000L))
        assertEquals(dateFormat.format(Date(currentUnixTime)), Utils.formatDate(currentUnixTime))
        println(Utils.formatDate(2690677720954L))
        println(Utils.formatDate(2000000000000000L))

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

        fun trimHtmlTags(input: String): String {
            return input.removePrefix(Constants.SHOWN_NAME_OPEN_TAG)
                .removeSuffix(Constants.SHOWN_NAME_CLOSE_TAG)
        }


        val result1 = trimHtmlTags(Utils.getFilenameForIcon("Myreport2023V2.pdf"))
        val result2 = trimHtmlTags(
            Utils.getFilenameForIcon("a.andverylongextensionthatdoesnotfitanywhereonthisplanet")
        )
        val result3 = Utils.getFilenameForIcon("short.txt")
        val result4 = trimHtmlTags(Utils.getFilenameForIcon("line.with.many.dots"))
        val result5 = Utils.getFilenameForIcon("")
        val result6 = trimHtmlTags(Utils.getFilenameForIcon("JustAVeryLongDocumentNameVersion3.docx"))

        assertEquals("Myreport<br>2023V2.pdf", result1)
        assertEquals("a.andveryl<br>o...hisplanet", result2)
        assertEquals("short.txt", result3)
        assertEquals("line.with<br>.many.dots", result4)  // ??? Still room for improvement
        assertEquals(Constants.NONAME_FILE, result5)  // not sure if it's possible
        assertEquals("JustAVeryL<br>o...ion3.docx", result6)
    }

    @Test
    fun resizeIcon() {
    }
}
