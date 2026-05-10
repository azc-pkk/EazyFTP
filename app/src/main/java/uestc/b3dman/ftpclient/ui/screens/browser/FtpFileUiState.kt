package uestc.b3dman.ftpclient.ui.screens.browser

data class FtpFileUiState(
    val name: String,
    val isFolder: Boolean,
    val lastUpdateTime: String,
    val size: String,
    val fullPath: String,
)