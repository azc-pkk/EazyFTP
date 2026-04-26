package uestc.b3dman.ftpclient.data.repository

import uestc.b3dman.ftpclient.data.model.FtpAccount
import uestc.b3dman.ftpclient.data.model.FtpFileItem
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import uestc.b3dman.ftpclient.data.local.FtpAccountDao
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FtpRepository @Inject constructor(
    private val accountDao: FtpAccountDao
) {
    // --- 本地数据库操作 ---
    val savedAccounts: Flow<List<FtpAccount>> = accountDao.getAllAccounts()

    suspend fun saveAccount(account: FtpAccount) {
        accountDao.insertAccount(account)
    }

    suspend fun updateLastLoginTime(account: FtpAccount) {
        val updatedAccount = account.copy(lastLoginTime = System.currentTimeMillis())
        accountDao.updateAccount(updatedAccount)
    }

    // --- 模拟远程 FTP 操作 ---

    // 模拟登录
    suspend fun login(account: FtpAccount): Result<Boolean> {
        delay(1500) // 模拟网络延迟
        return Result.success(true)
    }

    // 模拟获取文件列表
    suspend fun getFiles(path: String): List<FtpFileItem> {
        delay(1000) // 模拟网络请求
        return if (path == "/" || path == "Root") {
            listOf(
                FtpFileItem("Documents", true, "2026-03-20 10:00", fullPath = "/Documents"),
                FtpFileItem("Music", true, "2026-03-21 15:30", fullPath = "/Music"),
                FtpFileItem("config.ini", false, "2026-03-22 09:00", "1.2 KB", "/config.ini")
            )
        } else {
            // 模拟子目录内容
            listOf(
                FtpFileItem("Work_Report.pdf", false, "2026-03-22 11:00", "4.5 MB", "$path/Work_Report.pdf"),
                FtpFileItem("image_01.png", false, "2026-03-22 12:00", "2.1 MB", "$path/image_01.png")
            )
        }
    }

    // 模拟删除文件
    suspend fun deleteFile(file: FtpFileItem): Boolean {
        delay(800)
        return true
    }
}