package com.map.ui

fun cleanCategoryForDisplay(raw: String?): String {
    if (raw.isNullOrBlank()) return ""
    val last = raw.split(">").last().trim()
    return last.replace("병원,의원", "").trim().trim('>', ' ')
}