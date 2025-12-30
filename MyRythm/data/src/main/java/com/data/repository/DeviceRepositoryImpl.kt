package com.data.repository

import com.data.network.api.DeviceApi
import com.data.network.dto.device.RegisterDeviceRequest
import com.data.network.dto.device.toDomain
import com.domain.model.ApiResult
import com.domain.model.Device
import com.domain.model.DomainError
import com.domain.repository.DeviceRepository
import javax.inject.Inject

class DeviceRepositoryImpl @Inject constructor(
    private val api: DeviceApi
) : DeviceRepository {

    override suspend fun registerDevice(
        uuid: String,
        token: String,
        name: String
    ): ApiResult<Unit> =
        try {
            api.registerDevice(
                RegisterDeviceRequest(
                    uuid = uuid,
                    token = token,
                    device_name = name
                )
            )
            ApiResult.Success(Unit)
        } catch (e: Exception) {
            ApiResult.Failure(
                DomainError.Network(
                    message = e.message ?: "디바이스 등록 실패"
                )
            )
        }

    override suspend fun getMyDevices(): ApiResult<List<Device>> =
        try {
            val response = api.getMyDevices()
            ApiResult.Success(response.map { it.toDomain() })
        } catch (e: Exception) {
            ApiResult.Failure(
                DomainError.Network(
                    message = e.message ?: "디바이스 목록 조회 실패"
                )
            )
        }
}
