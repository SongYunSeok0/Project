package com.data.db.dao

import androidx.room.*
import com.data.db.entity.RegihistoryEntity
import com.data.db.entity.RegihistoryWithPlans
import kotlinx.coroutines.flow.Flow

@Dao
interface RegihistoryDao {

    @Transaction
    @Query("SELECT * FROM regihistory WHERE userId = :userId ORDER BY regihistoryId DESC")
    fun observePrescriptions(userId: Long): Flow<List<RegihistoryWithPlans>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPrescription(prescription: RegihistoryEntity): Long

    @Delete
    suspend fun deletePrescription(prescription: RegihistoryEntity)
}
