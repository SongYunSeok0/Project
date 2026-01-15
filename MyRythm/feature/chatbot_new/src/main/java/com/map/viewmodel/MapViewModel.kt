// map/viewmodel/MapViewModel.kt
package com.map.viewmodel

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.domain.model.ApiResult
import com.domain.usecase.SearchPlacesUseCase
import com.map.mapper.toDomainLocation
import com.map.mapper.toUiModel
import com.map.ui.PlaceWithLatLng
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.LocationTrackingMode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MapViewModel @Inject constructor(
    private val searchPlacesUseCase: SearchPlacesUseCase
) : ViewModel() {

    var uiState by mutableStateOf(MapUiState())
        private set

    private var searchJob: Job? = null

    /* ---------- 상태 변경 메소드 ---------- */

    fun updateSearchQuery(value: String) {
        uiState = uiState.copy(searchQuery = value)
    }

    fun enableLocationFollow() {
        uiState = uiState.copy(trackingMode = LocationTrackingMode.Follow)
    }

    fun updateMyLocation(location: LatLng) {
        uiState = uiState.copy(
            myLocation = location,
            mapCenter = location
        )
    }

    fun onCameraMove(isMoving: Boolean, center: LatLng) {
        uiState = if (isMoving) {
            uiState.copy(
                trackingMode = LocationTrackingMode.NoFollow,
                showSearchHere = true
            )
        } else {
            uiState.copy(
                mapCenter = center,
                showSearchHere = true
            )
        }
    }

    fun onModeChange(mode: String) {
        if (uiState.selectedChip == mode) return
        uiState = uiState.copy(selectedChip = mode)
        searchAround()
    }

    fun onMarkerSelected(place: PlaceWithLatLng) {
        uiState = uiState.copy(
            selected = place,
            showBottomSheet = true
        )
    }

    fun onBottomSheetDismiss() {
        uiState = uiState.copy(
            selected = null,
            showBottomSheet = false
        )
    }

    fun onRecenter() {
        uiState = uiState.copy(
            trackingMode = LocationTrackingMode.Follow,
            showSearchHere = false
        )
    }

    fun onSearchHere() {
        searchAround()
    }

    /* ---------- 검색 로직 ---------- */

    fun searchAround(centerOverride: LatLng? = null) {
        val center = centerOverride ?: uiState.mapCenter ?: uiState.myLocation
        if (center == null) {
            Log.w("MapViewModel", "검색 중심 위치가 없습니다")
            return
        }

        val query = uiState.searchQuery.ifBlank { uiState.selectedChip }

        // ✅ 기존 검색 작업 취소
        searchJob?.cancel()

        // ✅ 즉시 로딩 상태로 변경
        uiState = uiState.copy(isLoading = true)

        searchJob = viewModelScope.launch {
            try {
                val domainLocation = center.toDomainLocation()

                when (val result = searchPlacesUseCase(
                    query = query,
                    center = domainLocation,
                    mode = uiState.selectedChip,
                    radiusMeters = 1500
                )) {
                    is ApiResult.Success -> {
                        val uiPlaces = result.data.map { it.toUiModel() }

                        uiState = uiState.copy(
                            places = uiPlaces,
                            showSearchHere = false,
                            selected = null,
                            showBottomSheet = false,
                            isLoading = false  // ✅ 로딩 완료
                        )
                    }
                    is ApiResult.Failure -> {
                        Log.e("MapViewModel", "searchAround 실패: ${result.error}")
                        uiState = uiState.copy(
                            places = emptyList(),
                            isLoading = false  // ✅ 로딩 완료
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e("MapViewModel", "searchAround 예외: ${e.message}")
                uiState = uiState.copy(
                    places = emptyList(),
                    isLoading = false  // ✅ 로딩 완료
                )
            }
        }
    }
}