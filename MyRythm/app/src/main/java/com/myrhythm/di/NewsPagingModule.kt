package com.myrhythm.di

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.map
import com.data.mapper.toDomain
import com.data.network.datasource.NaverNewsPagingSource
import com.data.network.datasource.NewsHtmlParser
import com.data.network.datasource.NewsRemoteDataSource
import com.domain.model.News
import com.news.utils.NewsPagingFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.map
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NewsPagingModule {

    @Provides
    @Singleton
    fun provideNewsPagingFactory(
        remote: NewsRemoteDataSource,
        parser: NewsHtmlParser
    ): NewsPagingFactory = NewsPagingFactory { query ->
        Pager(
            config = PagingConfig(pageSize = 10, enablePlaceholders = false),
            pagingSourceFactory = { NaverNewsPagingSource(remote, parser, query) }
        ).flow.map { pagingData ->
            pagingData.map { dto ->
                val news = dto.toDomain()
                android.util.Log.e(
                    "NEWS_DEBUG",
                    "dtoTitle=[${dto.title}] dtoDesc=[${dto.description}] -> newsTitle=[${news.title}] newsDesc=[${news.description}]"
                )
                news
            }
        }

    }
}
