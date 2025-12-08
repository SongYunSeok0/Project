// data/db/dao/InquiryDao.kt
package com.data.db.dao

import androidx.room.*
import com.data.db.entity.InquiryEntity
import com.data.db.entity.InquiryCommentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface InquiryDao {
    // 내 문의사항 목록 (로컬)
    @Query("SELECT * FROM inquiry ORDER BY id DESC")
    fun getAllInquiries(): Flow<List<InquiryEntity>>

    // 문의사항 추가
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInquiry(inquiry: InquiryEntity)

    // 여러 문의사항 추가 (서버에서 가져온 데이터 캐싱용)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInquiries(inquiries: List<InquiryEntity>)

    // 특정 문의사항 조회
    @Query("SELECT * FROM inquiry WHERE id = :id")
    suspend fun getInquiryById(id: Long): InquiryEntity?

    // 댓글 추가
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertComment(comment: InquiryCommentEntity)

    // 댓글 목록 조회
    @Query("SELECT * FROM inquiry_comment WHERE inquiry_id = :inquiryId ORDER BY created_at ASC")
    suspend fun getComments(inquiryId: Long): List<InquiryCommentEntity>

    // 댓글 여러 개 추가
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertComments(comments: List<InquiryCommentEntity>)

    // 모든 데이터 삭제 (로그아웃 시)
    @Query("DELETE FROM inquiry")
    suspend fun clearInquiries()

    @Query("DELETE FROM inquiry_comment")
    suspend fun clearComments()
}