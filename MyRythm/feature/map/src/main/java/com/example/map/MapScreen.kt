package com.example.map

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import androidx.compose.ui.platform.LocalContext
import com.google.android.gms.location.LocationServices
import com.naver.maps.map.CameraUpdate
import com.naver.maps.map.compose.MapProperties
import com.naver.maps.map.compose.MapUiSettings
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.CameraPosition
import com.naver.maps.map.compose.ExperimentalNaverMapApi
import com.naver.maps.map.compose.Marker
import com.naver.maps.map.compose.NaverMap
import com.naver.maps.map.compose.rememberCameraPositionState
import com.naver.maps.map.overlay.OverlayImage
import com.naver.maps.map.compose.MarkerState
import com.naver.maps.map.compose.LocationTrackingMode
import kotlin.random.Random

// 장소 데이터를 위한 데이터 클래스
data class Place(
    val id: Int,
    val name: String,
    val latLng: LatLng,
    val type: String // "병원" 또는 "약국"
)

@OptIn(ExperimentalNaverMapApi::class)
@Composable
fun MapScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition(LatLng(37.5666102, 126.9783881), 14.0)
    }

    var myLocation by remember { mutableStateOf<LatLng?>(null) }
    var hasLocationPermission by remember {
        mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
    }
    // 병원/약국 목록을 저장할 상태 변수
    var places by remember { mutableStateOf<List<Place>>(emptyList()) }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted -> hasLocationPermission = isGranted }
    )

    // 권한 요청
    LaunchedEffect(Unit) {
        if (!hasLocationPermission) {
            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    // 위치 추적 및 초기 데이터 로드
    @SuppressLint("MissingPermission")
    LaunchedEffect(hasLocationPermission) {
        if (hasLocationPermission) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                location?.let {
                    val currentLatLng = LatLng(it.latitude, it.longitude)
                    myLocation = currentLatLng
                    cameraPositionState.move(CameraUpdate.toCameraPosition(CameraPosition(currentLatLng, 15.0)))

                    // ★★★ 수정된 핵심 부분 ★★★
                    // 내 위치를 성공적으로 찾은 후, 장소 목록이 비어있으면 기본 "병원" 목록을 바로 불러옵니다.
                    if (places.isEmpty()) {
                        places = generateFakePlaces(currentLatLng, "병원", 10)
                        Log.d("MapScreen", "초기 '병원' 데이터 로드 완료: ${places.size}개")
                    }
                }
            }
        }
    }

    Scaffold(
        topBar = {
            MapTopAppBar(
                // 칩을 클릭했을 때의 동작을 정의해서 전달
                onChipSelected = { type ->
                    myLocation?.let {
                        // 내 위치가 있을 때만 가짜 데이터 생성
                        places = generateFakePlaces(it, type, 10)
                        Log.d("MapScreen", "'${type}' 선택됨. ${places.size}개의 장소 생성.")
                    }
                }
            )
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Box(modifier = Modifier
            .padding(innerPadding)
            .fillMaxSize()) {
            NaverMap(
                modifier = Modifier.matchParentSize(),
                cameraPositionState = cameraPositionState,
                properties = MapProperties(
                    locationTrackingMode = if (hasLocationPermission) LocationTrackingMode.Follow else LocationTrackingMode.None
                ),
                uiSettings = MapUiSettings(isLocationButtonEnabled = hasLocationPermission)
            ) {
                // 내 위치 마커
                myLocation?.let { location ->
                    Marker(
                        state = MarkerState(position = location),
                        icon = OverlayImage.fromResource(R.drawable.icon), // 적절한 아이콘으로 변경하세요
                        captionText = "내 위치",
                        zIndex = 10 // 다른 마커들보다 위에 보이도록 zIndex 설정
                    )
                }

                // 주변 장소 마커들 표시
                places.forEach { place ->
                    Marker(
                        state = MarkerState(position = place.latLng),
                        icon = OverlayImage.fromResource(R.drawable.icon),
                        captionText = place.name,
                        onClick = {
                            Log.d("MapScreen", "${place.name} 클릭됨")
                            true // 이벤트 소비
                        }
                    )
                }
            }

            Box(modifier = Modifier
                .matchParentSize()
                .zIndex(1f)) {
                CurrentLocationChip(modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(16.dp))
            }
        }
    }
}

/**
 * 내 위치 주변에 가짜 장소 데이터를 생성하는 함수
 */
private fun generateFakePlaces(center: LatLng, type: String, count: Int): List<Place> {
    val random = Random(System.currentTimeMillis())
    return (1..count).map { i ->
        // 0.01도는 대략 1.1km에 해당. -0.01 ~ +0.01 사이의 값을 더해 주변에 무작위로 배치
        val randomLat = center.latitude + (random.nextDouble() * 0.02 - 0.01)
        val randomLng = center.longitude + (random.nextDouble() * 0.02 - 0.01)
        Place(id = i, name = "$type $i", latLng = LatLng(randomLat, randomLng), type = type)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapTopAppBar(onChipSelected: (String) -> Unit) {
    var selectedChip by remember { mutableStateOf("병원") }

    // ★★★ 이 부분의 LaunchedEffect는 삭제했습니다. ★★★
    // 초기 데이터 로딩은 MapScreen의 위치 확인 로직으로 이동했기 때문입니다.

    CenterAlignedTopAppBar(
        title = { Text("지도", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
        actions = {
            Row {
                FilterChip(
                    selected = selectedChip == "병원",
                    onClick = {
                        selectedChip = "병원"
                        onChipSelected("병원")
                    },
                    label = { Text("병원") },
                    // 스타일 코드 복원
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFF6AE0D9),
                        selectedLabelColor = Color.White
                    ),
                    modifier = Modifier.padding(end = 8.dp)
                )
                FilterChip(
                    selected = selectedChip == "약국",
                    onClick = {
                        selectedChip = "약국"
                        onChipSelected("약국")
                    },
                    label = { Text("약국") },
                    // 스타일 코드 복원
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFF6AE0D9),
                        selectedLabelColor = Color.White
                    )
                )
            }
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = Color(0xFFB5E5E1).copy(alpha = 0.36f)
        )
    )
}

@Composable
fun CurrentLocationChip(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(50),
        color = Color.White,
        shadowElevation = 4.dp
    ) {
        Text(
            text = "현재 위치 기준",
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            fontSize = 14.sp,
            color = Color(0xFF4A5565)
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun MapScreenPreview() {
    MaterialTheme {
        MapScreen()
    }
}
