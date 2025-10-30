package com.map

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Geocoder
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.map.data.NaverSearchService
import com.map.data.PlaceItem
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.CameraPosition
import com.naver.maps.map.CameraUpdate
import com.naver.maps.map.compose.ExperimentalNaverMapApi
import com.naver.maps.map.compose.LocationTrackingMode
import com.naver.maps.map.compose.MapProperties
import com.naver.maps.map.compose.MapUiSettings
import com.naver.maps.map.compose.Marker
import com.naver.maps.map.compose.MarkerState
import com.naver.maps.map.compose.NaverMap
import com.naver.maps.map.compose.rememberCameraPositionState
import com.naver.maps.map.compose.rememberFusedLocationSource
import com.naver.maps.map.overlay.OverlayImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

data class PlaceWithLatLng(
    val item: PlaceItem,
    val position: LatLng
)

/* -------------------- 업종 화이트리스트 -------------------- */

private val HOSPITAL_CATS = listOf(
    "병원","종합병원","일반병원","의원","치과","한의원",
    "내과","외과","정형외과","소아청소년과","산부인과",
    "이비인후과","안과","피부과","비뇨의학과","정신건강의학과",
    "재활의학과","응급의료","가정의학과"
)
private val PHARMACY_CATS = listOf("약국")

private fun isAllowedByCategory(item: PlaceItem, mode: String): Boolean {
    val c = item.category?.lowercase() ?: return false
    return if (mode == "약국") {
        PHARMACY_CATS.any { c.contains(it.lowercase()) }
    } else {
        HOSPITAL_CATS.any { c.contains(it.lowercase()) }
    }
}

/* -------------------- 화면 -------------------- */

