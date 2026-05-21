package uestc.b3dman.ftpclient.data.remote

import uestc.b3dman.ftpclient.data.model.FtpFileItem
import java.io.InputStream
import java.io.OutputStream

interface FtpManager {
    suspend fun connect(ip: String, port: Int, username: String, password: String): Boolean
    suspend fun listFiles(path: String): List<FtpFileItem>
    // TODO: 手动处理流拷贝，显示下载进度
    suspend fun downloadFile(remotePath: String, outputStream: OutputStream?): Boolean
    suspend fun uploadFile(remotePath: String, inputStream: InputStream?): Boolean
    suspend fun disconnect()
}