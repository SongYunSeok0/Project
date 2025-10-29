package com.example.map

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.core.content.ContextCompat
import com.example.map.data.NaverSearchApi
import com.example.map.data.PlaceItem
import com.google.android.gms.location.LocationServices
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.CameraPosition
import com.naver.maps.map.CameraUpdate
import com.naver.maps.map.compose.*
import com.naver.maps.map.overlay.OverlayImage
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

@OptIn(ExperimentalNaverMapApi::class)
@Composable
fun MapScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition(LatLng(37.5666102, 126.9783881), 14.0)
    }
    val coroutineScope = rememberCoroutineScope()

    var myLocation by remember { mutableStateOf<LatLng?>(null) }
    var hasLocationPermission by remember {
        mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
    }
    var places by remember { mutableStateOf<List<PlaceItem>>(emptyList()) }
    var selectedPlace by remember { mutableStateOf<PlaceItem?>(null) }

    val naverSearchApi = remember {
        Retrofit.Builder()
            .baseUrl("https://openapi.naver.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(NaverSearchApi::class.java)
    }

    fun searchPlaces(query: String, coordinate: LatLng?) {
        if (coordinate == null) {
            Log.w("MapScreen", "좌표가 없어 검색을 실행할 수 없습니다.")
            return
        }

        coroutineScope.launch {
            try {
                val coordinateString = "${coordinate.longitude},${coordinate.latitude}"
                val result = naverSearchApi.searchPlaces(
                    clientId = "ff1FDMV_KytGQEHXntal",
                    clientSecret = "k3Jxk1Of5l",
                    query = query,
                    display = 15,
                    coordinate = coordinateString
                )
                places = result.items
                Log.d("MapScreen-Content", "검색 결과: $places")

            } catch (e: Exception) {
                places = emptyList()
                Log.e("MapScreen", "API 검색 실패", e)
            }
        }
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            hasLocationPermission = isGranted
            if (!isGranted) {
                Log.d("MapScreen", "위치 권한이 거부되었습니다.")
            }
        }
    )

    LaunchedEffect(Unit) {
        if (!hasLocationPermission) {
            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    @SuppressLint("MissingPermission")
    LaunchedEffect(hasLocationPermission) {
        if (hasLocationPermission) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                location?.let {
                    val currentLatLng = LatLng(it.latitude, it.longitude)
                    myLocation = currentLatLng
                    cameraPositionState.move(CameraUpdate.toCameraPosition(CameraPosition(currentLatLng, 15.0)))
                    if (places.isEmpty()) {
                        searchPlaces("병원", currentLatLng)
                    }
                }
            }
        }
    }

    Scaffold(
        topBar = {
            MapTopAppBar(
                onChipSelected = { type ->
                    selectedPlace = null
                    searchPlaces(type, myLocation)
                }
            )
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
            NaverMap(
                modifier = Modifier.matchParentSize(),
                cameraPositionState = cameraPositionState,
                uiSettings = MapUiSettings(isLocationButtonEnabled = hasLocationPermission),
                onMapClick = { _, _ ->
                    selectedPlace = null
                }
            ) {
                myLocation?.let { location ->
                    Marker(
                        state = MarkerState(position = location),
                        icon = OverlayImage.fromResource(R.drawable.icon),
                        captionText = "내 위치",
                        zIndex = 10
                    )
                }

                places.forEach { place ->
                    val position: LatLng? = try {
                        // mapx, mapy가 10^7 배수로 되어 있으므로 나누기 필요
                        LatLng(place.mapy.toDouble() / 1e7, place.mapx.toDouble() / 1e7)
                    } catch (e: Exception) {
                        Log.e("MapScreen", "좌표 변환 실패: ${place.title}", e)
                        null
                    }

                    if (position != null) {
                        Marker(
                            state = MarkerState(position = position),
                            icon = OverlayImage.fromResource(R.drawable.icon),
                            captionText = place.title.replace(Regex("<.*?>"), ""),
                            onClick = {
                                selectedPlace = place
                                cameraPositionState.move(CameraUpdate.scrollTo(position))
                                true
                            }
                        )
                    }
                }

            }

            CurrentLocationChip(modifier = Modifier.align(Alignment.TopStart).padding(16.dp))

            selectedPlace?.let { place ->
                PlaceInfoSheet(
                    place = place,
                    modifier = Modifier.align(Alignment.BottomCenter),
                    onClose = { selectedPlace = null }
                )
            }
        }
    }
}


// --- 이하 다른 Composable 함수들은 기존과 동일 ---

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
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "닫기",
                    modifier = Modifier
                        .size(24.dp)
                        .clickable { onClose() }
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = place.address,
                fontSize = 14.sp,
                color = Color.Gray
            )
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapTopAppBar(onChipSelected: (String) -> Unit) {
    var selectedChip by remember { mutableStateOf("병원") }

    CenterAlignedTopAppBar(
        title = { Text("지도", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
        actions = {
            Row(modifier = Modifier.padding(end = 8.dp)) {
                FilterChip(
                    selected = selectedChip == "병원",
                    onClick = {
                        if (selectedChip != "병원") {
                            selectedChip = "병원"
                            onChipSelected("병원")
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
                            selectedChip = "약국"
                            onChipSelected("약국")
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

@Preview(showBackground = true)
@Composable
private fun MapScreenPreview() {
    MaterialTheme {
        MapScreen()
    }
}
