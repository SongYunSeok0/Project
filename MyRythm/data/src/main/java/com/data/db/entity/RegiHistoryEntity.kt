package com.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "regiHistory")
data class RegiHistoryEntity(
    @PrimaryKey(autoGenerate = false)
    val id: Long,             // 서버 PK
    val userId: Long,         // 서버에서 내려줌
    val regiType: String,
    val label: String?,
    val issuedDate: String?
)
