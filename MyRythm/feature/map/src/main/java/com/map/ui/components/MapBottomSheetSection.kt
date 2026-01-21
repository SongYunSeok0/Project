package com.map.ui.components

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.map.ui.PlaceWithLatLng
import com.naver.maps.geometry.LatLng
import com.shared.ui.theme.MapBottomSheetBackground

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
    // 하단 시트
    if (selected != null && showBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = { onBottomSheetDismiss() },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = MapBottomSheetBackground
        ) {
            PlaceInfoContent(
                place = selected,
                onClose = { onBottomSheetDismiss() },
                myLocation = myLocation
            )
        }
    }
}