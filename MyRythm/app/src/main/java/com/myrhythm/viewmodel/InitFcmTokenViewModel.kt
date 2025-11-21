package com.myrhythm.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import com.data.core.push.FcmTokenStore
import com.data.core.push.PushManager
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class InitFcmTokenViewModel @Inject constructor(
    private val fcmTokenStore: FcmTokenStore,   // Hilt로 주입
) : ViewModel() {

    private val tag = "MainViewModel"

    fun initFcmToken() {
        val localToken = fcmTokenStore.getToken()
        if (localToken != null) {
            Log.i(tag, "FCM local token = $localToken")
            PushManager.fcmToken = localToken
            return
        }

        FirebaseMessaging.getInstance().token
            .addOnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Log.w(tag, "getToken 실패", task.exception)
                    return@addOnCompleteListener
                }

                val token = task.result
                Log.i(tag, "FCM firebase token = $token")

                PushManager.fcmToken = token
                fcmTokenStore.saveToken(token)

                // viewModelScope.launch { repository.registerFcmToken(token) }
            }
    }
}