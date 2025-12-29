package com.map.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.map.ui.PlaceInfoContent
import com.map.ui.RoundRecenterButton
import com.map.ui.PlaceWithLatLng
import com.naver.maps.geometry.LatLng

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapBottomSheetSection(
    selected: PlaceWithLatLng?,
    showBottomSheet: Boolean,
    onBottomSheetDismiss: () -> Unit,
    myLocation: LatLng?,
    onRecenterClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // MapLogic.kt 내 데이터클래스 PlaceWithLatLng
    // 하단 시트
    if (selected != null && showBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = { onBottomSheetDismiss() },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ) {
            PlaceInfoContent(
                place = selected.item,
                onClose = { onBottomSheetDismiss() },
                myLocation = myLocation
            )
        }
    }
    // 내 위치 버튼
    RoundRecenterButton(
        onClick = onRecenterClick,
        modifier = modifier
            .padding(16.dp)
    )
}