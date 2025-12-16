package com.scheduler.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shared.R
import com.shared.ui.components.AppButton
import com.shared.ui.theme.AppTheme
import kotlinx.coroutines.launch

private val ITEM_HEIGHT = 50.dp          // 한 줄 높이
private const val VISIBLE_COUNT = 5      // 위 2줄 + 중앙 1줄 + 아래 2줄

@Composable
fun WheelTimePickerDialog(
    hour: Int,
    minute: Int,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var selectedHour by remember { mutableStateOf(hour) }
    var selectedMinute by remember { mutableStateOf(minute) }

    val confirmText = stringResource(R.string.confirm)
    val cancelText = stringResource(R.string.cancel)
    val colonText = stringResource(R.string.time_colon)


    AppTheme {
        AlertDialog(
            onDismissRequest = onDismiss,
            text = {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 30.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    WheelSelector(
                        range = (0..23).toList(),
                        initial = hour,
                        onSelect = { selectedHour = it }
                    )

                    Text(
                        text = colonText,
                        fontSize = 35.sp,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )

                    WheelSelector(
                        range = (0..59).toList(),
                        initial = minute,
                        onSelect = { selectedMinute = it }
                    )
                }
            },
            confirmButton = {
                AppButton(
                    text = confirmText,
                    height = 40.dp,
                    width = 70.dp,
                    onClick = {
                        onConfirm(
                            String.format(
                                "%02d:%02d",
                                selectedHour,
                                selectedMinute
                            )
                        )
                    }
                )
            },
            dismissButton = {
                AppButton(
                    text = cancelText,
                    height = 40.dp,
                    width = 70.dp,
                    backgroundColor = MaterialTheme.colorScheme.surfaceVariant,
                    textColor = MaterialTheme.colorScheme.onSurface,
                    onClick = onDismiss
                )
            }
        )
    }
}

@Composable
private fun WheelSelector(
    range: List<Int>,
    initial: Int,
    onSelect: (Int) -> Unit
) {
    val itemCount = range.size
    val loopCount = 100
    val bigList = remember { List(itemCount * loopCount) { range[it % itemCount] } }

    val middleIndex = itemCount * loopCount / 2
    val startIndex = middleIndex - (middleIndex % itemCount) + initial

    val listState = rememberLazyListState(startIndex)
    val flingBehavior = rememberSnapFlingBehavior(listState)

    // ⭐ 실제 중앙 = 첫 번째 보이는 아이템 + 2
    val centerIndex by remember {
        derivedStateOf { listState.firstVisibleItemIndex + 1 }
    }

    // ⭐ 중앙 아이템을 선택값으로 기록
    LaunchedEffect(centerIndex) {
        val realIndex = (centerIndex % itemCount + itemCount) % itemCount
        onSelect(range[realIndex])
    }

    Box(
        modifier = Modifier
            .height(ITEM_HEIGHT * VISIBLE_COUNT)
            .width(90.dp),
        contentAlignment = Alignment.Center
    ) {

        LazyColumn(
            state = listState,
            flingBehavior = flingBehavior,
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item { Spacer(Modifier.height(ITEM_HEIGHT)) }

            itemsIndexed(bigList) { index, item ->
                val scope = rememberCoroutineScope()

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(ITEM_HEIGHT)
                        .clickable { scope.launch { scrollToCenter(listState, index) } },
                    contentAlignment = Alignment.Center
                ) {
                    val isSelected = index == centerIndex

                    Text(
                        text = "%02d".format(item),
                        fontSize = if (isSelected) 35.sp else 26.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                    )
                }
            }

            item { Spacer(Modifier.height(ITEM_HEIGHT)) }
        }
    }
}

suspend fun scrollToCenter(
    state: LazyListState,
    index: Int,
    visibleCount: Int = VISIBLE_COUNT
) {
    val target = index - visibleCount / 2
    state.animateScrollToItem(target.coerceAtLeast(0))
}