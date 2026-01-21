package com.data.repository

import android.util.Log
import com.data.core.push.FcmTokenStore
import com.data.core.push.PushManager
import com.data.network.api.UserApi
import com.data.network.dto.user.FcmTokenRequestDto
import com.domain.model.ApiResult
import com.domain.model.DomainError
import com.domain.repository.PushRepository
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.suspendCancellableCoroutine
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject
import kotlin.coroutines.resume

class PushRepositoryImpl @Inject constructor(
    private val api: UserApi,
    private val fcmTokenStore: FcmTokenStore
): PushRepository {

    override suspend fun fetchAndSaveFcmToken(): String? {
        val localToken = fcmTokenStore.getToken()
        if (!localToken.isNullOrBlank()) {
            PushManager.fcmToken = localToken
            return localToken
        }

        return suspendCancellableCoroutine { cont ->
            FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val newToken = task.result
                    Log.i("FCM", "Firebase token fetched: $newToken")
                    fcmTokenStore.saveToken(newToken)
                    PushManager.fcmToken = newToken
                    cont.resume(newToken)
                } else {
                    Log.w("FCM", "Fetching FCM token failed", task.exception)
                    cont.resume(null)
                }
            }
        }
    }

    override suspend fun registerFcmToken(token: String): ApiResult<Unit> {
        return try {
            val response = api.registerFcmToken(FcmTokenRequestDto(fcm_token = token))

            if (response.isSuccessful) {
                Log.d("FCM", "FCM 토큰 서버 등록 성공")
                ApiResult.Success(Unit)
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e("FCM", "FCM 토큰 등록 실패: code=${response.code()}, body=$errorBody")
                ApiResult.Failure(DomainError.Server(code = response.code(), msg = errorBody))
            }
        } catch (e: HttpException) {
            Log.e("FCM", "HTTP 오류: ${e.code()}", e)
            ApiResult.Failure(DomainError.Server(code = e.code(), msg = e.message()))
        } catch (e: IOException) {
            Log.e("FCM", "네트워크 오류", e)
            ApiResult.Failure(DomainError.Network(msg = e.message))
        } catch (e: Exception) {
            Log.e("FCM", "알 수 없는 오류", e)
            ApiResult.Failure(DomainError.Unknown(msg = e.message ?: "알 수 없는 오류"))
        }
    }
}