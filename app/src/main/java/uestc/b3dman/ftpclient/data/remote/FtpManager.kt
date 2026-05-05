package uestc.b3dman.ftpclient.data.remote

import org.apache.commons.net.ftp.FTPClient
import org.apache.commons.net.ftp.FTPFile
import uestc.b3dman.ftpclient.data.model.FtpAccount
import uestc.b3dman.ftpclient.data.model.FtpFileItem
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.log10
import kotlin.math.pow

@Singleton
class FtpManager @Inject constructor() {
    private val ftpClient = FTPClient()

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
            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())

            files.map { file ->
                FtpFileItem(
                    name = file.name,
                    isFolder = file.isDirectory,
                    lastUpdateTime = sdf.format(file.timestamp.time),
                    size = if (file.isFile) formatSize(file.size) else null,
                    fullPath = if (path.endsWith("/")) path + file.name else "$path/${file.name}"
                )
            }.sortedWith(compareByDescending<FtpFileItem> { it.isFolder }.thenBy { it.name })
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    private fun formatSize(size: Long): String {
        if (size <= 0) return "0 B"
        val units = arrayOf("B", "KB", "MB", "GB", "TB")
        val digitGroups = (log10(size.toDouble()) / log10(1024.0)).toInt()
        return "%.1f %s".format(size / 1024.0.pow(digitGroups.toDouble()), units[digitGroups])
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