package uestc.b3dman.ftpclient.ui.screens.history

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import uestc.b3dman.ftpclient.data.model.DownloadHistoryEntry
import uestc.b3dman.ftpclient.data.model.DownloadStatus

class DownloadHistoryEntryMapperTest {

    @Test
    fun `toUiState maps success entry correctly`() {
        val entry = DownloadHistoryEntry(
            id = 1,
            fileName = "report.pdf",
            remotePath = "/docs/report.pdf",
            localPath = "/storage/downloads/report.pdf",
            fileSize = 2048000,
            downloadTime = 1716998400000L,
            accountId = 100,
            status = DownloadStatus.SUCCESS,
        )
        val uiState = entry.toUiState()
        assertEquals(1, uiState.id)
        assertEquals("report.pdf", uiState.fileName)
        assertEquals("/docs/report.pdf", uiState.remotePath)
        assertEquals("/storage/downloads/report.pdf", uiState.localPath)
        assertEquals(100, uiState.accountId)
        assertEquals(DownloadStatus.SUCCESS, uiState.status)
        assertTrue(uiState.isSuccess)
    }

    @Test
    fun `toUiState maps failed entry correctly`() {
        val entry = DownloadHistoryEntry(
            id = 2,
            fileName = "broken.zip",
            remotePath = "/broken.zip",
            localPath = "/storage/downloads/broken.zip",
            fileSize = 0,
            downloadTime = 0,
            accountId = 100,
            status = DownloadStatus.FAILED,
        )
        val uiState = entry.toUiState()
        assertEquals(DownloadStatus.FAILED, uiState.status)
        assertFalse(uiState.isSuccess)
    }

    @Test
    fun `toUiState maps downloading entry correctly`() {
        val entry = DownloadHistoryEntry(
            id = 3,
            fileName = "inprogress.iso",
            remotePath = "/iso/inprogress.iso",
            localPath = "/storage/downloads/inprogress.iso",
            fileSize = 512000000,
            downloadTime = 1716998400000L,
            accountId = 100,
            status = DownloadStatus.DOWNLOADING,
        )
        val uiState = entry.toUiState()
        assertEquals(DownloadStatus.DOWNLOADING, uiState.status)
        assertFalse(uiState.isSuccess)
    }

    @Test
    fun `toUiState formats file size`() {
        val entry = DownloadHistoryEntry(
            id = 4,
            fileName = "data.bin",
            remotePath = "/data.bin",
            localPath = "/data.bin",
            fileSize = 1048576,
            downloadTime = 0,
            accountId = 1,
            status = DownloadStatus.SUCCESS,
        )
        val uiState = entry.toUiState()
        assertEquals("1.0 MB", uiState.fileSize)
    }

    @Test
    fun `toUiState formats date`() {
        val entry = DownloadHistoryEntry(
            id = 5,
            fileName = "old.txt",
            remotePath = "/old.txt",
            localPath = "/old.txt",
            fileSize = 100,
            downloadTime = 1716998400000L,
            accountId = 1,
            status = DownloadStatus.SUCCESS,
        )
        val uiState = entry.toUiState()
        assert(uiState.downloadTime.contains("2024"))
    }
}
