package uestc.b3dman.ftpclient.data.local

import android.content.Context
import android.net.Uri
import android.os.Environment
import android.provider.OpenableColumns
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
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
            val extension = contentResolver.getType(uri)?.let { mimeType ->
                when {
                    mimeType.contains("png", ignoreCase = true) -> ".png"
                    mimeType.contains("jpeg", ignoreCase = true) || mimeType.contains("jpg", ignoreCase = true) -> ".jpg"
                    mimeType.contains("webp", ignoreCase = true) -> ".webp"
                    mimeType.contains("gif", ignoreCase = true) -> ".gif"
                    mimeType.contains("bmp", ignoreCase = true) -> ".bmp"
                    else -> ".jpg"
                }
            } ?: ".jpg"
            val fileName = "avatar_${UUID.randomUUID()}$extension"
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

    override fun getInputStream(uri: Uri): InputStream? {
        return try {
            context.contentResolver.openInputStream(uri)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    override fun getFileName(uri: Uri): String? {
        if (uri.scheme == "file") {
            return uri.lastPathSegment
        }
        var fileName: String? = null
        val contentResolver = context.contentResolver
        val cursor = contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val index = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (index >= 0) {
                    fileName = it.getString(index)
                }
            }
        }
        return fileName
    }
}