package uestc.b3dman.ftpclient.data.local

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.provider.OpenableColumns
import androidx.core.content.FileProvider
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.UUID
import javax.inject.Inject

class StorageManagerImpl @Inject constructor(
    @param:ApplicationContext private val context: Context
) : StorageManager {
    override val downloadDir: String = run {
        val dir = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "FTPClient")
        if (!dir.exists()) {
            dir.mkdirs()
        }
        dir.absolutePath
    }

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

    override fun createDownloadFile(fileName: String): File? {
        return try {
            var file = File(downloadDir, fileName)
            if (!file.exists()) return file

            val dotIndex = fileName.lastIndexOf('.')
            val baseName = if (dotIndex == -1) fileName else fileName.substring(0, dotIndex)
            val extension = if (dotIndex == -1) "" else fileName.substring(dotIndex)
            var counter = 1
            while (file.exists()) {
                file = File(downloadDir, "$baseName ($counter)$extension")
                counter++
            }
            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    override fun deleteFile(path: String) {
        try {
            val file = File(path)
            if (file.exists()) file.delete()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun openFile(localPath: String): Boolean {
        return try {
            val file = File(localPath)
            if (!file.exists()) return false
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, context.contentResolver.getType(uri) ?: "*/*")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
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