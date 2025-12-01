package com.mypage.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.domain.model.MediRecord
import com.domain.usecase.mypage.GetMediRecordsUseCase
import com.domain.usecase.plan.DeletePlanUseCase
import com.mypage.ui.GroupedMediRecord
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MediReportViewModel @Inject constructor(
    private val getMediRecordsUseCase: GetMediRecordsUseCase,
    private val deletePlanUseCase: DeletePlanUseCase
) : ViewModel() {

    private val _records = MutableStateFlow<List<MediRecord>>(emptyList())
    val records = _records.asStateFlow()

    init {
        loadRecords()
    }

    private fun loadRecords() {
        viewModelScope.launch {
            getMediRecordsUseCase().collect { list ->
                _records.value = list
            }
        }
    }

    fun deleteRecordGroup(userId: Long, group: GroupedMediRecord) {
        viewModelScope.launch {
            // 그룹의 모든 Plan 삭제
            group.records.forEach { record ->
                deletePlanUseCase(userId = userId, planId = record.id)
            }
        }
    }
}