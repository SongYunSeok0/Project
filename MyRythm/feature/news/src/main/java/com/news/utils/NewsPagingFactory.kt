package com.news.utils

import androidx.paging.PagingData
import com.domain.model.News
import kotlinx.coroutines.flow.Flow

fun interface NewsPagingFactory {
    fun create(query: String): Flow<PagingData<News>>
}