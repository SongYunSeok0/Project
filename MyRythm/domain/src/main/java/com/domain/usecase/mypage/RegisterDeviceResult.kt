package com.domain.usecase.mypage

sealed interface RegisterDeviceResult {

    object Success : RegisterDeviceResult

    sealed interface Error : RegisterDeviceResult {
        val message: String

        object NotLoggedIn : Error {
            override val message = "로그인이 필요합니다"
        }

        object InvalidDeviceName : Error {
            override val message = "디바이스 별명을 입력해 주세요"
        }

        object BleConnectFailed : Error {
            override val message = "BLE 연결 실패"
        }

        object BleSendFailed : Error {
            override val message = "기기 전송 실패"
        }

        data class Unknown(
            override val message: String
        ) : Error
    }
}

