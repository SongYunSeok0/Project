package com.map.data

data class PlaceItem(
    val title: String,           // 예: "<b>서울병원</b>"
    val link: String?,           // 장소 상세 링크
    val category: String?,       // 예: 병원 > 내과
    val description: String?,    // 설명 (있을 수도 있음)
    val telephone: String?,      // 전화번호
    val address: String,         // 도로명 주소
    val roadAddress: String?,    // 구주소
    val mapx: String,            // x좌표 (경도)
    val mapy: String             // y좌표 (위도)
)
