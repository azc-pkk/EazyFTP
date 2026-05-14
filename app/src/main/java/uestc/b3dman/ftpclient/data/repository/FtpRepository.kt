package uestc.b3dman.ftpclient.data.repository

import android.net.Uri
import uestc.b3dman.ftpclient.data.model.FtpAccount
import uestc.b3dman.ftpclient.data.model.FtpFileItem
import kotlinx.coroutines.flow.Flow
import uestc.b3dman.ftpclient.data.local.DownloadHistoryDao
import uestc.b3dman.ftpclient.data.local.FtpAccountDao
import uestc.b3dman.ftpclient.data.local.StorageManager
import uestc.b3dman.ftpclient.data.model.DownloadHistoryEntry
import uestc.b3dman.ftpclient.data.remote.FtpManager
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FtpRepository @Inject constructor(
    private val accountDao: FtpAccountDao,
    private val historyDao: DownloadHistoryDao,
    private val ftpManager: FtpManager,
    private val storage: StorageManager,
) {
    // --- 本地数据库操作 ---
    val savedAccounts: Flow<List<FtpAccount>> = accountDao.getAllAccounts()

    suspend fun saveAccountWithAvatar(account: FtpAccount, uri: Uri?) {
        val avatarPath = uri?.let { storage.saveAvatar(it) }
        accountDao.insertAccount(account.copy(avatarPath = avatarPath))
    }

    suspend fun updateLastLoginTime(account: FtpAccount) {
        val updatedAccount = account.copy(lastLoginTime = System.currentTimeMillis())
        accountDao.updateAccount(updatedAccount)
    }

    suspend fun deleteAccount(account: FtpAccount) {
        accountDao.deleteAccount(account)
    }

    suspend fun updateAccountWithAvatar(account: FtpAccount, uri: Uri?) {
        val avatarPath = uri?.let { storage.saveAvatar(it) }
        accountDao.updateAccount(account.copy(avatarPath = avatarPath))
    }

    suspend fun getAccountById(id: Int): FtpAccount? {
        return accountDao.getAccountById(id)
    }

    // --- 远程 FTP 操作 ---
    suspend fun login(account: FtpAccount): Result<Boolean> {
        val result = ftpManager.connect(account.ip, account.port, account.userName, account.password)
        return if (result) Result.success(true) else Result.failure(Exception("Login failed"))
    }

    suspend fun getFiles(path: String): List<FtpFileItem> {
        return ftpManager.listFiles(path)
    }

    suspend fun downloadFile(accountId:Int, file: FtpFileItem): Result<Boolean> {
        val result = ftpManager.downloadFile(file.fullPath, storage.getDownloadOutputStream(file.name))
        // TODO: 根据结果修改下载历史记录（成功/失败）
        historyDao.insert(DownloadHistoryEntry(
            fileName = file.name,
            remotePath = file.fullPath,
            localPath = storage.downloadDir + File.separator + file.name,
            fileSize = file.size,
            downloadTime = System.currentTimeMillis(),
            accountId = accountId
        ))
        return if(result) Result.success(true) else Result.failure(Exception("Download failed"))
    }

    suspend fun logout() {
        ftpManager.disconnect()
    }

    //
    fun getDownloadHistory(accountId: Int): Flow<List<DownloadHistoryEntry>> {
        return historyDao.getHistoryForAccount(accountId)
    }
}