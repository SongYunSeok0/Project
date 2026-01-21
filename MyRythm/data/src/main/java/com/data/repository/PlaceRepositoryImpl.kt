package com.data.repository

import android.content.Context
import android.util.Log
import com.data.mapper.toDomain
import com.data.network.api.NaverSearchApi
import com.data.util.LocationUtil
import com.data.util.PlaceFilterUtil
import com.data.util.apiResultOf
import com.domain.model.ApiResult
import com.domain.model.Location
import com.domain.model.Place
import com.domain.repository.PlaceRepository
import com.naver.maps.geometry.LatLng
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlaceRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val naverSearchApi: NaverSearchApi
) : PlaceRepository {

    override suspend fun searchPlaces(
        query: String,
        center: Location,
        mode: String,
        radiusMeters: Int
    ): ApiResult<List<Place>> = apiResultOf {

        // 1. 지역 힌트
        val hint = LocationUtil.getAreaHint(center, context)
        val searchQuery = listOf(hint, query)
            .filter { it.isNotBlank() }
            .joinToString(" ")

        // 2. API 호출
        val result = naverSearchApi.searchPlaces(
            query = searchQuery,
            display = 50,
            start = 1,
            sort = "sim"
        )

        // 3. 좌표 변환 및 매핑
        val places = result.items.mapNotNull { item ->
            try {
                val lon = item.mapx.toDouble() / 1e7
                val lat = item.mapy.toDouble() / 1e7
                val latLng = LatLng(lat, lon)

                Log.d(
                    "PlaceRepository",
                    "이름=${item.title}, 전화=${item.telephone ?: "없음"}, 카테고리=${item.category ?: "없음"}"
                )

                item.toDomain(latLng)
            } catch (e: Exception) {
                Log.e("PlaceRepository", "좌표 변환 실패: ${item.title}", e)
                null
            }
        }

        // 4. 필터링
        val filtered = PlaceFilterUtil.filterPlaces(
            places = places,
            center = center,
            mode = mode,
            radiusMeters = radiusMeters
        )

        Log.d(
            "PlaceRepository",
            "검색어=\"$searchQuery\" 반경=${radiusMeters}m 업종='${mode}' 결과=${filtered.size}개"
        )

        filtered
    }
}