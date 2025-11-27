package com.domain.repository

import com.domain.model.MediRecord
import kotlinx.coroutines.flow.Flow

interface MediRecordRepository {
    fun getRecords(): Flow<List<MediRecord>>

}

