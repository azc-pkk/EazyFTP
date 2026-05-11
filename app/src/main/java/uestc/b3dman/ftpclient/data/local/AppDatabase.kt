package uestc.b3dman.ftpclient.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import uestc.b3dman.ftpclient.data.model.FtpAccount
import uestc.b3dman.ftpclient.data.model.DownloadHistoryEntry

@Database(
    entities = [FtpAccount::class, DownloadHistoryEntry::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun ftpAccountDao(): FtpAccountDao

    abstract fun downloadHistoryDao(): DownloadHistoryDao
}