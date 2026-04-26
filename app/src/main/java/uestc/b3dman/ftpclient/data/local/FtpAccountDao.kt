package uestc.b3dman.ftpclient.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import uestc.b3dman.ftpclient.data.model.FtpAccount

@Dao
interface FtpAccountDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAccount(account: FtpAccount)

    @Query("SELECT * FROM ftp_accounts ORDER BY lastLoginTime DESC")
    fun getAllAccounts(): Flow<List<FtpAccount>> // 使用 Flow 实现自动 UI 更新

    @Update
    suspend fun updateAccount(account: FtpAccount)

    @Delete
    suspend fun deleteAccount(account: FtpAccount)
}