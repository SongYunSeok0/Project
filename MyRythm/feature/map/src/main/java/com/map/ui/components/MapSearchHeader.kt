package com.map.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.shared.R
import com.shared.ui.components.AppButton
import com.shared.ui.components.AppInputField
import com.shared.ui.components.AppTagButton
import com.shared.ui.theme.AppFieldHeight

@Composable
fun MapSearchHeader(
    searchQuery: String,
    onValueChange: (String) -> Unit,
    onSearchAroundClick: () -> Unit,
    onModeChange: (String) -> Unit,
    selectedChip: String?,
    modifier: Modifier = Modifier
) {
    val searchText = stringResource(R.string.search)
    val hospitalText = stringResource(R.string.hospital)
    val pharmacyText = stringResource(R.string.pharmacy)
    val searchMessage = stringResource(R.string.map_message_search)

    // 상단 검색영역(검색창 + 토글칩)
    Column(
        modifier = modifier
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .fillMaxWidth()
    ) {
        // 검색창 + 버튼 1203 디자인적용
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            AppInputField(
                value = searchQuery,
                onValueChange = onValueChange,
                label = searchMessage,
                singleLine = true,
                modifier = Modifier.weight(1f),
                height = AppFieldHeight
            )

            Spacer(modifier = Modifier.width(8.dp))

            AppButton(
                text = searchText,
                textStyle = MaterialTheme.typography.labelLarge,
                shape = MaterialTheme.shapes.medium,
                onClick = onSearchAroundClick,
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
                onClick = { onModeChange("병원") },
                selected = selectedChip == "병원",
                useFilterChipStyle = true
            )

            AppTagButton(
                label = pharmacyText,
                selected = selectedChip == "약국",
                onClick = { onModeChange("약국") },
                useFilterChipStyle = true
            )
        }
    }
}