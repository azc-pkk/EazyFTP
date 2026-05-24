package uestc.b3dman.ftpclient.utils

import org.junit.Assert.assertEquals
import org.junit.Test

class FormatterTest {

    @Test
    fun `formatSize zero returns 0 B`() {
        assertEquals("0 B", Formatter.formatSize(0))
    }

    @Test
    fun `formatSize negative returns 0 B`() {
        assertEquals("0 B", Formatter.formatSize(-1))
    }

    @Test
    fun `formatSize bytes`() {
        assertEquals("512.0 B", Formatter.formatSize(512))
    }

    @Test
    fun `formatSize kilobytes`() {
        assertEquals("1.0 KB", Formatter.formatSize(1024))
        assertEquals("1.5 KB", Formatter.formatSize(1536))
    }

    @Test
    fun `formatSize megabytes`() {
        assertEquals("1.0 MB", Formatter.formatSize(1048576))
        assertEquals("2.3 MB", Formatter.formatSize(1048576 * 2 + 314573))
    }

    @Test
    fun `formatSize gigabytes`() {
        assertEquals("1.0 GB", Formatter.formatSize(1073741824))
    }

    @Test
    fun `formatSize terabytes`() {
        assertEquals("1.0 TB", Formatter.formatSize(1099511627776))
    }

    @Test
    fun `formatDate returns formatted string`() {
        val timestamp = 1716998400000L
        val result = Formatter.formatDate(timestamp)
        assert(result.matches(Regex("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}")))
    }

    @Test
    fun `formatDate with epoch zero`() {
        val result = Formatter.formatDate(0)
        assert(result.contains("1970"))
    }
}
