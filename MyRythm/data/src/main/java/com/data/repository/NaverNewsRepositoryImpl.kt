package com.data.repository

import com.data.mapper.toDomain
import com.data.paging.NaverNewsPagingSource
import com.domain.model.News
import com.domain.repository.NewsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import androidx.paging.PagingData
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.map
import javax.inject.Inject

class NaverNewsRepositoryImpl @Inject constructor(
    private val pagingSourceFactory: NaverNewsPagingSource.Factory
) : NewsRepository {

    override fun getNewsPager(query: String): Flow<PagingData<News>> {
        return Pager(
            config = PagingConfig(pageSize = 10),
            pagingSourceFactory = { pagingSourceFactory.create(query) }
        ).flow.map { pagingData ->
            pagingData.map { it.toDomain() }
        }
    }
}
