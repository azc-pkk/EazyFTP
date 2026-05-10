package uestc.b3dman.ftpclient.data.repository

import android.net.Uri
import uestc.b3dman.ftpclient.data.model.FtpAccount
import uestc.b3dman.ftpclient.data.model.FtpFileItem
import kotlinx.coroutines.flow.Flow
import uestc.b3dman.ftpclient.data.local.FtpAccountDao
import uestc.b3dman.ftpclient.data.local.StorageManager
import uestc.b3dman.ftpclient.data.remote.FtpManager
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FtpRepository @Inject constructor(
    private val accountDao: FtpAccountDao,
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
        return if (ftpManager.connect(account)) Result.success(true) else Result.failure(Exception("Login failed"))
    }

    suspend fun getFiles(path: String): List<FtpFileItem> {
        return ftpManager.listFiles(path)
    }

    suspend fun downloadFile(remotePath: String, fileName: String): Result<Boolean> {
        return if (ftpManager.downloadFile(remotePath, storage.getDownloadOutputStream(fileName))) Result.success(true) else Result.failure(Exception("Download failed"))
    }

    suspend fun logout() {
        ftpManager.disconnect()
    }
}