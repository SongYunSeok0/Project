package com.data.mapper

import com.data.network.dto.news.NaverNewsItem
import com.domain.model.News

fun NaverNewsItem.toDomain(): News =
    News(
        title = title,
        link = link,
        description = description,
        pubDate = pubDate,
        image = image
    )
