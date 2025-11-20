package com.map.ui

import android.content.Context
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.naver.maps.geometry.LatLng
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.launch
import com.naver.maps.map.compose.LocationTrackingMode


data class MapUiState(
    val searchQuery: String = "",
    val selectedChip: String = "병원",
    val myLocation: LatLng? = null,
    val mapCenter: LatLng? = null,
    val trackingMode: LocationTrackingMode = LocationTrackingMode.None,
    val places: List<PlaceWithLatLng> = emptyList(),
    val selected: PlaceWithLatLng? = null,
    val showBottomSheet: Boolean = false,
    val showSearchHere: Boolean = false
)

@HiltViewModel
class MapViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {

    var uiState by mutableStateOf(MapUiState())
        private set

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
        searchAround()   // 모드 바꾸면 바로 재검색
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

    /**
     * 중심(centerOverride)이 있으면 그거 기준, 없으면 mapCenter / myLocation 기준으로 검색
     */
    fun searchAround(centerOverride: LatLng? = null) {
        val center = centerOverride ?: uiState.mapCenter ?: uiState.myLocation ?: return
        val query = uiState.searchQuery.ifBlank { uiState.selectedChip }

        viewModelScope.launch {
            try {
                val result = searchPlaces(
                    context = context,
                    baseQuery = query,
                    center = center,
                    mode = uiState.selectedChip
                )
                uiState = uiState.copy(
                    places = result,
                    showSearchHere = false,
                    selected = null,
                    showBottomSheet = false
                )
            } catch (e: Exception) {
                Log.e("MapViewModel", "searchAround 실패", e)
                uiState = uiState.copy(places = emptyList())
            }
        }
    }
}
