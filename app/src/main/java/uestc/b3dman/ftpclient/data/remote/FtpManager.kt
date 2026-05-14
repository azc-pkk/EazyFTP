package uestc.b3dman.ftpclient.data.remote

import uestc.b3dman.ftpclient.data.model.FtpFileItem
import java.io.OutputStream

interface FtpManager {
    fun connect(ip: String, port: Int, username: String, password: String): Boolean
    fun listFiles(path: String): List<FtpFileItem>
    fun downloadFile(remotePath: String, outputStream: OutputStream?): Boolean
    fun disconnect()
}