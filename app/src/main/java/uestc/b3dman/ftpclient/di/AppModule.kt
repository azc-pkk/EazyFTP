package uestc.b3dman.ftpclient.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import uestc.b3dman.ftpclient.data.local.AppDatabase
import uestc.b3dman.ftpclient.data.local.FtpAccountDao
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
    fun provideFtpAccountDao(db: AppDatabase): FtpAccountDao {
        return db.ftpAccountDao()
    }

    @Provides
    @Singleton
    fun provideFtpRepository(dao: FtpAccountDao): FtpRepository {
        // 现在 Repository 接收真实的 DAO 了
        return FtpRepository(dao)
    }
}