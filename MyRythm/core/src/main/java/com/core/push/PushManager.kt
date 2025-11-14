package com.core.push

object PushManager {
    @Volatile
    var fcmToken: String? = null
}