package com.data.util

fun cleanHtml(s: String): String = s.replace(Regex("<.*?>"), "").trim()

fun cleanCategoryForDisplay(raw: String?): String {
    if (raw.isNullOrBlank()) return ""
    val last = raw.split(">").last().trim()
    return last.replace("병원,의원", "").trim().trim('>', ' ')
}