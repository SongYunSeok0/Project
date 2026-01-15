package com.scheduler.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.scheduler.ui.RegiController
import com.scheduler.ui.RegiTab
import com.shared.R
import com.shared.ui.components.AppSelectableButton
import com.shared.ui.theme.AppFieldHeight

@Composable
fun RegiDateTimeSection(controller: RegiController) {
    val dosePeriod = stringResource(R.string.dose_period)
    val startDateText = stringResource(R.string.start_date)
    val endDateText = stringResource(R.string.end_date)
    val mealRelationText = stringResource(R.string.meal_relation)
    val mealRelationBefore = stringResource(R.string.meal_relation_before)
    val mealRelationAfter = stringResource(R.string.meal_relation_after)
    val mealRelationIrrelevant = stringResource(R.string.meal_relation_irrelevant)

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {

        // 기간
        Column {
            Text(dosePeriod, color = MaterialTheme.colorScheme.onSurface)
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                DateBox(startDateText, controller.startDay, Modifier.weight(1f)) { controller.showStart = true }
                DateBox(endDateText, controller.endDay, Modifier.weight(1f)) { controller.showEnd = true }
            }
        }

        // 식사 관계
        if (controller.tab == RegiTab.DISEASE) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(mealRelationText, color = MaterialTheme.colorScheme.onSurface)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AppSelectableButton(
                        text = mealRelationBefore,
                        selected = controller.mealRelation == "before",
                        onClick = { controller.mealRelation = "before" },
                        modifier = Modifier.weight(1f),
                        height = 44.dp
                    )
                    AppSelectableButton(
                        text = mealRelationAfter,
                        selected = controller.mealRelation == "after",
                        onClick = { controller.mealRelation = "after" },
                        modifier = Modifier.weight(1f),
                        height = 44.dp
                    )
                    AppSelectableButton(
                        text = mealRelationIrrelevant,
                        selected = controller.mealRelation == "none",
                        onClick = { controller.mealRelation = "none" },
                        modifier = Modifier.weight(1f),
                        height = 44.dp
                    )
                }
            }
        }
    }
}

/* 날짜 박스 */
@Composable
private fun DateBox(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val selectText = stringResource(R.string.select)

    Column(modifier = modifier) {
        Spacer(Modifier.height(8.dp))
        Text(
            label,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.labelSmall,
        )
        Spacer(Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(AppFieldHeight)
                .clip(MaterialTheme.shapes.large)
                .background(MaterialTheme.colorScheme.primary.copy(0.1f))
                .clickable { onClick() },
            contentAlignment = Alignment.CenterStart
        ) {
            Text(
                text = value.ifBlank { selectText },
                color = if (value.isNotBlank()) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.outline,
                modifier = Modifier.padding(14.dp)
            )
        }
    }
}