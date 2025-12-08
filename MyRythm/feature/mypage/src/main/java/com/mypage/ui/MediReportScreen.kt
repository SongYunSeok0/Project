package com.mypage.ui

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.mypage.viewmodel.MediReportViewModel
import com.shared.R

@Composable
fun MediReportScreen(
    userId: Long,
    viewModel: MediReportViewModel = hiltViewModel()
) {
    val doseRecordText = stringResource(R.string.dose_record)

    val records by viewModel.records.collectAsState()

    val groupedRecords = remember(records) {
        groupMediRecords(records)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 16.dp)
    ) {
        Text(
            text = doseRecordText,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(groupedRecords) { group ->
                GroupedMediRecordCard(
                    group = group,
                    onDeleteGroup = { deletedGroup ->
                        viewModel.deleteRecordGroup(userId, deletedGroup)
                    }
                )
            }
        }
    }
}