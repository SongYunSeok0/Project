package com.domain.repository

import androidx.paging.PagingData
import com.domain.model.News
import kotlinx.coroutines.flow.Flow

interface NewsRepository {
    fun getNewsPager(query: String): Flow<PagingData<News>>
}
