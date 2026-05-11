package uestc.b3dman.ftpclient.data.remote

import org.apache.commons.net.ftp.FTPClient
import org.apache.commons.net.ftp.FTPFile
import uestc.b3dman.ftpclient.data.model.FtpAccount
import uestc.b3dman.ftpclient.data.model.FtpFileItem
import java.io.OutputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FtpManager @Inject constructor() {
    private val ftpClient = FTPClient()
// TODO: 重构，去掉 FtpAccount 和 FTPClient 的依赖
    fun connect(account: FtpAccount): Boolean {
        return try {
            if (ftpClient.isConnected) {
                ftpClient.disconnect()
            }

            ftpClient.connect(account.ip, account.port)
            val result = ftpClient.login(account.userName, account.password)
            if (result) {
                // Apache Commons Net 默认使用主动模式
                ftpClient.enterLocalPassiveMode()
                ftpClient.controlEncoding = "UTF-8"
                ftpClient.setFileType(FTPClient.BINARY_FILE_TYPE)
            }
            result
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun listFiles(path: String): List<FtpFileItem> {
        return try {
            val files: Array<FTPFile> = ftpClient.listFiles(path)

            files.map { file ->
                FtpFileItem(
                    name = file.name,
                    isFolder = file.isDirectory,
                    lastUpdateTime = file.timestamp.time.time,
                    size = file.size,
                    fullPath = if (path.endsWith("/")) path + file.name else "$path/${file.name}"
                )
            }.sortedWith(compareByDescending<FtpFileItem> { it.isFolder }.thenBy { it.name })
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    fun downloadFile(remotePath: String, outputStream: OutputStream?): Boolean {
        if (outputStream == null) return false
        return try {
            ftpClient.retrieveFile(remotePath, outputStream)
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun disconnect() {
        if (ftpClient.isConnected) ftpClient.disconnect()
    }
}