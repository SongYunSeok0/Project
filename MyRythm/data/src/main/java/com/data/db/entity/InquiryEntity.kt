package com.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "inquiry")
data class InquiryEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val type: String,
    val title: String,
    val content: String,
    val answer: String? = null
)

