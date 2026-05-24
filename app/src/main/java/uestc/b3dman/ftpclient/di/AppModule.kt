package uestc.b3dman.ftpclient.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import uestc.b3dman.ftpclient.data.local.AppDatabase
import uestc.b3dman.ftpclient.data.local.DownloadHistoryDao
import uestc.b3dman.ftpclient.data.local.FtpAccountDao
import uestc.b3dman.ftpclient.data.local.StorageManager
import uestc.b3dman.ftpclient.data.local.StorageManagerImpl
import uestc.b3dman.ftpclient.data.remote.CustomFtpManager
import uestc.b3dman.ftpclient.data.remote.FtpManager
import uestc.b3dman.ftpclient.data.repository.FtpRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "ftp_database"
        ).build()
    }

    @Provides
    @Singleton
    fun provideFtpAccountDao(db: AppDatabase): FtpAccountDao {
        return db.ftpAccountDao()
    }

    @Provides
    @Singleton
    fun provideDownloadHistoryDao(db: AppDatabase): DownloadHistoryDao {
        return db.downloadHistoryDao()
    }

    @Provides
    @Singleton
    fun provideFtpManager(): FtpManager = CustomFtpManager()

    @Provides
    @Singleton
    fun provideStorageManager(@ApplicationContext context: Context): StorageManager = StorageManagerImpl(context)

    @Provides
    @Singleton
    fun provideFtpRepository(
        accountDao: FtpAccountDao,
        historyDao: DownloadHistoryDao,
        ftpManager: FtpManager,
        storage: StorageManager
    ): FtpRepository {
        return FtpRepository(accountDao, historyDao, ftpManager, storage)
    }
}