@OptIn(ExperimentalNaverMapApi::class, ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    val cameraPositionState = rememberCameraPositionState()
    val locationSource = rememberFusedLocationSource()
    var trackingMode by remember { mutableStateOf(LocationTrackingMode.None) }

    val scope = rememberCoroutineScope()
    var myLocation by remember { mutableStateOf<LatLng?>(null) }
    var hasLocationPermission by remember { mutableStateOf(false) }
    var places by remember { mutableStateOf<List<PlaceWithLatLng>>(emptyList()) }
    var selected by remember { mutableStateOf<PlaceWithLatLng?>(null) }
    var selectedChip by remember { mutableStateOf("병원") }

    var mapCenter by remember { mutableStateOf<LatLng?>(null) }
    var showSearchHere by remember { mutableStateOf(false) }

    // 권한
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { grants ->
        val fine = grants[Manifest.permission.ACCESS_FINE_LOCATION] == true
        val coarse = grants[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        hasLocationPermission = fine || coarse
        if (!hasLocationPermission) Log.d("MapScreen", "권한 거부")
    }

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

    // 카메라 이동 감지
    LaunchedEffect(cameraPositionState) {
        snapshotFlow { cameraPositionState.isMoving }.collectLatest { moving ->
            if (moving) {
                showSearchHere = true
                trackingMode = LocationTrackingMode.NoFollow
            } else {
                mapCenter = cameraPositionState.position.target
            }
        }
    }

    // 위치 얻기
    @SuppressLint("MissingPermission")
    fun fetchCurrentLocation(onGot: (LatLng) -> Unit) {
        if (!hasLocationPermission) return
        val cts = CancellationTokenSource()
        fusedLocationClient.getCurrentLocation(
            Priority.PRIORITY_HIGH_ACCURACY, cts.token
        ).addOnSuccessListener { loc ->
            if (loc != null) onGot(LatLng(loc.latitude, loc.longitude))
            else fusedLocationClient.lastLocation.addOnSuccessListener { last ->
                if (last != null) onGot(LatLng(last.latitude, last.longitude))
            }
        }
    }

    // 지역 힌트
    suspend fun areaHint(center: LatLng): String = withContext(Dispatchers.IO) {
        try {
            val g = Geocoder(context, Locale.KOREA)
            @Suppress("DEPRECATION")
            val list = g.getFromLocation(center.latitude, center.longitude, 1)
            val a = list?.firstOrNull()
            val city = a?.locality ?: a?.adminArea ?: ""
            val gu = a?.subAdminArea ?: ""
            val dong = a?.thoroughfare ?: ""
            listOf(city, gu, dong).filter { it.isNotBlank() }.joinToString(" ")
        } catch (_: Exception) { "" }
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

    // 검색: 반경 + 업종 필터
    fun searchPlaces(query: String, center: LatLng?, radiusMeters: Int = 1500) {
        if (center == null) {
            Log.w("MapScreen", "현재 위치 없음. 검색 중단")
            return
        }
        scope.launch {
            try {
                val hint = areaHint(center)
                // 핵심 수정: 힌트 + 사용자가 원하는 키워드 동시 사용
                val q = listOf(hint, query).filter { it.isNotBlank() }.joinToString(" ")

                val result = NaverSearchService.api.searchPlaces(
                    query = q,
                    display = 50,
                    start = 1,
                    sort = "sim"
                )
                Log.d("MapScreen", "q=\"$q\" total=${result.total}, items=${result.items.size}, center=$center")

                val converted = result.items.mapNotNull { p ->
                    try {
                        val lon = p.mapx.toDouble() / 1e7
                        val lat = p.mapy.toDouble() / 1e7
                        PlaceWithLatLng(p, LatLng(lat, lon))
                    } catch (e: Exception) {
                        Log.e("MapScreen", "좌표 변환 실패: ${p.title}", e)
                        null
                    }
                }

                val filtered = converted.filter { pw ->
                    distanceMeters(center, pw.position) <= radiusMeters &&
                            isAllowedByCategory(pw.item, selectedChip) // 업종 화이트리스트 통과만 표시
                }
                places = filtered
                Log.d("MapScreen", "반경 ${radiusMeters}m 업종='${selectedChip}' 결과 ${filtered.size}개")
            } catch (e: Exception) {
                places = emptyList()
                Log.e("MapScreen", "API 검색 실패", e)
            }
        }
    }

    // 처음 진입 시: 따라가기 + 초기 검색
    LaunchedEffect(hasLocationPermission) {
        if (hasLocationPermission) {
            trackingMode = LocationTrackingMode.Follow
            fetchCurrentLocation { here ->
                myLocation = here
                cameraPositionState.move(
                    CameraUpdate.toCameraPosition(CameraPosition(here, 15.0))
                )
                mapCenter = here
                if (places.isEmpty()) searchPlaces(selectedChip, here)
                showSearchHere = false
            }
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("지도", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
                actions = {
                    Row(modifier = Modifier.padding(end = 8.dp)) {
                        FilterChip(
                            selected = selectedChip == "병원",
                            onClick = {
                                if (selectedChip != "병원") {
                                    selectedChip = "병원"; selected = null
                                    val center = mapCenter ?: myLocation
                                    searchPlaces("병원", center)
                                }
                            },
                            label = { Text("병원") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFF6AE0D9),
                                selectedLabelColor = Color.White
                            ),
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        FilterChip(
                            selected = selectedChip == "약국",
                            onClick = {
                                if (selectedChip != "약국") {
                                    selectedChip = "약국"; selected = null
                                    val center = mapCenter ?: myLocation
                                    searchPlaces("약국", center)
                                }
                            },
                            label = { Text("약국") },
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
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
            NaverMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                uiSettings = MapUiSettings(isLocationButtonEnabled = hasLocationPermission),
                locationSource = if (hasLocationPermission) locationSource else null,
                properties = MapProperties(locationTrackingMode = trackingMode),
                onMapClick = { _, _ -> selected = null }
            ) {
                places.forEach { pw ->
                    Marker(
                        state = MarkerState(position = pw.position),
                        icon = OverlayImage.fromResource(R.drawable.icon),
                        captionText = pw.item.title.replace(Regex("<.*?>"), ""),
                        onClick = {
                            selected = pw
                            cameraPositionState.move(CameraUpdate.scrollTo(pw.position))
                            true
                        }
                    )
                }
            }

            CurrentLocationChip(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(16.dp)
            )

            SearchHereChip(
                visible = showSearchHere && mapCenter != null,
                onClick = {
                    val center = mapCenter ?: myLocation
                    selected = null
                    searchPlaces(selectedChip, center)
                    showSearchHere = false
                },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
            )

            selected?.let { pw ->
                PlaceInfoSheet(
                    place = pw.item,
                    modifier = Modifier.align(Alignment.BottomCenter),
                    onClose = { selected = null }
                )
            }
        }
    }
}

/* -------------------- 하단 UI -------------------- */

@Composable
fun PlaceInfoSheet(
    place: PlaceItem,
    modifier: Modifier = Modifier,
    onClose: () -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = place.title.replace(Regex("<.*?>"), ""),
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    modifier = Modifier.weight(1f)
                )
                androidx.compose.material3.Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "닫기",
                    modifier = Modifier
                        .size(24.dp)
                        .clickable { onClose() }
                )
            }
            Spacer(modifier = Modifier.padding(top = 8.dp))
            Text(text = place.address, fontSize = 14.sp, color = Color.Gray)
            place.category?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF607D8B)
                )
            }
        }
    }
}

@Composable
fun CurrentLocationChip(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.zIndex(1f),
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

@Composable
fun SearchHereChip(
    visible: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (!visible) return
    Surface(
        modifier = modifier.zIndex(1f),
        shape = RoundedCornerShape(50),
        color = Color.White,
        shadowElevation = 4.dp,
        onClick = onClick
    ) {
        Text(
            text = "이 위치에서 검색",
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            fontSize = 14.sp,
            color = Color(0xFF4A5565)
        )
    }
}
