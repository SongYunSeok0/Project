package com.scheduler.navigation

import java.net.URLEncoder
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

internal fun packNames(names: List<String>): String =
    names.joinToString(separator = "|") { URLEncoder.encode(it, StandardCharsets.UTF_8.name()) }

internal fun unpackNames(csv: String?): List<String> =
    if (csv.isNullOrBlank()) emptyList()
    else csv.split("|").map { URLDecoder.decode(it, StandardCharsets.UTF_8.name()) }
