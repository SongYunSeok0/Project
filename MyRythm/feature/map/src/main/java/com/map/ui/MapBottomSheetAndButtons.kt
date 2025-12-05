package com.map.ui

import android.content.Intent
import android.location.Geocoder
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.map.data.PlaceItem
import com.naver.maps.geometry.LatLng
import com.shared.R
import com.shared.ui.components.AppTagButton
import java.util.Locale

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

    val phoneText = stringResource(R.string.phone)
    val navigationText = stringResource(R.string.navigation)
    val closeText = stringResource(R.string.close)
    val errorAddressNotFound = stringResource(R.string.map_error_address_not_found)
    val errorNavigationFailed = stringResource(R.string.map_error_navigation_failed)
    val errorLocationNotFound = stringResource(R.string.map_error_location_not_found)

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
                style = MaterialTheme.typography.titleLarge
            )
        }

        // 주소
        Text(text = place.address.ifBlank { errorAddressNotFound })

        // 전화(대부분 빈 값일 수 있음)
        place.telephone?.takeIf { it.isNotBlank() }?.let {
            Text(text = "$phoneText $it")
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
                    val destX = place.mapx.toDoubleOrNull() // TM128 X
                    val destY = place.mapy.toDoubleOrNull() // TM128 Y
                    val placeName = cleanTitle

                    if (start != null && destX != null && destY != null) {
                        try {
                            // 출발지 주소 변환(표시용)
                            val geocoder = Geocoder(context, Locale.KOREA)
                            val addressList =
                                geocoder.getFromLocation(start.latitude, start.longitude, 1)
                            val startAddress =
                                addressList?.firstOrNull()?.getAddressLine(0) ?: "내 위치"

                            // 네이버 지도 앱
                            val appUrl = "nmap://route/public" +
                                    "?slat=${start.latitude}&slng=${start.longitude}" +
                                    "&sname=${Uri.encode(startAddress)}" +
                                    "&dlat=${destY / 1e7}&dlng=${destX / 1e7}" +
                                    "&dname=${Uri.encode(placeName)}&appname=com.myrythm"

                            Log.d("MapDebug", "App URL = $appUrl")

                            val appIntent = Intent(Intent.ACTION_VIEW, Uri.parse(appUrl))
                            appIntent.addCategory(Intent.CATEGORY_BROWSABLE)

                            try {
                                context.startActivity(appIntent) // 앱 시도
                            } catch (e: Exception) {
                                // 앱 없음 → 웹
                                Log.w("MapDebug", "네이버 지도 앱 없음, 웹으로 이동")

                                val webUrl =
                                    "https://map.naver.com/p/directions/" +
                                            "${start.longitude},${start.latitude},${Uri.encode(startAddress)},0,FROM_COORD/" +
                                            "${destX},${destY},${Uri.encode(placeName)},0,TO_COORD/-/transit?c=16.00,0,0,0,dh"

                                Log.d("MapDebug", "Web URL = $webUrl")
                                context.startActivity(
                                    Intent(
                                        Intent.ACTION_VIEW,
                                        Uri.parse(webUrl)
                                    )
                                )
                            }
                        } catch (e: Exception) {
                            Log.e("MapDebug", "길찾기 처리 실패", e)
                            Toast.makeText(
                                context,
                                errorNavigationFailed,
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } else {
                        Toast.makeText(
                            context,
                            errorLocationNotFound,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            ) {
                Text(navigationText)
            }

            Spacer(modifier = Modifier.width(8.dp))

            Button(onClick = onClose) {
                Text(closeText)
            }
        }
    }
}

/* -------------------- 공용 UI -------------------- */

@Composable
fun SearchHereChip(
    visible: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val searchHereText = stringResource(R.string.search_here)

    if (!visible) return
    AppTagButton(
        label = searchHereText,
        onClick = onClick,
        modifier = modifier
            .shadow(4.dp, MaterialTheme.shapes.large)
            .wrapContentWidth()
            .height(36.dp),
        isCircle = false,
        alpha = 0.7f
    )
}

@Composable
fun RoundRecenterButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val myLocationText = stringResource(R.string.mylocation)

    Surface(
        modifier = modifier
            .size(56.dp)
            .clickable { onClick() },
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 6.dp
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                painter = painterResource(R.drawable.location),
                contentDescription = myLocationText
            )
        }
    }
}