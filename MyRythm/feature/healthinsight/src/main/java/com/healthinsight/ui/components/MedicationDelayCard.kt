package com.healthinsight.ui.components

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.domain.usecase.plan.MedicationDelayUI
import com.shared.R
import com.shared.ui.components.InsightCard

@Composable
fun MedicationDelayCard(medicationDelays: List<MedicationDelayUI>) {
    val noDataMessage = stringResource(R.string.healthinsight_message_no_data)
    val mediComplianceTitle = stringResource(R.string.medi_compliance_title)

    InsightCard (
        title = mediComplianceTitle,
        isEmpty = medicationDelays.isEmpty(),
        emptyMessage = noDataMessage
    ){
        val groupedByMed = medicationDelays.groupBy { it.label }

        groupedByMed.entries.forEachIndexed { index, (medName, delays) ->
            MedicationChart(
                medName = medName,
                delays = delays
            )

            if (index < groupedByMed.size - 1) {
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}
