package com.example.map

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
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


// --- NaverMap 관련 API는 실험적 기능이므로 OptIn 어노테이션이 필요합니다. ---
@OptIn(ExperimentalNaverMapApi::class)
@Composable
fun MapScreen(modifier: Modifier = Modifier) {
    // Scaffold를 사용해 상단바와 본문을 분리합니다.
    Scaffold(
        topBar = { MapTopAppBar() }, // 상단바 UI
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        // --- 지도와 지도 위의 UI 요소들을 담는 컨테이너 ---
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            // --- 1. 실제 네이버 지도 ---
            NaverMap(
                modifier = Modifier.matchParentSize(), // Box를 꽉 채웁니다.
                // 초기 카메라 위치 설정 (예시: 서울 시청)
                cameraPositionState = rememberCameraPositionState {
                    position = CameraPosition(LatLng(37.5666102, 126.9783881), 14.0)
                }
            ) {
                // --- 2. 지도 위에 표시될 마커들 ---
                // TODO: 이 부분은 나중에 서버에서 받아온 데이터 목록으로 반복문을 돌려 생성해야 합니다.
                // 예시 마커 1: 서울 시청
                Marker(
                    state = MarkerState(position = LatLng(37.5666102, 126.9783881)),
                    icon = OverlayImage.fromResource(R.drawable.icon),
                    onClick = {
                        // TODO: 마커 클릭 시 동작 (정보창 띄우기, BottomSheet 상태 변경 등)
                        println("마커 1 클릭됨")
                        true
                    }
                )

                // 예시 마커 2: 광화문
                Marker(
                    state = MarkerState(position = LatLng(37.5715, 126.9769)),
                    icon = OverlayImage.fromResource(R.drawable.icon),
                    onClick = {
                        println("마커 2 클릭됨")
                        true
                    }
                )
            }

            // --- 3. 지도 위에 떠 있어야 하는 UI (칩 등) ---
            // zIndex를 사용해 지도(기본 zIndex=0f)보다 위에 표시되도록 합니다.
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .zIndex(1f) // zIndex가 높을수록 더 위에 보입니다.
            ) {
                CurrentLocationChip(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(16.dp)
                )
                // TODO: 나중에 BottomSheet을 구현하면 이 Box 안에 함께 배치됩니다.
            }
        }
    }
}

// --- 상단바 UI ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapTopAppBar() {
    CenterAlignedTopAppBar(
        title = {
            Text(
                "지도",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        },
        // 뒤로가기 버튼. 아이콘이 없다면 이 부분을 주석 처리하세요.
        navigationIcon = {
            // IconButton(onClick = { /* TODO: 뒤로가기 동작 */ }) {
            //     Icon(
            //         painter = painterResource(id = R.drawable.ic_arrow_back), // 이 아이콘이 drawable에 있어야 함
            //         contentDescription = "뒤로가기"
            //     )
            // }
        },
        // 병원, 약국 선택 칩
        actions = {
            var selectedChip by remember { mutableStateOf("병원") }
            Row {
                FilterChip(
                    selected = selectedChip == "병원",
                    onClick = { selectedChip = "병원" },
                    label = { Text("병원") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFF6AE0D9),
                        selectedLabelColor = Color.White
                    ),
                    modifier = Modifier.padding(end = 8.dp)
                )
                FilterChip(
                    selected = selectedChip == "약국",
                    onClick = { selectedChip = "약국" },
                    label = { Text("약국") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFF6AE0D9),
                        selectedLabelColor = Color.White
                    )
                )
            }
        },
        // 상단바 배경색
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = Color(0xFFB5E5E1).copy(alpha = 0.36f)
        )
    )
}

// --- "현재 위치 기준" 칩 UI ---
@Composable
fun CurrentLocationChip(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(50), // 둥근 모서리
        color = Color.White,
        shadowElevation = 4.dp // 그림자 효과
    ) {
        Text(
            text = "현재 위치 기준",
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            fontSize = 14.sp,
            color = Color(0xFF4A5565)
        )
    }
}

// --- 지도 위의 약국 정보 아이템 UI (마커 모양을 커스텀할 때 사용 가능) ---
@Composable
fun MapPharmacyItem(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 위치 아이콘
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(50))
                .background(Color.White),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = R.drawable.icon),
                contentDescription = "위치 아이콘",
                modifier = Modifier.size(32.dp),
                tint = Color(0xFF6AE0D9)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        // 약국 정보 컨테이너
        Surface(
            shape = RoundedCornerShape(50),
            color = Color.White,
            shadowElevation = 4.dp
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "ㅇㅇㅇ 병원", // 실제로는 파라미터로 받아야 함
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF3B566E)
                )
                Text(
                    text = "내기준 111m", // 실제로는 파라미터로 받아야 함
                    fontSize = 12.sp,
                    color = Color(0xFF6F8BA4)
                )
            }
        }
    }
}

// --- 미리보기 ---
@Preview(showBackground = true)
@Composable
private fun MapScreenPreview() {
    // 미리보기에서는 네이버 지도가 보이지 않을 수 있습니다.
    // 이는 정상이며, 실제 에뮬레이터에서 확인해야 합니다.
    MaterialTheme { // 실제 프로젝트의 테마 이름(MyRythmTheme)으로 변경하면 더 좋습니다.
        MapScreen()
    }
}
