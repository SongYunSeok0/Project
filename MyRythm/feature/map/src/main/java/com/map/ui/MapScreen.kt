package com.map.ui

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.core.content.ContextCompat
import com.common.design.R
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.map.data.NaverSearchService
import com.map.data.PlaceItem
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.CameraPosition
import com.naver.maps.map.CameraUpdate
import com.naver.maps.map.compose.*
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

/* -------------------- 업종 화이트/블랙 리스트 -------------------- */

private val NEGATIVE_KEYWORDS = listOf(
    "기공소","용품","재료","장비","기기","상사","도매","유통","제조","판매",
    "쇼핑몰","원자재","도구","부자재","공구"
)

private val HOSPITAL_POSITIVE = listOf(
    "종합병원","일반병원","병원","의원","치과의원","치과병원","한의원",
    "내과","외과","정형외과","소아청소년과","산부인과",
    "이비인후과","안과","피부과","비뇨의학과","정신건강의학과",
    "재활의학과","응급의료","가정의학과"
).map { it.lowercase() }

private fun cleanHtml(s: String) = s.replace(Regex("<.*?>"), "").trim()

/** 네이버 category 문자열을 예쁘게 표시용으로 정리 */
private fun cleanCategoryForDisplay(raw: String?): String {
    if (raw.isNullOrBlank()) return ""
    // 예: "병원,의원>치과" -> "치과"
    val last = raw.split(">").last().trim()
    // "병원,의원" 같은 상위군 제거
    return last.replace("병원,의원", "").trim().trim('>', ' ')
}

private fun isAllowedByCategory(item: PlaceItem, mode: String): Boolean {
    val name = cleanHtml(item.title).lowercase()
    val cat  = (item.category ?: "").lowercase()

    // 블랙리스트: 이름/카테고리에 제외어가 하나라도 있으면 탈락
    if (NEGATIVE_KEYWORDS.any { bad -> name.contains(bad) || cat.contains(bad) }) {
        return false
    }

    return if (mode == "약국") {
        // 약국은 '약국'이 카테고리 또는 이름에 포함
        cat.contains("약국") || name.contains("약국")
    } else {
        // 병원/의원 계열 우선: 카테고리 기반
        if (HOSPITAL_POSITIVE.any { key -> cat.contains(key) }) return true
        // 폴백: 이름 키워드(블랙리스트는 이미 제외됨)
        name.contains("병원") || name.contains("의원") || name.contains("치과")
    }
}

/* -------------------- 화면 -------------------- */

@OptIn(ExperimentalNaverMapApi::class, ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(modifier: Modifier = Modifier) {
    var showBottomSheet by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

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
    var selectedChip by remember { mutableStateOf("병원") } // 검색 기본 업종

    var mapCenter by remember { mutableStateOf<LatLng?>(null) }

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

    // 검색: 반경 + 업종 필터 (+ 블랙리스트 적용)
    fun searchPlaces(query: String, center: LatLng?, radiusMeters: Int = 1500) {
        if (center == null) {
            Log.w("MapScreen", "현재 위치 없음. 검색 중단")
            return
        }
        scope.launch {
            try {
                val hint = areaHint(center)
                val q = listOf(hint, query).filter { it.isNotBlank() }.joinToString(" ")

                val result = NaverSearchService.api.searchPlaces(
                    query = q, display = 50, start = 1, sort = "sim"
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
                        Log.e("MapScreen", "좌표 변환 실패: ${p.title}", e)
                        null
                    }
                }

                val filtered = converted.filter { pw ->
                    distanceMeters(center, pw.position) <= radiusMeters &&
                            isAllowedByCategory(pw.item, selectedChip)
                }

                places = filtered
                Log.d("MapScreen", "q=\"$q\" 반경 ${radiusMeters}m 업종='${selectedChip}' 결과 ${filtered.size}개")
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
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {

        // 지도
        NaverMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            uiSettings = MapUiSettings(isLocationButtonEnabled = false),
            locationSource = if (hasLocationPermission) locationSource else null,
            properties = MapProperties(locationTrackingMode = trackingMode),
            onMapClick = { _, _ -> selected = null }
        ) {
            places.forEach { pw ->
                Marker(
                    state = MarkerState(position = pw.position),
                    icon = OverlayImage.fromResource(R.drawable.icon),
                    captionText = cleanHtml(pw.item.title),
                    onClick = {
                        selected = pw
                        showBottomSheet = true
                        cameraPositionState.move(CameraUpdate.scrollTo(pw.position))
                        true
                    }
                )
            }
        }

        // 상단 검색창
        Row(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .zIndex(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("병원, 약국 등 검색어를 입력하세요") },
                singleLine = true,
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp),
                shape = RoundedCornerShape(30.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                )
            )

            Spacer(modifier = Modifier.width(8.dp))

            Button(
                onClick = {
                    val center = mapCenter ?: myLocation
                    selected = null
                    // 검색어 비어있으면 업종 기본값(병원)으로
                    val q = searchQuery.ifBlank { "병원" }
                    searchPlaces(q, center)
                },
                shape = RoundedCornerShape(30.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6AE0D9))
            ) {
                Text("검색", color = Color.White)
            }
        }

        // 하단 시트
        if (selected != null && showBottomSheet) {
            ModalBottomSheet(
                onDismissRequest = {
                    showBottomSheet = false
                    selected = null
                },
                sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
            ) {
                PlaceInfoContent(
                    place = selected!!.item,
                    onClose = {
                        showBottomSheet = false
                        selected = null
                    },
                    myLocation = myLocation
                )
            }
        }

        // 내 위치 버튼
        RoundRecenterButton(
            onClick = {
                myLocation?.let {
                    cameraPositionState.move(CameraUpdate.scrollTo(it))
                    trackingMode = LocationTrackingMode.Follow
                }
            },
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(16.dp)
        )
    }
}

