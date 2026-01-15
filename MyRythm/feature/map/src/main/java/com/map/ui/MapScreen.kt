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
    val lifecycleOwner = LocalLifecycleOwner.current

    val fusedLocationClient = remember {
        LocationServices.getFusedLocationProviderClient(context)
    }

    var hasLocationPermission by remember { mutableStateOf(false) }
    var naverMap by remember { mutableStateOf<NaverMap?>(null) }
    val mapView = remember { MapView(context) }
    val markers = remember { mutableListOf<Marker>() }

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

    /* ---------- Lifecycle 처리 ---------- */
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_CREATE -> mapView.onCreate(null)
                Lifecycle.Event.ON_START -> mapView.onStart()
                Lifecycle.Event.ON_RESUME -> mapView.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                Lifecycle.Event.ON_STOP -> mapView.onStop()
                Lifecycle.Event.ON_DESTROY -> mapView.onDestroy()
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

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
    LaunchedEffect(hasLocationPermission, naverMap) {
        if (hasLocationPermission && naverMap != null) {
            naverMap?.locationOverlay?.isVisible = true
            viewModel.enableLocationFollow()
            fetchCurrentLocation { here ->
                viewModel.updateMyLocation(here)
                naverMap?.moveCamera(
                    CameraUpdate.toCameraPosition(CameraPosition(here, 15.0))
                )
                if (uiState.places.isEmpty()) {
                    viewModel.searchAround(here)
                }
            }
        }
    }

    /* ---------- 위치 추적 모드 업데이트 ---------- */
    LaunchedEffect(uiState.trackingMode, naverMap, hasLocationPermission) {
        naverMap?.let { map ->
            if (hasLocationPermission) {
                map.locationTrackingMode = uiState.trackingMode
            }
        }
    }

    /* ---------- 마커 업데이트 ---------- */
    LaunchedEffect(uiState.places, naverMap) {
        naverMap?.let { map ->
            // 기존 마커 제거
            markers.forEach { it.map = null }
            markers.clear()

            // 새 마커 추가
            uiState.places.forEach { pw ->
                val marker = Marker().apply {
                    position = pw.position
                    icon = OverlayImage.fromResource(R.drawable.icon)
                    captionText = pw.title
                    setOnClickListener {
                        viewModel.onMarkerSelected(pw)
                        map.moveCamera(CameraUpdate.scrollTo(pw.position))
                        true
                    }
                    this.map = map
                }
                markers.add(marker)
            }
        }
    }

    /* ---------- 카메라 이동 감지 ---------- */
    LaunchedEffect(naverMap) {
        naverMap?.addOnCameraChangeListener { reason, animated ->
            val target = naverMap?.cameraPosition?.target
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
                    getMapAsync(OnMapReadyCallback { map ->
                        naverMap = map

                        // 위치 추적 설정
                        if (hasLocationPermission) {
                            map.locationSource = locationSource
                            map.locationOverlay.isVisible = true
                            map.uiSettings.isLocationButtonEnabled = false
                        }

                        // 초기 카메라 위치
                        val initialPosition = LatLng(37.5666805, 126.9784147) // 서울시청
                        map.moveCamera(CameraUpdate.scrollTo(initialPosition))

                        // 지도 클릭 리스너
                        map.setOnMapClickListener { _, _ ->
                            viewModel.onBottomSheetDismiss()
                        }
                    })
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
                    naverMap?.moveCamera(CameraUpdate.scrollTo(it))
                    viewModel.onRecenter()
                }
            },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 80.dp, end = 16.dp)
                .zIndex(1f)
        )

        // ✅ 검색 결과 리스트
        PlaceListSection(
            places = uiState.places,
            onPlaceClick = { place ->
                viewModel.onMarkerSelected(place)
                naverMap?.moveCamera(CameraUpdate.scrollTo(place.position))
            },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .zIndex(1f)
        )

        // 하단 시트 섹션
        MapBottomSheetSection(
            selected = uiState.selected,
            showBottomSheet = uiState.showBottomSheet,
            onBottomSheetDismiss = { viewModel.onBottomSheetDismiss() },
            myLocation = uiState.myLocation,
            onRecenterClick = {
                uiState.myLocation?.let {
                    naverMap?.moveCamera(CameraUpdate.scrollTo(it))
                    viewModel.onRecenter()
                }
            },
            modifier = Modifier.align(Alignment.BottomStart)
        )
    }
}

private const val LOCATION_PERMISSION_REQUEST_CODE = 1000