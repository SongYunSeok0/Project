package com.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "heart_rate")
data class HeartRateEntity(
    @PrimaryKey
    val id: Long,
    val bpm: Int,
    val collectedAt: String
)
