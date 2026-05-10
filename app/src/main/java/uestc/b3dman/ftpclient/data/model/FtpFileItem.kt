package uestc.b3dman.ftpclient.data.model

data class FtpFileItem(
    val name: String,
    val isFolder: Boolean,
    val lastUpdateTime: Long,
    val size: Long,
    val fullPath: String = ""
)
