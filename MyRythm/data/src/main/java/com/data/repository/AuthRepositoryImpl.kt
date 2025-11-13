package com.data.repository

import com.core.auth.TokenStore
import com.data.mapper.auth.asAuthTokens
import com.data.mapper.auth.toDto
import com.data.mapper.auth.toDomainTokens
import com.data.network.api.UserApi
import com.data.network.dto.user.UserLoginRequest
import com.domain.model.AuthTokens
import com.domain.model.SocialLoginParam
import com.domain.model.SocialLoginResult
import com.domain.repository.AuthRepository
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val api: UserApi,
    private val tokenStore: TokenStore
) : AuthRepository {

    override suspend fun login(id: String, pw: String): AuthTokens? {
        val res = api.login(UserLoginRequest(email = id, password = pw))

        if (!res.isSuccessful) return null
        val body = res.body() ?: return null

        val tokens = body.asAuthTokens()
        tokenStore.set(tokens.access, tokens.refresh)
        return tokens
    }

    override suspend fun socialLogin(param: SocialLoginParam): SocialLoginResult {
        val res = api.socialLogin(param.toDto())

        if (!res.isSuccessful) {
            return when (res.code()) {
                409, 428 -> SocialLoginResult.NeedAdditionalInfo
                else -> SocialLoginResult.Error(res.errorBody()?.string())
            }
        }

        val body = res.body() ?: return SocialLoginResult.Error("empty body")

        // 1) 서버가 명시적으로 추가정보 필요 플래그를 준 경우
        if (body.needAdditionalInfo == true) {
            return SocialLoginResult.NeedAdditionalInfo
        }

        // 2) 토큰이 정상으로 온 경우
        val hasTokens = !body.access.isNullOrBlank() && !body.refresh.isNullOrBlank()
        return if (hasTokens) {
            val tokens = body.toDomainTokens()
            tokenStore.set(tokens.access, tokens.refresh)
            SocialLoginResult.Success(tokens)
        } else {
            // 3) 토큰도 없고 플래그도 없으면 서버 응답 이상
            SocialLoginResult.Error("invalid token data")
        }
    }

    override suspend fun refresh(refreshToken: String): AuthTokens? {
        // 서버에 refresh 엔드포인트가 없으면 null 유지
        return null
    }

    override suspend fun tryRefreshFromLocal(): Boolean {
        val refresh = tokenStore.current().refresh ?: return false
        val newTokens = refresh(refresh) ?: return false
        tokenStore.set(newTokens.access, newTokens.refresh)
        return true
    }

    override suspend fun clearTokens() {
        tokenStore.clear()
    }
}
