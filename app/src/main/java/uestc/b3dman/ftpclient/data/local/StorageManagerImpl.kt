package uestc.b3dman.ftpclient.data.local

import android.content.Context
import android.net.Uri
import android.os.Environment
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.util.UUID
import javax.inject.Inject

class StorageManagerImpl @Inject constructor(
    @param:ApplicationContext private val context: Context
) : StorageManager {
    override var downloadDir: String = ""

    init {
        // 初始化下载目录
        val dir = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "FTPClient")
        if (!dir.exists()) {
            dir.mkdirs()
        }
        this.downloadDir = dir.absolutePath
    }

    // FIXME: 头像相同会多次保存。
    override suspend fun saveAvatar(uri: Uri): String? {
        return try {
            val contentResolver = context.contentResolver
            val avatarDir = File(context.filesDir, "avatars")
            if (!avatarDir.exists()) {
                avatarDir.mkdirs()
            }
            val fileName = "avatar_${UUID.randomUUID()}.jpg"
            val avatarFile = File(avatarDir, fileName)

            contentResolver.openInputStream(uri)?.use { inputStream ->
                FileOutputStream(avatarFile).use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }

            avatarFile.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    override fun getDownloadOutputStream(fileName: String): OutputStream? {
       return try {
            val file = File(downloadDir, fileName)
            file.outputStream()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}