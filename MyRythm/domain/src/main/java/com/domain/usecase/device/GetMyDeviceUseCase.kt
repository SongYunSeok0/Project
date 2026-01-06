package com.domain.usecase.device

import com.domain.model.ApiResult
import com.domain.model.Device
import com.domain.repository.DeviceRepository
import javax.inject.Inject

class GetMyDevicesUseCase @Inject constructor(
    private val repository: DeviceRepository
) {
    suspend operator fun invoke(): ApiResult<List<Device>> {
        return repository.getMyDevices()
    }
}
