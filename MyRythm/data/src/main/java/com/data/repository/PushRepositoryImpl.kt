package com.data.repository

import android.util.Log
import com.data.core.push.FcmTokenStore
import com.data.core.push.PushManager
import com.data.network.api.UserApi
import com.data.network.dto.user.FcmTokenRequestDto
import com.domain.repository.PushRepository
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import kotlin.coroutines.resume

class PushRepositoryImpl @Inject constructor(
    private val api: UserApi,
    private val fcmTokenStore: FcmTokenStore // ✅ FcmTokenStore 주입 추가
): PushRepository {

    // 1. 토큰 가져오기 (로컬 캐시 확인 -> 없으면 Firebase)
    override suspend fun fetchAndSaveFcmToken(): String? {
        // 로컬 확인
        val localToken = fcmTokenStore.getToken()
        if (!localToken.isNullOrBlank()) {
            PushManager.fcmToken = localToken // 메모리 캐시에도 동기화
            return localToken
        }

        // Firebase에서 가져오기 (비동기 -> 코루틴 변환)
        return suspendCancellableCoroutine { cont ->
            FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val newToken = task.result
                    Log.i("FCM", "Firebase token fetched: $newToken")

                    // 저장 (로컬 + 메모리)
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

    // 2. 서버에 등록하기
    override suspend fun registerFcmToken(token: String) {
        try {
            api.registerFcmToken(FcmTokenRequestDto(fcm_token = token))
        } catch (e: Exception) {
            Log.e("FCM", "registerFcmToken 실패", e)
        }
    }
}