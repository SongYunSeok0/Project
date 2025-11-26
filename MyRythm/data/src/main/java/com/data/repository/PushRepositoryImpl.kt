package com.data.repository

import android.util.Log
import com.data.network.api.UserApi
import com.data.network.dto.user.FcmTokenRequestDto
import com.domain.repository.PushRepository
import javax.inject.Inject

class PushRepositorylmpl @Inject constructor(
    private val api: UserApi,
): PushRepository {


    override suspend fun registerFcmToken(token: String) {
        try {
            api.registerFcmToken(FcmTokenRequestDto(fcm_token = token))
        } catch (e: Exception) {
            Log.e("FCM", "registerFcmToken 실패", e)
        }
    }
}