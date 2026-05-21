package uestc.b3dman.ftpclient.data.remote

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.apache.commons.net.ftp.FTPClient
import org.apache.commons.net.ftp.FTPFile
import uestc.b3dman.ftpclient.data.model.FtpFileItem
import java.io.InputStream
import java.io.OutputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ApacheFtpManager @Inject constructor() : FtpManager{
    private val ftpClient = FTPClient()

    override suspend fun connect(ip: String, port: Int, username: String, password: String): Boolean =
        withContext(Dispatchers.IO) {
            try {
                if (ftpClient.isConnected) {
                    ftpClient.disconnect()
                }

                ftpClient.connect(ip, port)
                val result = ftpClient.login(username, password)
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

    override suspend fun listFiles(path: String): List<FtpFileItem> =
        withContext(Dispatchers.IO) {
            try {
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

    override suspend fun downloadFile(remotePath: String, outputStream: OutputStream?): Boolean =
        withContext(Dispatchers.IO) {
            if (outputStream == null) return@withContext false
            try {
                ftpClient.retrieveFile(remotePath, outputStream)
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }

    override suspend fun uploadFile(remotePath: String, inputStream: InputStream?): Boolean =
        withContext(Dispatchers.IO) {
            if (inputStream == null) return@withContext false
            try {
                val success = ftpClient.storeFile(remotePath, inputStream)
                inputStream.close()
                success
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }

    override suspend fun disconnect() {
        if (ftpClient.isConnected) ftpClient.disconnect()
    }
}