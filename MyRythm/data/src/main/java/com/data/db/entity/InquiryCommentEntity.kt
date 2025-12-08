package com.data.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "inquiry_comment",
    foreignKeys = [
        ForeignKey(
            entity = InquiryEntity::class,
            parentColumns = ["id"],
            childColumns = ["inquiry_id"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class InquiryCommentEntity(
    @PrimaryKey val id: Long = 0,
    @ColumnInfo(name = "inquiry_id") val inquiryId: Long,
    @ColumnInfo(name = "user_id") val userId: Long,
    @ColumnInfo(name = "username") val username: String?,
    @ColumnInfo(name = "content") val content: String,
    @ColumnInfo(name = "created_at") val createdAt: String?,
    @ColumnInfo(name = "is_staff") val isStaff: Boolean = false
)