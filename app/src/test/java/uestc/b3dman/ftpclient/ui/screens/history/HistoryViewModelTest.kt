package uestc.b3dman.ftpclient.ui.screens.history

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import uestc.b3dman.ftpclient.data.model.DownloadHistoryEntry
import uestc.b3dman.ftpclient.data.model.DownloadStatus
import uestc.b3dman.ftpclient.data.repository.FtpRepository

@OptIn(ExperimentalCoroutinesApi::class)
class HistoryViewModelTest {

    private val repository: FtpRepository = mockk(relaxed = true)
    private val testDispatcher = StandardTestDispatcher()
    private lateinit var viewModel: HistoryViewModel

    private val successEntry = DownloadHistoryEntry(
        id = 1, fileName = "success.pdf", remotePath = "/docs/success.pdf",
        localPath = "/local/success.pdf", fileSize = 1024,
        downloadTime = 1000, accountId = 1, status = DownloadStatus.SUCCESS,
    )

    private val failedEntry = DownloadHistoryEntry(
        id = 2, fileName = "failed.zip", remotePath = "/failed.zip",
        localPath = "/local/failed.zip", fileSize = 512,
        downloadTime = 2000, accountId = 1, status = DownloadStatus.FAILED,
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        every { repository.getDownloadHistory(1) } returns flowOf(listOf(successEntry, failedEntry))
        every { repository.getDownloadHistory(2) } returns flowOf(emptyList())
        viewModel = HistoryViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial history is empty`() = runTest {
        assertEquals(0, viewModel.history.value.size)
    }

    @Test
    fun `setAccount loads history`() = runTest {
        val scope = CoroutineScope(testDispatcher)
        scope.launch { viewModel.history.collect {} }
        viewModel.setAccount(1)
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(2, viewModel.history.value.size)
    }

    @Test
    fun `history maps entry fields correctly`() = runTest {
        val scope = CoroutineScope(testDispatcher)
        scope.launch { viewModel.history.collect {} }
        viewModel.setAccount(1)
        testDispatcher.scheduler.advanceUntilIdle()
        val entries = viewModel.history.value
        assertEquals(2, entries.size)
        assertEquals("success.pdf", entries[0].fileName)
        assertEquals(DownloadStatus.SUCCESS, entries[0].status)
        assertTrue(entries[0].isSuccess)
        assertEquals("failed.zip", entries[1].fileName)
        assertEquals(DownloadStatus.FAILED, entries[1].status)
        assertFalse(entries[1].isSuccess)
    }

    @Test
    fun `onClickEntry for success opens file`() = runTest {
        val scope = CoroutineScope(testDispatcher)
        scope.launch { viewModel.history.collect {} }
        viewModel.setAccount(1)
        testDispatcher.scheduler.advanceUntilIdle()
        every { repository.openDownloadedFile("/local/success.pdf") } returns true
        viewModel.onClickEntry(viewModel.history.value[0])
        verify { repository.openDownloadedFile("/local/success.pdf") }
    }

    @Test
    fun `onClickEntry for failed shows retry dialog`() = runTest {
        val scope = CoroutineScope(testDispatcher)
        scope.launch { viewModel.history.collect {} }
        viewModel.setAccount(1)
        testDispatcher.scheduler.advanceUntilIdle()
        viewModel.onClickEntry(viewModel.history.value[1])
        assertNotNull(viewModel.retryTargetEntry.value)
        assertEquals("failed.zip", viewModel.retryTargetEntry.value?.fileName)
    }

    @Test
    fun `onClickEntry for downloading is no-op`() = runTest {
        val downloadingEntry = DownloadHistoryEntry(
            id = 3, fileName = "inprogress.iso", remotePath = "/inprogress.iso",
            localPath = "/local/inprogress.iso", fileSize = 2048,
            downloadTime = 3000, accountId = 1, status = DownloadStatus.DOWNLOADING,
        )
        every { repository.getDownloadHistory(1) } returns flowOf(listOf(downloadingEntry))
        val scope = CoroutineScope(testDispatcher)
        scope.launch { viewModel.history.collect {} }
        viewModel.setAccount(1)
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(1, viewModel.history.value.size)
        viewModel.onClickEntry(viewModel.history.value[0])
        assertNull(viewModel.retryTargetEntry.value)
    }

    @Test
    fun `retryTargetEntry is null initially`() = runTest {
        assertNull(viewModel.retryTargetEntry.value)
    }

    @Test
    fun `onRetryDismiss clears retryTargetEntry`() = runTest {
        val scope = CoroutineScope(testDispatcher)
        scope.launch { viewModel.history.collect {} }
        viewModel.setAccount(1)
        testDispatcher.scheduler.advanceUntilIdle()
        viewModel.onClickEntry(viewModel.history.value[1])
        assertNotNull(viewModel.retryTargetEntry.value)
        viewModel.onRetryDismiss()
        assertNull(viewModel.retryTargetEntry.value)
    }

    @Test
    fun `onRetryConfirm downloads file`() = runTest {
        val scope = CoroutineScope(testDispatcher)
        scope.launch { viewModel.history.collect {} }
        viewModel.setAccount(1)
        testDispatcher.scheduler.advanceUntilIdle()
        viewModel.onClickEntry(viewModel.history.value[1])
        coEvery { repository.downloadFile(1, any()) } returns Result.success(true)
        viewModel.onRetryConfirm()
        testDispatcher.scheduler.advanceUntilIdle()
        coVerify { repository.downloadFile(1, match { it.name == "failed.zip" }) }
        assertNull(viewModel.retryTargetEntry.value)
    }

    @Test
    fun `onRetryConfirm with null entry is no-op`() = runTest {
        assertNull(viewModel.retryTargetEntry.value)
        viewModel.onRetryConfirm()
        testDispatcher.scheduler.advanceUntilIdle()
    }

    @Test
    fun `setAccount with different id reloads history`() = runTest {
        val newEntry = DownloadHistoryEntry(
            id = 10, fileName = "other.pdf", remotePath = "/other.pdf",
            localPath = "/local/other.pdf", fileSize = 100,
            downloadTime = 5000, accountId = 2, status = DownloadStatus.SUCCESS,
        )
        every { repository.getDownloadHistory(2) } returns flowOf(listOf(newEntry))
        val scope = CoroutineScope(testDispatcher)
        scope.launch { viewModel.history.collect {} }
        viewModel.setAccount(2)
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(1, viewModel.history.value.size)
        assertEquals("other.pdf", viewModel.history.value[0].fileName)
    }
}
