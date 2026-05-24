package uestc.b3dman.ftpclient.data.local

import android.net.Uri
import java.io.File
import java.io.InputStream

interface StorageManager {
    val downloadDir: String
    suspend fun saveAvatar(uri: Uri): String?
    fun createDownloadFile(fileName: String): File?
    fun openFile(localPath: String): Boolean
    fun getInputStream(uri: Uri): InputStream?
    fun getFileName(uri: Uri): String?
}