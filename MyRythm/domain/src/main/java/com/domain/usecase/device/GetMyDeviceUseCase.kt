package com.domain.usecase.device

import com.domain.repository.DeviceRepository
import javax.inject.Inject

class GetMyDevicesUseCase @Inject constructor(
    private val repository: DeviceRepository
) {
    suspend operator fun invoke(): List<com.domain.model.Device> {
        return repository.getMyDevices()
    }
}