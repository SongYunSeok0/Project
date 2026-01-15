// map/src/main/java/com/map/ui/components/PlaceListSection.kt
package com.map.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border  // ✅ 추가
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.map.ui.PlaceWithLatLng
import com.shared.ui.theme.*

@Composable
fun PlaceListSection(
    places: List<PlaceWithLatLng>,
    onPlaceClick: (PlaceWithLatLng) -> Unit,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(false) }

    if (places.isEmpty()) return

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MapListBackground)
    ) {
        // 토글 헤더
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { isExpanded = !isExpanded }
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "검색 결과 ${places.size}개",
                style = MaterialTheme.typography.titleMedium,
                color = MapListItemText
            )
            Icon(
                imageVector = if (isExpanded) Icons.Default.KeyboardArrowDown else Icons.Default.KeyboardArrowUp,
                contentDescription = if (isExpanded) "접기" else "펼치기",
                tint = MapListIconTint
            )
        }

        // 리스트
        if (isExpanded) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 300.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(places) { place ->
                    PlaceListItem(
                        place = place,
                        onClick = { onPlaceClick(place) }
                    )
                }
            }
        }
    }
}

@Composable
private fun PlaceListItem(
    place: PlaceWithLatLng,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(  // ✅ 테두리 추가
                width = 1.dp,
                color = MapListItemBorder,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MapListItemCard
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // 제목
            Text(
                text = place.title,
                style = MaterialTheme.typography.titleSmall,
                color = MapListItemText,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            // 주소
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Place,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MapListItemSubText
                )
                Text(
                    text = place.address,
                    style = MaterialTheme.typography.bodySmall,
                    color = MapListItemSubText,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // 카테고리
            place.category?.let { category ->
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = MapListCategoryChip  // ✅ 연한 회색
                ) {
                    Text(
                        text = category,
                        style = MaterialTheme.typography.labelSmall,
                        color = MapListCategoryText,  // ✅ 회색 텍스트
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}