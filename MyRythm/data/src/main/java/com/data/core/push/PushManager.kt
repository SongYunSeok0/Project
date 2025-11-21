package com.data.core.push

object PushManager {
    @Volatile
    var fcmToken: String? = null
}