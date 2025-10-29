package com.map

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.CameraPosition
import com.naver.maps.map.compose.*
import com.naver.maps.map.overlay.OverlayImage
import com.common.design.R

@OptIn(ExperimentalNaverMapApi::class, ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    modifier: Modifier = Modifier,
    onBack: (() -> Unit)? = null
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("지도", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
                navigationIcon = {
                    if (onBack != null) {
                        IconButton(onClick = onBack) {
                            Icon(
                                painter = painterResource(id = R.drawable.back),
                                contentDescription = "뒤로가기",
                                tint = Color.Black
                            )
                        }
                    }
                },
                actions = {
                    var selected by remember { mutableStateOf("병원") }
                    Row {
                        FilterChip(
                            selected = selected == "병원",
                            onClick = { selected = "병원" },
                            label = { Text("병원") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFF6AE0D9),
                                selectedLabelColor = Color.White
                            ),
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        FilterChip(
                            selected = selected == "약국",
                            onClick = { selected = "약국" },
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
                ),
                windowInsets = WindowInsets(0, 0, 0, 0)
            )
        },
        modifier = modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { inner ->
        Box(
            modifier = Modifier
                .padding(inner)
                .fillMaxSize()
        ) {
            // 지도
            val camera = rememberCameraPositionState {
                position = CameraPosition(LatLng(37.5666102, 126.9783881), 14.0)
            }
            NaverMap(
                modifier = Modifier.matchParentSize(),
                cameraPositionState = camera
            ) {
                Marker(
                    state = MarkerState(LatLng(37.5666102, 126.9783881)),
                    icon = OverlayImage.fromResource(R.drawable.pill),
                    width = 24.dp,   // 작게
                    height = 24.dp
                )

                Marker(
                    state = MarkerState(LatLng(37.5715, 126.9769)),
                    icon = OverlayImage.fromResource(R.drawable.pill),
                    width = 24.dp,
                    height = 24.dp
                )
            }

            // 지도 위 부유 UI
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .zIndex(1f)
            ) {
                CurrentLocationChip(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(16.dp)
                )
            }
        }
    }
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
    MaterialTheme { MapScreen(onBack = {}) }
}
