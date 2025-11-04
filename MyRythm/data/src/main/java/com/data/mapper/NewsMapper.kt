package com.data.mapper

import com.data.remote.dto.NewsItem
import com.domain.model.News

fun NewsItem.toDomain(): News {
    return News(
        title = title,
        link = link,
        description = description,
        date = pubDate,
        image = image
    )
}