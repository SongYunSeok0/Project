package com.data.db.dao

import androidx.room.*
import com.data.db.entity.UserEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users_user LIMIT 1")
    fun observe(): Flow<UserEntity?>          // 화면에서 지속 관찰용

    @Query("SELECT * FROM users_user LIMIT 1")
    suspend fun getOne(): UserEntity?         // 즉시 조회용

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(user: UserEntity)

    @Query("DELETE FROM users_user")
    suspend fun clear()
}

