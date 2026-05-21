package uestc.b3dman.ftpclient.data.remote

import uestc.b3dman.ftp.FtpClient
import uestc.b3dman.ftpclient.data.model.FtpFileItem
import java.io.InputStream
import java.io.OutputStream
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.comparisons.compareByDescending
import kotlin.comparisons.thenBy

@Singleton
class CustomFtpManager @Inject constructor(): FtpManager {
    val ftpClient = FtpClient()
    override suspend fun connect(
        ip: String,
        port: Int,
        username: String,
        password: String
    ): Boolean {
        ftpClient.connect(ip, port)
        return ftpClient.login(username, password)
    }

    override suspend fun listFiles(path: String): List<FtpFileItem> {
        val files = ftpClient.listFiles(path)
        return files.map { file ->
            FtpFileItem(
                name = file.name,
                isFolder = file.isFolder,
                lastUpdateTime = file.lastUpdateTime,
                size = file.size,
                fullPath = file.fullPath
            )
        }.sortedWith (compareByDescending<FtpFileItem> { it.isFolder }.thenBy { it.name })
    }

    override suspend fun downloadFile(
        remotePath: String,
        outputStream: OutputStream?
    ): Boolean {
        if (outputStream == null) return false
        return ftpClient.downloadFile(remotePath, outputStream)
    }

    override suspend fun uploadFile(
        remotePath: String,
        inputStream: InputStream?
    ): Boolean {
        if (inputStream == null) return false
        return ftpClient.uploadFile(remotePath, inputStream)
    }

    override suspend fun disconnect() {
        ftpClient.disconnect()
    }
}