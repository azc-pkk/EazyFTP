package uestc.b3dman.ftpclient.data.remote

import uestc.b3dman.ftpclient.data.model.FtpFileItem
import java.io.InputStream
import java.io.OutputStream

interface FtpManager {
    suspend fun connect(ip: String, port: Int, username: String, password: String): Boolean
    suspend fun listFiles(path: String): List<FtpFileItem>
    suspend fun rename(fromPath: String, toPath: String): Boolean
    suspend fun mkdir(path: String): Boolean
    suspend fun deleteFile(path: String): Boolean
    suspend fun downloadFile(remotePath: String, outputStream: OutputStream?): Boolean
    suspend fun uploadFile(remotePath: String, inputStream: InputStream?): Boolean
    suspend fun disconnect()
}