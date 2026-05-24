package uestc.b3dman.ftpclient.ui.screens.history

import uestc.b3dman.ftpclient.utils.Formatter
import uestc.b3dman.ftpclient.data.model.DownloadHistoryEntry

fun DownloadHistoryEntry.toUiState(): DownloadHistoryEntryUiState {
    return DownloadHistoryEntryUiState(
        id = id,
        fileName = fileName,
        remotePath = remotePath,
        localPath = localPath,
        fileSize = Formatter.formatSize(fileSize),
        downloadTime = Formatter.formatDate(downloadTime),
        accountId = accountId,
        status = status,
        isSuccess = status == uestc.b3dman.ftpclient.data.model.DownloadStatus.SUCCESS,
    )
}
