package com.map.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shared.R
import com.shared.ui.components.AppButton
import com.shared.ui.components.AppInputField
import com.shared.ui.components.AppTagButton

@Composable
fun MapSearchHeader(
    searchQuery: String,
    onValueChange: (String) -> Unit,
    onSearchAroundClick: () -> Unit,
    onModeChange: (String) -> Unit,
    selectedChip: String?,
    isLoading: Boolean = false,
    modifier: Modifier = Modifier
) {
    val searchText = stringResource(R.string.search)
    val hospitalText = stringResource(R.string.hospital)
    val pharmacyText = stringResource(R.string.pharmacy)
    val searchMessage = stringResource(R.string.map_message_search)

    Column(
        modifier = modifier
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .fillMaxWidth()
    ) {
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
                height = 56.dp
            )

            Spacer(modifier = Modifier.width(8.dp))

            AppButton(
                text = searchText,
                textStyle = MaterialTheme.typography.bodyLarge.copy(fontSize = 20.sp),
                shape = MaterialTheme.shapes.medium,
                onClick = onSearchAroundClick,
                enabled = !isLoading,
                backgroundColor = if (isLoading) {
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                } else {
                    MaterialTheme.colorScheme.primary
                },
                modifier = Modifier
                    .height(56.dp)
                    .widthIn(min = 80.dp)
            ) {
                if (isLoading) {
                    Box(
                        modifier = Modifier
                            .height(56.dp)
                            .widthIn(min = 80.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            AppTagButton(
                label = hospitalText,
                onClick = { if (!isLoading) onModeChange("병원") },
                selected = selectedChip == "병원",
                useFilterChipStyle = true,
            )

            AppTagButton(
                label = pharmacyText,
                selected = selectedChip == "약국",
                onClick = { if (!isLoading) onModeChange("약국") },
                useFilterChipStyle = true,
            )
        }
    }
}