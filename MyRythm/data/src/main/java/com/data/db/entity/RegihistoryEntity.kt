package com.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "regihistory")
data class RegihistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val regihistoryId: Long = 0,
    val userId: Long,                 // Django: user (ForeignKey)
    val regiType: String,     // Django: regi_type
    val label: String?,           // Django: label
    val issuedDate: String?           // Django: issued_date
)
