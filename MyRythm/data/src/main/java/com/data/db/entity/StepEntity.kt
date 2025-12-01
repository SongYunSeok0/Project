package com.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "steps")
data class StepEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val steps: Int
)

@Entity(tableName = "daily_steps")
data class DailyStepEntity(
    @PrimaryKey val date: String,   // "2025-11-21"
    val steps: Int
)
