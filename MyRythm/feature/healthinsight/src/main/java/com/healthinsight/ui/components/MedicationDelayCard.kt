package com.healthinsight.ui.components

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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

    InsightCard {
        Text(
            text = mediComplianceTitle,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (medicationDelays.isEmpty()) {
            Text(
                text = noDataMessage,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(vertical = 32.dp)
            )
        } else {
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
}
