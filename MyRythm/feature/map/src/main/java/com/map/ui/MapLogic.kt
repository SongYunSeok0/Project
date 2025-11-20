package com.map.ui

import android.content.Context
import android.location.Geocoder
import android.util.Log
import com.map.data.NaverSearchService
import com.map.data.PlaceItem
import com.naver.maps.geometry.LatLng
import java.util.Locale
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class PlaceWithLatLng(
    val item: PlaceItem,
    val position: LatLng
)

/* -------------------- 업종 화이트/블랙 리스트 -------------------- */

private val NEGATIVE_KEYWORDS = listOf(
    "기공소","용품","재료","장비","기기","상사","도매","유통","제조","판매",
    "쇼핑몰","원자재","도구","부자재","공구","배달","오토바이"
)

private val HOSPITAL_POSITIVE = listOf(
    "종합병원","일반병원","병원","의원","치과의원","치과병원","한의원",
    "내과","외과","정형외과","소아청소년과","산부인과",
    "이비인후과","안과","피부과","비뇨의학과","정신건강의학과",
    "재활의학과","응급의료","가정의학과"
).map { it.lowercase() }

fun cleanHtml(s: String): String = s.replace(Regex("<.*?>"), "").trim()

/** 네이버 category 문자열을 예쁘게 표시용으로 정리 */
fun cleanCategoryForDisplay(raw: String?): String {
    if (raw.isNullOrBlank()) return ""
    // 예: "병원,의원>치과" -> "치과"
    val last = raw.split(">").last().trim()
    return last.replace("병원,의원", "").trim().trim('>', ' ')
}

fun isAllowedByCategory(item: PlaceItem, mode: String): Boolean {
    val name = cleanHtml(item.title).lowercase()
    val cat  = (item.category ?: "").lowercase()

    // 블랙리스트: 이름/카테고리에 제외어가 하나라도 있으면 탈락
    if (NEGATIVE_KEYWORDS.any { bad -> name.contains(bad) || cat.contains(bad) }) {
        return false
    }

    return if (mode == "약국") {
        cat.contains("약국") || name.contains("약국")
    } else {
        if (HOSPITAL_POSITIVE.any { key -> cat.contains(key) }) return true
        name.contains("병원") || name.contains("의원") || name.contains("치과")
    }
}

// 거리(m)
fun distanceMeters(a: LatLng, b: LatLng): Double {
    val R = 6371000.0
    val dLat = Math.toRadians(b.latitude - a.latitude)
    val dLon = Math.toRadians(b.longitude - a.longitude)
    val lat1 = Math.toRadians(a.latitude)
    val lat2 = Math.toRadians(b.latitude)
    val h = sin(dLat / 2).pow(2.0) + sin(dLon / 2).pow(2.0) * cos(lat1) * cos(lat2)
    return 2 * R * asin(min(1.0, sqrt(h)))
}

/** 중심 좌표 기준 지역 이름 힌트 */
private suspend fun areaHint(center: LatLng, context: Context): String =
    withContext(Dispatchers.IO) {
        try {
            val g = Geocoder(context, Locale.KOREA)
            @Suppress("DEPRECATION")
            val list = g.getFromLocation(center.latitude, center.longitude, 1)
            val a = list?.firstOrNull()
            val city = a?.locality ?: a?.adminArea ?: ""
            val gu = a?.subAdminArea ?: ""
            val dong = a?.thoroughfare ?: ""
            listOf(city, gu, dong).filter { it.isNotBlank() }.joinToString(" ")
        } catch (_: Exception) {
            ""
        }
    }

/**
 * 네이버 검색 + 좌표 변환 + 반경/업종 필터
 */
suspend fun searchPlaces(
    context: Context,
    baseQuery: String,
    center: LatLng,
    mode: String,
    radiusMeters: Int = 1500
): List<PlaceWithLatLng> {
    val hint = areaHint(center, context)
    val q = listOf(hint, baseQuery).filter { it.isNotBlank() }.joinToString(" ")

    return try {
        val result = NaverSearchService.api.searchPlaces(
            query = q,
            display = 50,
            start = 1,
            sort = "sim"
        )

        // 디버그: 전화번호/카테고리 확인
        result.items.forEach { item ->
            Log.d(
                "SearchResult",
                "이름=${cleanHtml(item.title)}, 전화번호=${item.telephone ?: ""}, 카테고리=${item.category ?: ""}"
            )
        }

        val converted = result.items.mapNotNull { p ->
            try {
                val lon = p.mapx.toDouble() / 1e7
                val lat = p.mapy.toDouble() / 1e7
                PlaceWithLatLng(p, LatLng(lat, lon))
            } catch (e: Exception) {
                Log.e("MapLogic", "좌표 변환 실패: ${p.title}", e)
                null
            }
        }

        val filtered = converted.filter { pw ->
            distanceMeters(center, pw.position) <= radiusMeters &&
                    isAllowedByCategory(pw.item, mode)
        }

        Log.d("MapLogic", "q=\"$q\" 반경 ${radiusMeters}m 업종='${mode}' 결과 ${filtered.size}개")
        filtered
    } catch (e: Exception) {
        Log.e("MapLogic", "API 검색 실패", e)
        emptyList()
    }
}
