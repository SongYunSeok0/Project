package com.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "regiHistory")
data class RegiHistoryEntity(
    @PrimaryKey(autoGenerate = false)
    val id: Long,
    val userId: Long,
    val regiType: String,
    val label: String?,
    val issuedDate: String?
)
