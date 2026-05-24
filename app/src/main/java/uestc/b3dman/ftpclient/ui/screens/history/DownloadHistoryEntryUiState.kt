package uestc.b3dman.ftpclient.ui.screens.history

import uestc.b3dman.ftpclient.data.model.DownloadStatus

data class DownloadHistoryEntryUiState(
    val id: Int = 0,
    val fileName: String,
    val remotePath: String,
    val localPath: String,
    val fileSize: String,
    val downloadTime: String,
    val accountId: Int,
    val status: DownloadStatus,
    val isSuccess: Boolean,
)
