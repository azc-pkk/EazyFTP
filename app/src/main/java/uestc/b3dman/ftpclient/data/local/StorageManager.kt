package uestc.b3dman.ftpclient.data.local

import android.net.Uri
import java.io.InputStream
import java.io.OutputStream

interface StorageManager {
    val downloadDir: String
    suspend fun saveAvatar(uri: Uri): String?
    fun getDownloadOutputStream(fileName: String): OutputStream?
    fun getInputStream(uri: Uri): InputStream?
    fun getFileName(uri: Uri): String?
}