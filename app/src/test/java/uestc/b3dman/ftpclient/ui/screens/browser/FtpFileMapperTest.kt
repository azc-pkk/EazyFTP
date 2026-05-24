package uestc.b3dman.ftpclient.ui.screens.browser

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import uestc.b3dman.ftpclient.data.model.FtpFileItem

class FtpFileMapperTest {

    @Test
    fun `toUiState maps folder correctly`() {
        val item = FtpFileItem(
            name = "Documents",
            isFolder = true,
            lastUpdateTime = 1716998400000L,
            size = 4096,
            fullPath = "/home/Documents"
        )
        val uiState = item.toUiState()
        assertEquals("Documents", uiState.name)
        assertTrue(uiState.isFolder)
        assertEquals("/home/Documents", uiState.fullPath)
    }

    @Test
    fun `toUiState maps file correctly`() {
        val item = FtpFileItem(
            name = "readme.txt",
            isFolder = false,
            lastUpdateTime = 1716998400000L,
            size = 2048,
            fullPath = "/home/readme.txt"
        )
        val uiState = item.toUiState()
        assertEquals("readme.txt", uiState.name)
        assertFalse(uiState.isFolder)
        assertEquals("/home/readme.txt", uiState.fullPath)
    }

    @Test
    fun `toUiState formats size correctly`() {
        val item = FtpFileItem(
            name = "large.bin",
            isFolder = false,
            lastUpdateTime = 0,
            size = 1048576,
            fullPath = "/large.bin"
        )
        val uiState = item.toUiState()
        assertEquals("1.0 MB", uiState.size)
    }

    @Test
    fun `toUiState formats date correctly`() {
        val item = FtpFileItem(
            name = "file.txt",
            isFolder = false,
            lastUpdateTime = 1716998400000L,
            size = 100,
            fullPath = "/file.txt"
        )
        val uiState = item.toUiState()
        assert(uiState.lastUpdateTime.contains("2024"))
    }
}
