package com.data.util

import android.util.Log
import com.domain.model.Location
import com.domain.model.Place
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

object PlaceFilterUtil {

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

    fun distanceMeters(a: Location, b: Location): Double {
        val R = 6371000.0
        val dLat = Math.toRadians(b.latitude - a.latitude)
        val dLon = Math.toRadians(b.longitude - a.longitude)
        val lat1 = Math.toRadians(a.latitude)
        val lat2 = Math.toRadians(b.latitude)
        val h = sin(dLat / 2).pow(2.0) + sin(dLon / 2).pow(2.0) * cos(lat1) * cos(lat2)
        return 2 * R * asin(min(1.0, sqrt(h)))
    }

    fun isAllowedByCategory(place: Place, mode: String): Boolean {
        val name = place.title.lowercase()
        val cat = (place.category ?: "").lowercase()

        // 블랙리스트
        if (NEGATIVE_KEYWORDS.any { bad -> name.contains(bad) || cat.contains(bad) }) {
            Log.d("PlaceFilter", "블랙리스트 필터: ${place.title}")
            return false
        }

        return if (mode == "약국") {
            (cat.contains("약국") || name.contains("약국")).also {
                if (!it) Log.d("PlaceFilter", "약국 필터 탈락: ${place.title}")
            }
        } else {
            val allowed = HOSPITAL_POSITIVE.any { key -> cat.contains(key) } ||
                    name.contains("병원") || name.contains("의원") || name.contains("치과")
            if (!allowed) Log.d("PlaceFilter", "병원 필터 탈락: ${place.title}")
            allowed
        }
    }

    fun filterPlaces(
        places: List<Place>,
        center: Location,
        mode: String,
        radiusMeters: Int
    ): List<Place> {
        return places.filter { place ->
            val distance = distanceMeters(center, place.location)
            val withinRadius = distance <= radiusMeters
            val categoryAllowed = isAllowedByCategory(place, mode)

            if (!withinRadius) {
                Log.d("PlaceFilter", "거리 초과 (${distance.toInt()}m): ${place.title}")
            }

            withinRadius && categoryAllowed
        }
    }
}