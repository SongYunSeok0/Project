package com.scheduler.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.scheduler.ui.RegiController
import com.scheduler.ui.RegiTab
import com.shared.R
import com.shared.ui.components.AppInputField

@Composable
fun RegiHeaderSection(controller: RegiController) {
    val diseaseText = stringResource(R.string.disease)
    val supplementText = stringResource(R.string.supplement)
    val diseaseNameText = stringResource(R.string.disease_name)
    val supplementNameText = stringResource(R.string.supplement_name)
    val enterDiseaseNameMessage = stringResource(R.string.scheduler_message_disease_name)
    val enterSupplementNameMessage = stringResource(R.string.scheduler_message_supplement_name)

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        TabRow(
            selectedTabIndex = if (controller.tab == RegiTab.DISEASE) 0 else 1,
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            indicator = { pos ->
                TabRowDefaults.SecondaryIndicator(
                    Modifier.tabIndicatorOffset(pos[if (controller.tab == RegiTab.DISEASE) 0 else 1]),
                    height = 3.dp,
                    color = MaterialTheme.colorScheme.primary
                )
            },
            divider = {}
        ) {
            Tab(
                selected = controller.tab == RegiTab.DISEASE,
                onClick = { controller.tab = RegiTab.DISEASE },
                text = { Text(diseaseText) }
            )
            Tab(
                selected = controller.tab == RegiTab.SUPPLEMENT,
                onClick = { controller.tab = RegiTab.SUPPLEMENT },
                text = { Text(supplementText) }
            )
        }

        // 병명 / 영양제
        if (controller.tab == RegiTab.DISEASE) {
            Column {
                Text(diseaseNameText, color = MaterialTheme.colorScheme.onSurface)
                Spacer(Modifier.height(8.dp))
                AppInputField(
                    value = controller.disease,
                    onValueChange = { controller.disease = it },
                    label = enterDiseaseNameMessage,
                    singleLine = true,
                )
            }
        } else {
            Column {
                Text(supplementNameText, color = MaterialTheme.colorScheme.onSurface)
                Spacer(Modifier.height(8.dp))
                AppInputField(
                    value = controller.supplement,
                    onValueChange = { controller.supplement = it },
                    label = enterSupplementNameMessage,
                    singleLine = true,
                )
            }
        }
    }
}