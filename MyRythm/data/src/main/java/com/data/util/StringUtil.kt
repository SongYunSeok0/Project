package com.data.util

fun cleanHtml(input: String?): String =
    input?.let { org.jsoup.Jsoup.parse(it).text() }.orEmpty()


fun cleanCategoryForDisplay(raw: String?): String {
    if (raw.isNullOrBlank()) return ""
    val last = raw.split(">").last().trim()
    return last.replace("병원,의원", "").trim().trim('>', ' ')
}