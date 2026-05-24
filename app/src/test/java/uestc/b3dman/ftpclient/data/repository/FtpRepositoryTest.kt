package uestc.b3dman.ftpclient.data.repository

import android.net.Uri
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import uestc.b3dman.ftpclient.data.local.DownloadHistoryDao
import uestc.b3dman.ftpclient.data.local.FtpAccountDao
import uestc.b3dman.ftpclient.data.local.StorageManager
import uestc.b3dman.ftpclient.data.model.DownloadHistoryEntry
import uestc.b3dman.ftpclient.data.model.DownloadStatus
import uestc.b3dman.ftpclient.data.model.FtpAccount
import uestc.b3dman.ftpclient.data.model.FtpFileItem
import uestc.b3dman.ftpclient.data.remote.FtpManager

@OptIn(ExperimentalCoroutinesApi::class)
class FtpRepositoryTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    private val accountDao: FtpAccountDao = mockk(relaxed = true)
    private val historyDao: DownloadHistoryDao = mockk(relaxed = true)
    private val ftpManager: FtpManager = mockk(relaxed = true)
    private val storage: StorageManager = mockk(relaxed = true)
    private lateinit var repository: FtpRepository

    private val testAccount = FtpAccount(
        id = 1, ip = "192.168.1.1", port = 21,
        userName = "testuser", password = "testpass",
        alias = "Test", lastLoginTime = 0
    )

    @Before
    fun setUp() {
        repository = FtpRepository(accountDao, historyDao, ftpManager, storage)
    }

    @Test
    fun `savedAccounts returns flow from dao`() = runTest {
        val accounts = listOf(testAccount)
        every { accountDao.getAllAccounts() } returns flowOf(accounts)
        val freshRepo = FtpRepository(accountDao, historyDao, ftpManager, storage)
        val result = freshRepo.savedAccounts.first()
        assertEquals(accounts, result)
    }

    @Test
    fun `login success returns Result success`() = runTest {
        coEvery { ftpManager.connect("192.168.1.1", 21, "testuser", "testpass") } returns true
        val result = repository.login(testAccount)
        assertTrue(result.isSuccess)
        assertEquals(true, result.getOrNull())
    }

    @Test
    fun `login failure returns Result failure`() = runTest {
        coEvery { ftpManager.connect("192.168.1.1", 21, "testuser", "testpass") } returns false
        val result = repository.login(testAccount)
        assertTrue(result.isFailure)
    }

    @Test
    fun `getFiles delegates to ftpManager`() = runTest {
        val files = listOf(
            FtpFileItem("file1.txt", false, 1000, 500, "/file1.txt"),
            FtpFileItem("folder1", true, 2000, 0, "/folder1")
        )
        coEvery { ftpManager.listFiles("/home") } returns files
        val result = repository.getFiles("/home")
        assertEquals(2, result.size)
        assertEquals("file1.txt", result[0].name)
        assertEquals("folder1", result[1].name)
    }

    @Test
    fun `downloadFile success updates history`() = runTest {
        val file = FtpFileItem("test.txt", false, 0, 1000, "/test.txt")
        val localFile = tempFolder.newFile("test.txt")
        every { storage.createDownloadFile("test.txt") } returns localFile
        coEvery { historyDao.insertEntry(any()) } returns 1L
        coEvery { ftpManager.downloadFile(any(), any()) } returns true
        coEvery { historyDao.update(any()) } returns Unit
        val result = repository.downloadFile(1, file)
        assertTrue(result.isSuccess)
        coVerify { historyDao.insertEntry(match { it.fileName == "test.txt" && it.status == DownloadStatus.DOWNLOADING }) }
        coVerify { historyDao.update(match { it.status == DownloadStatus.SUCCESS }) }
    }

    @Test
    fun `downloadFile failure updates history with failed status`() = runTest {
        val file = FtpFileItem("fail.txt", false, 0, 100, "/fail.txt")
        val localFile = tempFolder.newFile("fail.txt")
        every { storage.createDownloadFile("fail.txt") } returns localFile
        coEvery { historyDao.insertEntry(any()) } returns 2L
        coEvery { ftpManager.downloadFile(any(), any()) } returns false
        val result = repository.downloadFile(1, file)
        assertTrue(result.isFailure)
        coVerify { historyDao.update(match { it.status == DownloadStatus.FAILED }) }
    }

    @Test
    fun `downloadFile returns failure when local file cannot be created`() = runTest {
        val file = FtpFileItem("nope.txt", false, 0, 100, "/nope.txt")
        every { storage.createDownloadFile("nope.txt") } returns null
        val result = repository.downloadFile(1, file)
        assertTrue(result.isFailure)
    }

    @Test
    fun `uploadFile success`() = runTest {
        val uri: Uri = mockk(relaxed = true)
        val localFile = tempFolder.newFile("upload.txt")
        every { storage.getFileName(uri) } returns "upload.txt"
        every { storage.getInputStream(uri) } returns localFile.inputStream()
        coEvery { ftpManager.uploadFile(any(), any()) } returns true
        val result = repository.uploadFile("/remote", uri)
        assertTrue(result.isSuccess)
    }

    @Test
    fun `uploadFile returns failure when getFileName returns null`() = runTest {
        val uri: Uri = mockk(relaxed = true)
        every { storage.getFileName(uri) } returns null
        val result = repository.uploadFile("/remote", uri)
        assertTrue(result.isFailure)
    }

    @Test
    fun `uploadFile returns failure when ftpManager fails`() = runTest {
        val uri: Uri = mockk(relaxed = true)
        val localFile = tempFolder.newFile("fail.txt")
        every { storage.getFileName(uri) } returns "fail.txt"
        every { storage.getInputStream(uri) } returns localFile.inputStream()
        coEvery { ftpManager.uploadFile(any(), any()) } returns false
        val result = repository.uploadFile("/remote", uri)
        assertTrue(result.isFailure)
    }

    @Test
    fun `renameFile success`() = runTest {
        coEvery { ftpManager.rename("/old.txt", "/new.txt") } returns true
        val result = repository.renameFile("/old.txt", "/new.txt")
        assertTrue(result.isSuccess)
    }

    @Test
    fun `renameFile failure`() = runTest {
        coEvery { ftpManager.rename("/old.txt", "/new.txt") } returns false
        val result = repository.renameFile("/old.txt", "/new.txt")
        assertTrue(result.isFailure)
    }

    @Test
    fun `createFolder success`() = runTest {
        coEvery { ftpManager.mkdir("/newfolder") } returns true
        val result = repository.createFolder("/newfolder")
        assertTrue(result.isSuccess)
    }

    @Test
    fun `deleteFile success`() = runTest {
        coEvery { ftpManager.deleteFile("/garbage.txt") } returns true
        val result = repository.deleteFile("/garbage.txt")
        assertTrue(result.isSuccess)
    }

    @Test
    fun `deleteFile delegates to ftpManager`() = runTest {
        coEvery { ftpManager.deleteFile(any()) } returns true
        repository.deleteFile("/test.txt")
        coVerify { ftpManager.deleteFile("/test.txt") }
    }

    @Test
    fun `saveAccountWithAvatar delegates to dao`() = runTest {
        val uri: Uri = mockk(relaxed = true)
        coEvery { storage.saveAvatar(uri) } returns "/avatars/avatar.jpg"
        coEvery { accountDao.insertAccount(any()) } returns Unit
        repository.saveAccountWithAvatar(testAccount, uri)
        coVerify { accountDao.insertAccount(match { it.avatarPath == "/avatars/avatar.jpg" }) }
    }

    @Test
    fun `saveAccountWithAvatar with null uri`() = runTest {
        coEvery { accountDao.insertAccount(any()) } returns Unit
        repository.saveAccountWithAvatar(testAccount, null)
        coVerify { accountDao.insertAccount(testAccount.copy(avatarPath = null)) }
    }

    @Test
    fun `updateLastLoginTime updates timestamp`() = runTest {
        coEvery { accountDao.updateAccount(any()) } returns Unit
        val before = System.currentTimeMillis()
        repository.updateLastLoginTime(testAccount)
        val slot = slot<FtpAccount>()
        coVerify { accountDao.updateAccount(capture(slot)) }
        assertTrue(slot.captured.lastLoginTime >= before)
    }

    @Test
    fun `deleteAccount deletes avatar and account`() = runTest {
        val accountWithAvatar = testAccount.copy(avatarPath = "/avatars/old.jpg")
        repository.deleteAccount(accountWithAvatar)
        verify { storage.deleteFile("/avatars/old.jpg") }
        coVerify { accountDao.deleteAccount(accountWithAvatar) }
    }

    @Test
    fun `deleteAccount with null avatar skips deleteFile`() = runTest {
        repository.deleteAccount(testAccount)
        coVerify { accountDao.deleteAccount(testAccount) }
    }

    @Test
    fun `getAccountById delegates to dao`() = runTest {
        coEvery { accountDao.getAccountById(1) } returns testAccount
        val result = repository.getAccountById(1)
        assertEquals(testAccount, result)
    }

    @Test
    fun `getAccountById returns null when not found`() = runTest {
        coEvery { accountDao.getAccountById(999) } returns null
        val result = repository.getAccountById(999)
        assertNull(result)
    }

    @Test
    fun `updateAccountWithAvatar with new avatar`() = runTest {
        val uri: Uri = mockk(relaxed = true)
        val accountWithOldAvatar = testAccount.copy(avatarPath = "/avatars/old.jpg")
        coEvery { storage.saveAvatar(uri) } returns "/avatars/new.jpg"
        repository.updateAccountWithAvatar(accountWithOldAvatar, uri)
        verify { storage.deleteFile("/avatars/old.jpg") }
        coVerify { accountDao.updateAccount(match { it.avatarPath == "/avatars/new.jpg" }) }
    }

    @Test
    fun `logout disconnects ftpManager`() = runTest {
        coEvery { ftpManager.disconnect() } returns Unit
        repository.logout()
        coVerify { ftpManager.disconnect() }
    }

    @Test
    fun `getDownloadHistory delegates to historyDao`() = runTest {
        val history = listOf(
            DownloadHistoryEntry(1, "a.txt", "/a.txt", "/local/a.txt", 100, 0, 1, DownloadStatus.SUCCESS)
        )
        every { historyDao.getHistoryForAccount(1) } returns flowOf(history)
        val result = repository.getDownloadHistory(1).first()
        assertEquals(history, result)
    }

    @Test
    fun `openDownloadedFile delegates to storage`() {
        every { storage.openFile("/local/file.pdf") } returns true
        val result = repository.openDownloadedFile("/local/file.pdf")
        assertTrue(result)
        verify { storage.openFile("/local/file.pdf") }
    }

    @Test
    fun `openDownloadedFile returns false on failure`() {
        every { storage.openFile("/missing") } returns false
        val result = repository.openDownloadedFile("/missing")
        assertFalse(result)
    }
}
