package uestc.b3dman.ftpclient.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "download_history")
data class DownloadHistoryEntry(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val fileName: String,
    val remotePath: String,
    val localPath: String,
    val fileSize: Long,
    val downloadTime: Long,
    val accountId: Int,
)
