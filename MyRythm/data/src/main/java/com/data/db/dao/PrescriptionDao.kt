package com.data.db.dao

import androidx.room.*
import com.data.db.entity.PrescriptionEntity
import com.data.db.entity.PrescriptionWithPlans
import kotlinx.coroutines.flow.Flow

@Dao
interface PrescriptionDao {

    @Transaction
    @Query("SELECT * FROM prescriptions WHERE userId = :userId ORDER BY prescriptionId DESC")
    fun observePrescriptions(userId: Long): Flow<List<PrescriptionWithPlans>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPrescription(prescription: PrescriptionEntity): Long

    @Delete
    suspend fun deletePrescription(prescription: PrescriptionEntity)
}
