package uestc.b3dman.ftp

data class FtpFile(
    val name: String,
    val isFolder: Boolean,
    val lastUpdateTime: Long,
    val size: Long,
    val fullPath: String
)
