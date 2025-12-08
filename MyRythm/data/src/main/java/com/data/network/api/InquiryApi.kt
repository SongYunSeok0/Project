package com.data.network.api

import com.data.network.dto.inquiry.InquiryDto
import com.data.network.dto.inquiry.InquiryCommentDto
import retrofit2.Response
import retrofit2.http.*

interface InquiryApi {
    // 내 문의사항 목록
    @GET("faqs/")
    suspend fun getMyInquiries(): List<InquiryDto>

    // 문의사항 상세
    @GET("faqs/{id}/")
    suspend fun getInquiryById(@Path("id") id: Long): InquiryDto

    // 문의사항 생성
    @POST("faqs/")
    suspend fun createInquiry(@Body request: Map<String, String>): Response<InquiryDto>

    // 🔥 스태프 전용: 모든 문의사항 조회
    @GET("faqs/all/")
    suspend fun getAllInquiries(): List<InquiryDto>

    // 특정 문의사항의 댓글 목록
    @GET("faqs/{id}/comments/")
    suspend fun getComments(@Path("id") id: Long): List<InquiryCommentDto>

    // 🔥 스태프 전용: 댓글 작성
    @POST("faqs/{id}/comments/")
    suspend fun addComment(
        @Path("id") id: Long,
        @Body request: Map<String, String>
    ): Response<InquiryCommentDto>
}