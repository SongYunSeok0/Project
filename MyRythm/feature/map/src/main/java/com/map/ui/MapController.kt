package com.map.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.CameraPosition
import com.naver.maps.map.CameraUpdate
import com.naver.maps.map.LocationTrackingMode
import com.naver.maps.map.NaverMap
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.overlay.OverlayImage
import com.shared.R

/*
 - NaverMap 인스턴스 관리
 - 마커 생성/삭제/업데이트
 - 카메라 이동 제어
 - 지도 이벤트 리스너 등록

 기존 MapScreen에서 분리된 부분
 - var naverMap by remember { mutableStateOf<NaverMap?>(null) }
 - val markers = remember { mutableListOf<Marker>() }
 - LaunchedEffect(uiState.places, naverMap) { 마커 업데이트 로직 }
 등등 네이버지도 부분을 맵컨트롤러로 분리, 위치와 마커 로직 맵컨트롤러로 이동.

 변경 부분은 MapScreen.kt의 //0115 주석 검색해서 참고할 것.
 */

// 기존 var naverMap by remember { mutableStateOf<NaverMap?>(null) }
// -> 옵저버 필요 없이 private var naverMapInstance: NaverMap? = null 프로퍼티화
class MapController {
    private var naverMapInstance: NaverMap? = null
    private val markers = mutableListOf<Marker>()

    // NaverMap 인스턴스 설정
    fun setNaverMap(map: NaverMap) {
        naverMapInstance = map
    }

    //NaverMap 인스턴스 가져오기
    fun getNaverMap(): NaverMap? = naverMapInstance

    /* ---------- 마커 업데이트 ---------- */
    // 기존 마커 & 새 마커 추가 부분은 맵컨트롤러로 이동
    fun updateMarkers(
        places: List<PlaceWithLatLng>,
        onMarkerClick: (PlaceWithLatLng) -> Unit
    ) {
        val map = naverMapInstance ?: return

        // 기존 마커 제거
        markers.forEach { it.map = null }
        markers.clear()

        // 새 마커 추가
        // 기존 uiState.places.forEach { pw ->
        // 변경         places.forEach { place ->
        places.forEach { place ->
            val marker = Marker().apply {
                position = place.position
                icon = OverlayImage.fromResource(R.drawable.icon)
                captionText = place.title
                setOnClickListener {
                    // 기존 뷰모델 의존 -> 변경 뷰모델 역할 분리, MapScreen.kt 참고
                    onMarkerClick(place)
                    moveCamera(place.position)
                    true
                }
                this.map = map
            }
            markers.add(marker)
        }
    }

    // 카메라를 특정 위치로 이동 (스크롤)
    fun moveCamera(latLng: LatLng) {
        naverMapInstance?.moveCamera(CameraUpdate.scrollTo(latLng))
    }

    /* ---------- 권한 허용 후 초기 위치/검색 ---------- */
    // 카메라를 특정 위치로 줌과 함께 이동
    fun moveCameraWithZoom(latLng: LatLng, zoom: Double = 15.0) {
        naverMapInstance?.moveCamera(
            CameraUpdate.toCameraPosition(
                CameraPosition(latLng, zoom)
            )
        )
    }

    /* ---------- 위치 추적 모드 업데이트 ---------- */
    fun setLocationTrackingMode(mode: LocationTrackingMode) {
        naverMapInstance?.locationTrackingMode = mode
    }

    /* ---------- 카메라 이동 감지 ---------- */
    // 기존 코드 이동
    fun setOnCameraChangeListener(listener: (reason: Int, animated: Boolean, target: LatLng?) -> Unit) {
        naverMapInstance?.addOnCameraChangeListener { reason, animated ->
            val target = naverMapInstance?.cameraPosition?.target
            listener(reason, animated, target)
        }
    }

    // 지도 클릭 리스너 등록
    fun setOnMapClickListener(listener: () -> Unit) {
        naverMapInstance?.setOnMapClickListener { _, _ ->
            listener()
        }
    }


    // 0115 메모리 누수 방지 및 네이버지도 마커 제거(정리)용도 추가.
    fun cleanup() {
        markers.forEach { it.map = null }
        markers.clear()
    }
}

// MapController를 remember로 생성하는 헬퍼 함수
@Composable
fun rememberMapController(): MapController {
    return remember { MapController() }
}