package uestc.b3dman.ftpclient.ui.screens.browser

import uestc.b3dman.ftpclient.data.model.FtpFileItem
import uestc.b3dman.ftpclient.utils.Formatter

fun FtpFileItem.toUiState(): FtpFileUiState {
    return FtpFileUiState(
        name = name,
        isFolder = isFolder,
        lastUpdateTime = Formatter.formatDate(lastUpdateTime),
        size = Formatter.formatSize(size),
        fullPath = fullPath
    )
}