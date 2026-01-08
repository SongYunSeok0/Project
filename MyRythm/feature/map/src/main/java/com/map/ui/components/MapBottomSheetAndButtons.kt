package com.map.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.map.ui.PlaceWithLatLng
import com.map.ui.cleanCategoryForDisplay
import com.naver.maps.geometry.LatLng
import com.shared.R
import com.shared.ui.theme.*

/* -------------------- 하단 시트 컨텐츠 -------------------- */

@Composable
fun PlaceInfoContent(
    place: PlaceWithLatLng,
    onClose: () -> Unit,
    myLocation: LatLng?,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    val cleanTitle = place.title
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
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = cleanTitle,
                style = MaterialTheme.typography.titleLarge,
                color = MapBottomSheetText,
                modifier = Modifier.weight(1f, fill = false)
            )

            // 카테고리 칩 (오른쪽)
            if (prettyCategory.isNotBlank()) {
                Spacer(modifier = Modifier.width(8.dp))
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = MapListCategoryChip
                ) {
                    Text(
                        text = prettyCategory,
                        style = MaterialTheme.typography.labelSmall,
                        color = MapListCategoryText,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 주소
        Text(
            text = place.address.ifBlank { errorAddressNotFound },
            style = MaterialTheme.typography.bodyMedium,
            color = MapBottomSheetSubText
        )

        // 전화
        place.telephone?.takeIf { it.isNotBlank() }?.let {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "$phoneText $it",
                style = MaterialTheme.typography.bodyMedium,
                color = MapBottomSheetSubText
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            // 길찾기 버튼
            Button(
                onClick = { },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MapBottomSheetButton,
                    contentColor = MapBottomSheetButtonText
                )
            ) {
                Text(navigationText)
            }

            Spacer(modifier = Modifier.width(8.dp))

            Button(
                onClick = onClose,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MapBottomSheetButton,
                    contentColor = MapBottomSheetButtonText
                )
            ) {
                Text(closeText)
            }
        }
    }
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
        color = MapBottomSheetBackground,
        shadowElevation = 6.dp
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                painter = painterResource(R.drawable.location),
                contentDescription = myLocationText,
                tint = MapListIconTint
            )
        }
    }
}