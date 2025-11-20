package com.domain.usecase.push
//나중에 공통 모듈로 이동할 예정
object PushManager {
    @Volatile
    var fcmToken: String? = null
}