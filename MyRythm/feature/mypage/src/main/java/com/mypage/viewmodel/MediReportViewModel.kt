package com.mypage.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.domain.model.MediRecord
import com.domain.usecase.mypage.GetMediRecordsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MediReportViewModel @Inject constructor(
    private val getMediRecordsUseCase: GetMediRecordsUseCase
) : ViewModel() {

    val records = MutableStateFlow<List<MediRecord>>(emptyList())

    init {
        loadRecords()
    }

    private fun loadRecords() {
        viewModelScope.launch {
            getMediRecordsUseCase().collect { list ->
                records.value = list
            }
        }
    }
}



