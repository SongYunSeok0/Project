package com.domain.usecase.push

import com.domain.repository.PushRepository
import javax.inject.Inject

class GetFcmTokenUseCase @Inject constructor(
    private val repository: PushRepository
) {
    /**
     * FCM 토큰을 가져옵니다.
     * Repository 내부에서:
     * 1. 로컬 저장소(FcmTokenStore) 확인
     * 2. 없으면 Firebase SDK를 통해 받아오고 로컬에 저장 후 반환
     */
    suspend operator fun invoke(): String? {
        return repository.fetchAndSaveFcmToken()
    }
}