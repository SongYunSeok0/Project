// data/src/main/java/com/data/db/dao/RegiHistoryDao.kt
package com.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.data.db.entity.RegiHistoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RegiHistoryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: RegiHistoryEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(list: List<RegiHistoryEntity>)

    @Query("SELECT * FROM `regihistory` ORDER BY id DESC")
    fun getAll(): Flow<List<RegiHistoryEntity>>

    @Query("DELETE FROM regihistory WHERE id = :id")
    suspend fun deleteById(id: Long)

    // 🔹 여기서도 userId 컬럼을 사용
    @Query("DELETE FROM `regihistory` WHERE userId = :userId")
    suspend fun deleteAllByUser(userId: Long)
}
