package uestc.b3dman.ftpclient.ui.screens.browser

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FileActionTest {

    @Test
    fun `FileAction enum has three values`() {
        val values = FileAction.entries
        assertEquals(3, values.size)
        assertTrue(values.contains(FileAction.DOWNLOAD))
        assertTrue(values.contains(FileAction.RENAME))
        assertTrue(values.contains(FileAction.DELETE))
    }

    @Test
    fun `SortType enum has three values`() {
        val values = SortType.entries
        assertEquals(3, values.size)
    }

    @Test
    fun `SortType NAME has correct display name`() {
        assertEquals("\u6309\u540D\u79F0", SortType.NAME.displayName)
    }

    @Test
    fun `SortType SIZE has correct display name`() {
        assertEquals("\u6309\u5927\u5C0F", SortType.SIZE.displayName)
    }

    @Test
    fun `SortType TIME has correct display name`() {
        assertEquals("\u6309\u65F6\u95F4", SortType.TIME.displayName)
    }
}
