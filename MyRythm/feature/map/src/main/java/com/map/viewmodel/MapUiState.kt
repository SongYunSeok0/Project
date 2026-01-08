package com.map.viewmodel

import com.map.ui.PlaceWithLatLng
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.LocationTrackingMode

data class MapUiState(
    val searchQuery: String = "",
    val selectedChip: String = "병원",
    val myLocation: LatLng? = null,
    val mapCenter: LatLng? = null,
    val trackingMode: LocationTrackingMode = LocationTrackingMode.None,
    val places: List<PlaceWithLatLng> = emptyList(),
    val selected: PlaceWithLatLng? = null,
    val showBottomSheet: Boolean = false,
    val showSearchHere: Boolean = false,
    val isLoading: Boolean = false
)