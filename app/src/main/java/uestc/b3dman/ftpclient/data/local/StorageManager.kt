package uestc.b3dman.ftpclient.data.local

import android.net.Uri
import java.io.OutputStream

interface StorageManager {
    suspend fun saveAvatar(uri: Uri): String?

    fun getDownloadOutputStream(fileName: String): OutputStream?
}