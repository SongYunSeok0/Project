package com.map.ui

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.zIndex
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.map.ui.components.MapBottomSheetSection
import com.map.ui.components.MapSearchHeader
import com.map.ui.components.PlaceListSection
import com.map.ui.components.RoundRecenterButton
import com.map.viewmodel.MapViewModel
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.CameraPosition
import com.naver.maps.map.CameraUpdate
import com.naver.maps.map.MapView
import com.naver.maps.map.NaverMap
import com.naver.maps.map.OnMapReadyCallback
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.overlay.OverlayImage
import com.naver.maps.map.util.FusedLocationSource
import com.shared.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    modifier: Modifier = Modifier,
    viewModel: MapViewModel = hiltViewModel()
) {
    val focusManager = LocalFocusManager.current
    val context = LocalContext.current

    // 0115 naverMap 포함 이벤트 관련 mapController.kt 생성, 권한 이동(캡슐화)
    val mapController = rememberMapController()

    val fusedLocationClient = remember {
        LocationServices.getFusedLocationProviderClient(context)
    }

    var hasLocationPermission by remember { mutableStateOf(false) }
    val mapView = remember { MapView(context) }

    val locationSource = remember {
        FusedLocationSource(
            context as androidx.activity.ComponentActivity,
            LOCATION_PERMISSION_REQUEST_CODE
        )
    }

    val uiState = viewModel.uiState

    /* ---------- 권한 런처 ---------- */
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { grants ->
        val fine = grants[Manifest.permission.ACCESS_FINE_LOCATION] == true
        val coarse = grants[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        hasLocationPermission = fine || coarse
        if (!hasLocationPermission) Log.d("MapScreen", "권한 거부")
    }

    /* ---------- 권한 확인 ---------- */
    LaunchedEffect(Unit) {
        val fine = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val coarse = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        hasLocationPermission = fine || coarse
        if (!hasLocationPermission) {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    // 0115 기존의 Lifecycle 로직 -> MapLifecycleHandler.kt로 분리 (상태 호이스팅)
    MapLifecycleHandler(mapView)

    /* ---------- 현재 위치 얻기 ---------- */
    @SuppressLint("MissingPermission")
    fun fetchCurrentLocation(onGot: (LatLng) -> Unit) {
        if (!hasLocationPermission) return
        val cts = CancellationTokenSource()
        fusedLocationClient.getCurrentLocation(
            Priority.PRIORITY_HIGH_ACCURACY,
            cts.token
        ).addOnSuccessListener { loc ->
            if (loc != null) {
                onGot(LatLng(loc.latitude, loc.longitude))
            } else {
                fusedLocationClient.lastLocation.addOnSuccessListener { last ->
                    if (last != null) onGot(LatLng(last.latitude, last.longitude))
                }
            }
        }
    }

    /* ---------- 권한 허용 후 초기 위치/검색 ---------- */
    //0115 navermap -> MapController.kt로 상태호이스팅 진행하면서 코드 일부 변경
    LaunchedEffect(hasLocationPermission, mapController.getNaverMap()) {
        val map = mapController.getNaverMap()

        if (hasLocationPermission && map != null) {
            map.locationOverlay?.isVisible = true
            viewModel.enableLocationFollow()
            fetchCurrentLocation { here ->
                viewModel.updateMyLocation(here)

                // 0115 naverMap?.moveCamera -> mapController.moveCameraWithZoom
                mapController.moveCameraWithZoom(here, 15.0)

                if (uiState.places.isEmpty()) {
                    viewModel.searchAround(here)
                }
            }
        }
    }

    /* ---------- 위치 추적 모드 업데이트 ---------- */
    // 0115 naverMap → mapController.getNaverMap()
    // 0115 map.locationTrackingMode → mapController.setLocationTrackingMode
    LaunchedEffect(uiState.trackingMode, mapController.getNaverMap(), hasLocationPermission) {
        if (hasLocationPermission && mapController.getNaverMap() != null) {
            mapController.setLocationTrackingMode(uiState.trackingMode)
        }
    }

    /* ---------- 마커 업데이트 ---------- */
    // 0115 마커 관련 코드 MapController.kt로 이동
    LaunchedEffect(uiState.places, mapController.getNaverMap()) {
        if (mapController.getNaverMap() != null) {
            mapController.updateMarkers(uiState.places) { place ->
                viewModel.onMarkerSelected(place)
            }
        }
    }

    /* ---------- 카메라 이동 감지 ---------- */
    // 0115 naverMap → mapController.getNaverMap()
    // naverMap?.addOnCameraChangeListener -> mapController.setOnCameraChangeListener
    LaunchedEffect(mapController.getNaverMap()) {
        mapController.setOnCameraChangeListener { _, animated, target ->
            if (target != null) {
                viewModel.onCameraMove(animated, target)
            }
        }
    }

    /* ---------- UI ---------- */
    Box(modifier = modifier.fillMaxSize()) {
        // 지도
        AndroidView(
            factory = {
                mapView.apply {
                    getMapAsync { map ->
                        // 0115
                        mapController.setNaverMap(map)

                        // 위치 추적 설정
                        if (hasLocationPermission) {
                            map.locationSource = locationSource
                            map.locationOverlay.isVisible = true
                            map.uiSettings.isLocationButtonEnabled = false
                        }

                        // 초기 카메라 위치
                        val initialPosition = LatLng(37.5666805, 126.9784147) // 서울시청
                        map.moveCamera(CameraUpdate.scrollTo(initialPosition))

                        // 0115
                        mapController.setOnMapClickListener {
                            viewModel.onBottomSheetDismiss()
                        }
                    }
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        // 검색 헤더
        MapSearchHeader(
            searchQuery = uiState.searchQuery,
            onValueChange = viewModel::updateSearchQuery,
            onSearchAroundClick = {
                focusManager.clearFocus(force = true)
                val center = uiState.mapCenter ?: uiState.myLocation
                viewModel.searchAround(center)
            },
            onModeChange = viewModel::onModeChange,
            selectedChip = uiState.selectedChip,
            isLoading = uiState.isLoading,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .zIndex(1f)
        )

        RoundRecenterButton(
            onClick = {
                uiState.myLocation?.let {
                    // 0115
                    mapController.moveCamera(it)
                    viewModel.onRecenter()
                }
            },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 80.dp, end = 16.dp)
                .zIndex(1f)
        )

        // ✅ 검색 결과 리스트
        // 0115 mapController.moveCamera로 변경
        PlaceListSection(
            places = uiState.places,
            onPlaceClick = { place ->
                viewModel.onMarkerSelected(place)
                mapController.moveCamera(place.position)
            },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .zIndex(1f)
        )

        // 하단 시트 섹션
        // 0115 mapController.moveCamera로 변경
        MapBottomSheetSection(
            selected = uiState.selected,
            showBottomSheet = uiState.showBottomSheet,
            onBottomSheetDismiss = { viewModel.onBottomSheetDismiss() },
            myLocation = uiState.myLocation,
            onRecenterClick = {
                uiState.myLocation?.let {
                    mapController.moveCamera(it)
                    viewModel.onRecenter()
                }
            },
            modifier = Modifier.align(Alignment.BottomStart)
        )
    }

    // 0115 메모리 누수 방지 & 상태 초기화 용도로 리소스 정리 역할의 코드 추가
    DisposableEffect(Unit) {
        onDispose {
            mapController.cleanup()
        }
    }
}

private const val LOCATION_PERMISSION_REQUEST_CODE = 1000