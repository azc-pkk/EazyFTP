package uestc.b3dman.ftpclient.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ftp_accounts")
data class FtpAccount(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val ip: String,
    val port: Int = 21,
    val userName: String = "anonymous",
    val password: String = "",
    val alias: String = "",
    val avatarPath: String? = null, // 存储头像本地路径
    val lastLoginTime: Long
)