/* -------------------- 하단 시트 컨텐츠 -------------------- */

@Composable
fun PlaceInfoContent(
    place: PlaceItem,
    onClose: () -> Unit,
    myLocation: LatLng?
) {
    val context = LocalContext.current

    val cleanTitle = cleanHtml(place.title)
    val prettyCategory = cleanCategoryForDisplay(place.category)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        // 상호명 (뒤에 업종 붙임)
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = cleanTitle + if (prettyCategory.isNotBlank()) " · $prettyCategory" else "",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }

        // 주소
        Text(text = place.address.ifBlank { "주소 정보 없음" })

        // 전화(대부분 빈 값일 수 있음)
        place.telephone?.takeIf { it.isNotBlank() }?.let {
            Text(text = "전화번호: $it")
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            // 길찾기 (네이버 지도 앱 우선, 없으면 웹 Fallback)
            Button(
                onClick = {
                    val start = myLocation
                    val destX = place.mapx.toDoubleOrNull() // 네이버 검색 API TM128 X
                    val destY = place.mapy.toDoubleOrNull() // 네이버 검색 API TM128 Y
                    val placeName = cleanTitle

                    if (start != null && destX != null && destY != null) {
                        try {
                            // 출발지 주소 변환(표시용)
                            val geocoder = Geocoder(context, Locale.KOREA)
                            val addressList = geocoder.getFromLocation(start.latitude, start.longitude, 1)
                            val startAddress = addressList?.firstOrNull()?.getAddressLine(0) ?: "내 위치"

                            // ✅ 네이버 지도 앱
                            // 앱은 WGS84 위경도 사용
                            val appUrl = "nmap://route/public" +
                                    "?slat=${start.latitude}&slng=${start.longitude}" +
                                    "&sname=${Uri.encode(startAddress)}" +
                                    // 목적지는 검색 API 좌표가 TM128 이므로 WGS84로 이미 변환된 마커 좌표를 쓰는 게 이상적이나
                                    // 여기서는 웹 fallback 대비 이름 중심으로 처리 (앱이 자동보정)
                                    "&dlat=${destY / 1e7}&dlng=${destX / 1e7}" +
                                    "&dname=${Uri.encode(placeName)}&appname=com.myrythm"

                            Log.d("MapDebug", "App URL = $appUrl")

                            val appIntent = Intent(Intent.ACTION_VIEW, Uri.parse(appUrl))
                            appIntent.addCategory(Intent.CATEGORY_BROWSABLE)

                            try {
                                context.startActivity(appIntent) // 앱 시도
                            } catch (e: Exception) {
                                // ❌ 앱 없음 → 웹으로
                                Log.w("MapDebug", "네이버 지도 앱 없음, 웹으로 이동")

                                // ✅ 네이버 웹(빠른길찾기) - TM128 사용
                                val webUrl =
                                    "https://map.naver.com/p/directions/" +
                                            "${start.longitude},${start.latitude},${Uri.encode(startAddress)},0,FROM_COORD/" +
                                            "${destX},${destY},${Uri.encode(placeName)},0,TO_COORD/-/transit?c=16.00,0,0,0,dh"

                                Log.d("MapDebug", "Web URL = $webUrl")
                                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(webUrl)))
                            }
                        } catch (e: Exception) {
                            Log.e("MapDebug", "길찾기 처리 실패", e)
                            Toast.makeText(context, "길찾기를 시작할 수 없습니다.", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(context, "위치 정보를 가져올 수 없습니다.", Toast.LENGTH_SHORT).show()
                    }
                }
            ) {
                Text("길찾기")
            }

            Spacer(modifier = Modifier.width(8.dp))

            Button(onClick = onClose) {
                Text("닫기")
            }
        }
    }
}

/* -------------------- 공용 UI -------------------- */

@Composable
fun RoundRecenterButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier
            .size(56.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(50), // 원형
        color = Color.White,
        shadowElevation = 6.dp
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                painter = painterResource(R.drawable.location),
                contentDescription = "현재 위치",
                tint = Color(0xFF4A5565)
            )
        }
    }
}
