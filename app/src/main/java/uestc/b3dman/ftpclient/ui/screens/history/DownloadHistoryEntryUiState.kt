package uestc.b3dman.ftpclient.ui.screens.history

data class DownloadHistoryEntryUiState(
    val id: Int = 0,
    val fileName: String,
    val remotePath: String,
    val localPath: String,
    val fileSize: String,
    val downloadTime: String,
    val accountId: Int, // FIXME: 可能不需要
)