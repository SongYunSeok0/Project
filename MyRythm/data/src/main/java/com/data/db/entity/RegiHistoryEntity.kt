// data/src/main/java/com/data/db/entity/RegiHistoryEntity.kt
package com.data.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "regihistory")
data class RegiHistoryEntity(
    @PrimaryKey(autoGenerate = false)
    val id: Long,

    // 🔹 SQL에서 userId 라고 쓰고 있으므로 컬럼명도 userId 로 고정
    @ColumnInfo(name = "userId")
    val userId: Long,

    @ColumnInfo(name = "regi_type")
    val regiType: String,

    @ColumnInfo(name = "label")
    val label: String,

    @ColumnInfo(name = "issued_date")
    val issuedDate: String?,

    @ColumnInfo(name = "useAlarm")
    val useAlarm: Boolean,

    @ColumnInfo(name = "device")
    val device: Long? = null
)
