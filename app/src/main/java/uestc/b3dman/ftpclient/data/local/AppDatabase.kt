package uestc.b3dman.ftpclient.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import uestc.b3dman.ftpclient.data.model.FtpAccount

@Database(
    entities = [FtpAccount::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun ftpAccountDao(): FtpAccountDao
}