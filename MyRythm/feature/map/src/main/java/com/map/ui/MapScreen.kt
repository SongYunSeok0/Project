package com.map.ui

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.CameraPosition
import com.naver.maps.map.CameraUpdate
import com.naver.maps.map.compose.ExperimentalNaverMapApi
import com.naver.maps.map.compose.MapProperties
import com.naver.maps.map.compose.MapUiSettings
import com.naver.maps.map.compose.Marker
import com.naver.maps.map.compose.MarkerState
import com.naver.maps.map.compose.NaverMap
import com.naver.maps.map.compose.rememberCameraPositionState
import com.naver.maps.map.compose.rememberFusedLocationSource
import com.naver.maps.map.overlay.OverlayImage
import com.shared.R
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import androidx.compose.runtime.snapshotFlow
import com.shared.ui.components.AppButton
import com.shared.ui.components.AppInputField
import com.shared.ui.components.AppTagButton
import com.shared.ui.theme.AppFieldHeight


@OptIn(ExperimentalNaverMapApi::class, ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    modifier: Modifier = Modifier,
    viewModel: MapViewModel = hiltViewModel()
) {
    val focusManager = LocalFocusManager.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val fusedLocationClient = remember {
        LocationServices.getFusedLocationProviderClient(context)
    }

    val cameraPositionState = rememberCameraPositionState()
    val locationSource = rememberFusedLocationSource()

    var hasLocationPermission by remember { mutableStateOf(false) }

    val uiState = viewModel.uiState

    // 문자열 리소스
    val searchText = stringResource(R.string.search)
    val hospitalText = stringResource(R.string.hospital)
    val pharmacyText = stringResource(R.string.pharmacy)
    val searchMessage = stringResource(R.string.map_message_search)

    /* ---------- 권한 런처 ---------- */

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

    /* ---------- 카메라 이동 감지 → ViewModel에 반영 ---------- */

    LaunchedEffect(cameraPositionState) {
        snapshotFlow { cameraPositionState.isMoving to cameraPositionState.position.target }
            .collectLatest { (moving, target) ->
                viewModel.onCameraMove(moving, target)
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

    LaunchedEffect(hasLocationPermission) {
        if (hasLocationPermission) {
            viewModel.enableLocationFollow()
            fetchCurrentLocation { here ->
                viewModel.updateMyLocation(here)
                cameraPositionState.move(
                    CameraUpdate.toCameraPosition(CameraPosition(here, 15.0))
                )
                if (uiState.places.isEmpty()) {
                    viewModel.searchAround(here)
                }
            }
        }
    }

    /* ---------- UI ---------- */

    Box(
        modifier = modifier.fillMaxSize()
    ) {
        // 지도
        NaverMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            uiSettings = MapUiSettings(isLocationButtonEnabled = false),
            locationSource = if (hasLocationPermission) locationSource else null,
            properties = MapProperties(locationTrackingMode = uiState.trackingMode),
            onMapClick = { _, _ -> viewModel.onBottomSheetDismiss() }
        ) {
            uiState.places.forEach { pw ->
                Marker(
                    state = MarkerState(position = pw.position),
                    icon = OverlayImage.fromResource(R.drawable.icon),
                    captionText = cleanHtml(pw.item.title),
                    onClick = {
                        viewModel.onMarkerSelected(pw)
                        cameraPositionState.move(CameraUpdate.scrollTo(pw.position))
                        true
                    }
                )
            }
        }

        // 상단 검색영역(검색창 + 토글칩)
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .zIndex(1f)
                .fillMaxWidth()
        ) {

            // 검색창 + 버튼 1203 디자인적용
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                AppInputField(
                    value = uiState.searchQuery,
                    onValueChange = viewModel::updateSearchQuery,
                    label = searchMessage,
                    singleLine = true,
                    modifier = Modifier
                        .weight(1f),
                    height = AppFieldHeight
                )

                Spacer(modifier = Modifier.width(8.dp))

                AppButton(
                    text = searchText,
                    textStyle = MaterialTheme.typography.labelLarge,
                    shape = MaterialTheme.shapes.medium,
                    onClick = {
                        focusManager.clearFocus(force = true)
                        val center = uiState.mapCenter ?: uiState.myLocation
                        viewModel.searchAround(center)
                    },
                    modifier = Modifier
                        .height(AppFieldHeight)
                        .widthIn(min = 90.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 병원/약국 토글 칩
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AppTagButton(
                    label = hospitalText,
                    onClick = { viewModel.onModeChange("병원") },
                    selected = uiState.selectedChip == "병원",
                    useFilterChipStyle = true
                )

                AppTagButton(
                    label = pharmacyText,
                    selected = uiState.selectedChip == "약국",
                    onClick = { viewModel.onModeChange("약국") },
                    useFilterChipStyle = true
                )
            }
        }

        // "이 위치에서 검색" 칩 - 디자인은 mapbottomsheet~파일에
        SearchHereChip(
            visible = uiState.showSearchHere &&
                    (uiState.mapCenter != null || uiState.myLocation != null),
            onClick = {
                focusManager.clearFocus(force = true)
                viewModel.onSearchHere()
            },
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 12.dp)
                .offset(y = 72.dp) // 검색창 아래로 살짝 내리기
        )

        // 하단 시트
        if (uiState.selected != null && uiState.showBottomSheet) {
            ModalBottomSheet(
                onDismissRequest = { viewModel.onBottomSheetDismiss() },
                sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
            ) {
                PlaceInfoContent(
                    place = uiState.selected.item,
                    onClose = { viewModel.onBottomSheetDismiss() },
                    myLocation = uiState.myLocation
                )
            }
        }

        // 내 위치 버튼
        RoundRecenterButton(
            onClick = {
                uiState.myLocation?.let {
                    cameraPositionState.move(CameraUpdate.scrollTo(it))
                    viewModel.onRecenter()
                }
            },
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(16.dp)
        )
    }
}
