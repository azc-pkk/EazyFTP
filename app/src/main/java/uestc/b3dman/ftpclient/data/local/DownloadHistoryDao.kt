package uestc.b3dman.ftpclient.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import uestc.b3dman.ftpclient.data.model.DownloadHistoryEntry

@Dao
interface DownloadHistoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: DownloadHistoryEntry)

    @Query("SELECT * FROM download_history WHERE accountId = :accountId ORDER BY downloadTime DESC")
    fun getHistoryForAccount(accountId: Int): Flow<List<DownloadHistoryEntry>>

    @Delete
    suspend fun delete(entry: DownloadHistoryEntry)

    @Update
    suspend fun update(entry: DownloadHistoryEntry)
}