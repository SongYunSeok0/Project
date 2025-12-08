package com.data.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "inquiry")
data class InquiryEntity(
    @PrimaryKey val id: Long = 0,
    @ColumnInfo(name = "user_id") val userId: Long = 0,
    @ColumnInfo(name = "username") val username: String? = null,
    @ColumnInfo(name = "type") val type: String,  // category
    @ColumnInfo(name = "title") val title: String,
    @ColumnInfo(name = "content") val content: String,
    @ColumnInfo(name = "answer") val answer: String? = null,  // deprecated
    @ColumnInfo(name = "is_answered") val isAnswered: Boolean = false,
    @ColumnInfo(name = "created_at") val createdAt: String? = null,
    @ColumnInfo(name = "updated_at") val updatedAt: String? = null,
    @ColumnInfo(name = "comment_count") val commentCount: Int = 0
)