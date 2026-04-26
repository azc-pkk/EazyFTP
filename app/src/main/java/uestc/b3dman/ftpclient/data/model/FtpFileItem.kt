package uestc.b3dman.ftpclient.data.model

data class FtpFileItem(
    val name: String,
    val isFolder: Boolean,
    val lastUpdateTime: String,
    val size: String? = null,
    val fullPath: String = ""
)
